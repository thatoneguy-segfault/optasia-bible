import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * In 2010, Biblegateway changed how they layed out their files.
 * There are many similarities.
 */
class BibleGatewayFactory3 extends BibleGatewayFactoryBase {
	BibleGatewayFactory3() {
		super();
		translationMap = new TreeMap<String, String>();
		translationMap.put("CEB", "Common-English-Bible-CEB");
	}

	// BibleGateway names their translations by number
	TreeMap<String,String> translationMap;

	ArrayList<String> getSupportedTranslations() {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(translationMap.keySet());
		return l;
	}

	Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException {
		if (translationMap.containsKey(translation)) {
			//def filepattern = ~/index.html\?search=[+0-9A-Za-z]+\+[0-9]+&amp;version=[A-Z]+/
			String filepattern = ".*[A-Z0-9]+_(?:[0-9]_)?[A-Za-z_]+_[0-9]+\\.html$";
			Translation t = this.readDirectory(
					new Path(inputDirectory, "biblegateway3", translationMap.get(translation)),
					translation, filepattern, indexOnly);
			t.verify();
			return t;
		} else {
			return null;
		}
	}

	/**
	 * Identify the portion of the chapter file containing the scripture.
	 * BibleGateway3
	 */
	protected void readChapterFile(Translation t, BufferedReader r, LibraryIndex indexOnly) throws IOException {
		// Unfortunately, the html isn't XML compliant,
		// and doesn't support XmlParser or XmlSlurper
		String book = null;
		String chapter = null;

		// The scripture is inside a div class tag.
		while (true) {
			String s = r.readLine();
			if (s == null) break;

			Matcher m = Pattern.compile("^.*?<div class='heading passage-class-0'><h3>((?:[0-9] )?[A-Za-z ]+) ([0-9]+) <\\/h3>.*?$").matcher(s);
			if (m.matches()) {
				assert (book == null);
				assert (chapter == null);
				book = m.group(1);
				chapter = m.group(2);

				book = fixBookName(book);
				s = r.readLine(); // assume scripture is on the next line
				parseText(t, s, indexOnly, book, chapter);
				return;
			}
		}

		throw new AssertionError("Expected verses in file");
	}

	protected void parseText(Translation t, String s, LibraryIndex indexOnly, String strBook, String strChapter) {
		assert(strBook != null);
		assert(strChapter != null);
		Book b = t.getBook(strBook);
		Chapter c = b.createChapter(Integer.parseInt(strChapter));
		if (null != indexOnly) {
			indexOnly.addSupport(c);
			return; // no more processing.  Only index.
		}

		REHelper re = new REHelper();

		// process chapter heading e.g. <h4>1 Chronicles 1</h4>
		if (re.strMatches(s, "^<h4>([^<]+?)</h4>(.*?)$")) {
			//if (!re.group(1).equals(strBook + " " + strChapter)) {
				//System.out.println(c.fullString() + " Error:  '" + re.group(1) + "' != '" + strBook + " " + strChapter + "'");
				//assert (false);
			//}
			s = re.group(2);
		}

		//Note: current download of CEB has two different formats.  The version
		// was probably updated in the middle of the download.

		final String verseRegex1 = "<sup class=\"versenum\" id=\"en-CEB-\\d+?\">(\\d+?)(?:-(\\d+?))?</sup>";


		final String verseRegex2 = "^"
			 + "<span (?:id=\"en-[A-Z]+?-[0-9]+?\" )?class=\"text [0-9A-Za-z]+?-([0-9]+?)-([0-9]+?)(?:-[0-9A-Za-z]+?-[0-9]+?-([0-9]+?))?\">" // verse markings
			 + "(?:<span class=\"chapternum\">[0-9]*?[^<]+</span>|<sup class=\"versenum\">[0-9]+[^<]*?</sup>|)" // optional tags
			+ "(.*?)$"; // text after html tags
		// 1 = chapter number as string
		// 2 = verse number as string
		// 3 = postfix


		final String htmlRegex = "^((?:<br />|<p>|</p>|<p />|\\s|&nbsp;|<table>|<tr>|<td>|</tr>|</td>|</table>)+?)(.*?)$";

		final String replacePRegex = "^"
			+ "<p class=\"(?:line|chapter-\\d+?|\\s|left-1|first-line-none)+\">"
			+ "(.*?)$";

		final String ignoreRegex = "^(?:(?:"
			 + "<p class=\"line\">"
			 + "|<div class=\"(?:list|passage-scroller|left-1|poetry|line|\\s|child-vertical-none|top-1)+?\">"
			 + "|</div>"
			+ ")+?)(.*?)$";

		s = removeParsedFootnotes(s);
		s = removeParsedOtherSpan(s);
		s = cleanUnicode(new StringBuilder(s), c).toString();

		while (!s.equals("")) {

			if (re.strMatches(s, htmlRegex)) {
				// HTML to copy over
				c.addHtml(re.group(1));
				s = re.group(2);
			} else if (re.strMatches(s, replacePRegex)) {
				c.addHtml("<p>");
				s = re.group(1);
			} else if (re.strMatches(s, ignoreRegex)) {
				// HTML to ignore
				s = re.group(1);
			} else if (re.strMatches(s, "^<h5 class=\"passage-header\">(.*?)</h5>(.*)$")) {
				c.add(new Heading(c, 5, re.group(1)));
				s = re.group(2);
			} else if (re.strMatches(s, "^" + verseRegex1 + "(.*?)$")) {
				String strVerseNum1 = re.group(1);
				String strVerseNum2 = re.group(2);
				int verseNum1 = Integer.parseInt(strVerseNum1);
				int verseNum2 = -1;
				try {
					verseNum2 = Integer.parseInt(strVerseNum2);
				} catch (NumberFormatException e) {
					// do nothing;
				}

				assert(verseNum1 > 0);

				String verseGroup = re.group(3);
				String verseText;

				int endVerseIndex = verseGroup.indexOf("<sup class=\"versenum\"");
				if (endVerseIndex > 0) {
					verseText = verseGroup.substring(0, endVerseIndex);
					s = verseGroup.substring(endVerseIndex);
				} else {
					verseText = verseGroup;
					s = "";
				}

				if (verseNum2 >= 0) {
					c.addVerse(verseNum1, verseNum2, verseText);
				} else {
					c.addVerse(verseNum1, verseText);
				}
			} else if (re.strMatches(s, verseRegex2)) {
				assert(strChapter.equals(re.group(1)));
				String strVerseNum1 = re.group(2);
				String strVerseNum2 = re.group(3);
				int verseNum1 = Integer.valueOf(strVerseNum1);
				assert (verseNum1 > 0);
				int verseNum2 = 0;
				if (strVerseNum2 != null && !strVerseNum2.equals("")) {
					verseNum2 = Integer.valueOf(strVerseNum2);
					assert (verseNum2 > 0);
				}


				String postfix = re.group(4);

				int end_verse_index = indexOfAfterEndTag(postfix, "<span", "</span>");
				String strVerse = postfix.substring(0, end_verse_index - "</span>".length());
				strVerse = removeUnexpectedVerseSup(strVerse);

				if (verseNum2 > 0) {
					c.addVerse(verseNum1, verseNum2, strVerse);
				} else {
					c.addVerse(verseNum1, strVerse);
				}

				s = postfix.substring(end_verse_index);
			} else if (re.strMatches(s, "^<h3><span (?:id=\"en-[A-Z]+?-\\d+?\" )?class=\"text [0-9A-Za-z]+?-\\d+?-\\d+?\">(.*?)</span></h3>(.*)$")) {
				c.add(new Heading(c, 3, re.group(1)));
				s = re.group(2);
			} else if (re.strMatches(s, "^<b>([^<]+?)</b>(.*?)$")) {
				c.add(new Heading(c, 4, re.group(1)));
				s = re.group(2);
			} else {
				if (s.length() > 500) {
					System.out.println(s.substring(0, 500));
				} else {
					System.out.println("\""+s+"\"");
				}
				assert(false);
			}
		}

	}


	protected String removeParsedFootnotes(String s) {
		StringBuilder result = new StringBuilder();
		REHelper re = new REHelper();
		while (true) {
			// Footnote Reference
			if (re.strMatches(s, "^(.*?)(<sup class='footnote'.*)$")) {
				result.append(re.group(1));
				s = re.group(2);

				// throw away everything before the end tag
				s = s.substring(indexOfAfterEndTag(s, "<sup", "</sup>"));

				// continue
			} else if (re.strMatches(s, "^(.*?)<div class=\"footnotes\">.*$")) {
				result.append(re.group(1));
				break;
			} else { // no more footnotes
				result.append(s);
				break;
			}

		}

		return result.toString();
	}
	
	protected String removeUnexpectedVerseSup(String s) {
		REHelper re = new REHelper();
		while (true) {
			if (re.strMatches(s, "^(.*?)<sup class=\"versenum\">\\d+.?</sup>(.*?)$")) {
				s = re.group(1) + re.group(2); // everything before and after <sup>...</sup>
			} else if (re.strMatches(s, "^(.*?)<sup>(?:a|b)</sup>(.*?)$")) {
				s = re.group(1) + re.group(2); // everything before and after <sup>...</sup>
			} else {
				return s;
			}
		}
	}

	/**
	 * Parse and remove things like <span class=indent-1-breaks>don't delete me</span>
	 */
	protected String removeParsedOtherSpan(String s) {
		StringBuilder result = new StringBuilder();
		REHelper re = new REHelper();
		final String otherSpanRegex = "^(.*?)<span (?:style=\"font-variant: small-caps\" )?class=\"(?:indent-\\d-breaks|indent-\\d|small-caps|inscription|\\s|chapter-\\d+?)+\">(.*?)$";
		while (true) {
			if (re.strMatches(s, otherSpanRegex)) {
				result.append(re.group(1));
				String postfix = re.group(2);
				int end_span_index = indexOfAfterEndTag(postfix, "<span", "</span>");
				s = postfix.substring(0, end_span_index - "</span>".length())
					+ postfix.substring(end_span_index);

			} else {
				result.append(s);
				break;
			}
		}

		return result.toString();
	}

	/**
	  * Find the index of first character after the end tag.
	  */
	public int indexOfAfterEndTag(final String s, final String start_tag, final String end_tag) {
		int current_index = 0;
		int levels = 1;
		final int end_tag_length = end_tag.length();

		// parse our way through the various levels.
		while (true) {
			int a = s.indexOf(start_tag, current_index+1);
			int b = s.indexOf(end_tag, current_index+1);

			// which comes first start or end
			if (a > current_index && b > current_index && a < b) { // start is first
				levels++;
				current_index = a;
			} else if (a == -1 && b > current_index) { //end is first
				levels--;
				current_index = b;
			} else if (a > current_index && b > current_index && b < a) { //end is first
				levels--;
				current_index = b;
			} else {
				throw new Error("Unable to find end_tag " + end_tag + ": " + s);
			}

			if (levels == 0) {
				return current_index + end_tag_length;
			}
		}
	}
}
