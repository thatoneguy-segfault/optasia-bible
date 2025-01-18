import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

class CatholicOrgFactory extends BibleFactory {

	private TreeMap<String, String> translationMap;

	public CatholicOrgFactory() {
		super();
		translationMap = new TreeMap<String, String>();
		translationMap.put("NJB", "New Jerusalem Bible");
		translationMap.put("NJBLink", "New Jerusalem Bible");
	}

	// CatholicOrgFactory
	ArrayList<String> getSupportedTranslations() {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(translationMap.keySet());
		return l;
	}

	// CatholicOrgFactory
	public Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException {
		this.library = library;
		if (translationMap.containsKey(translation)) {
			Translation t = this.readDirectory(
					new Path(inputDirectory, "catholic_org", translation.toUpperCase().substring(0,3)),
					translationMap.get(translation), translation, indexOnly);
			t.verify();
			return t;
		} else {
			return null;
		}
	}

	// CatholicOrgFactory
	public Translation readDirectory(Path dir, String fullname, String shortname, LibraryIndex indexOnly) throws IOException {
		Translation t = new Translation(this.library);
		t.setFullname(fullname);
		t.setShortname(shortname);
		for (File file : dir.toFile().listFiles()) {
			Matcher m = Pattern.compile("^([^.]+)_book.php.id=\\d+&bible_chapter=(\\d+)$").matcher(file.getName());
			if (m.matches()) {
				String book = m.group(1);
				int chapter = Integer.valueOf(m.group(2));
				assert chapter != 0;
				assert !book.equals("");
				book = fixBookName(book);

				if (null != indexOnly) {
					indexOnly.addSupport(shortname, book, chapter);
				} else {
					try {
						readChapterFile(t, book, chapter, new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")), file.getName());
					} catch (IOException e) {
						System.out.println("Error reading file" + file.getName());
						throw e;
					}
				}
			}
		}

		assert (t.shortname.equals("NJB") || t.shortname.equals("NJBLink"));
		t.setCopyright("<p>New Jerusalem Bible, copyright 1985 by Darton, Longman & Todd, Ltd. and Doubleday, a division of Random House, Inc</p>");

		return t;
	}

	private void readChapterFile(Translation t, String book, int chapter, BufferedReader r, String filename) throws IOException {

		if (!BookComparator.bookOrder.containsKey(book)) {
			throw new AssertionError("Unknown book: '"+book+"'");
		}

		Book b = t.getBook(book);
		Chapter c = b.createChapter(chapter);

		String s;


		boolean foundtext = false;
		while (true) {
			s = r.readLine();
			if (s == null) break;
			if (s.matches("^\\s*<li(?: class=\"active\")?><a href=\"/bible/book.php.id=\\d+&bible_chapter=\\d+\">&raquo;</a></li>\\s*$")) {
				foundtext = true;
				break;
			}
			if (s.matches("^\\s*<li class=\"disabled\"><a href=\".\">&raquo;</a></li>\\s*$")) {
				foundtext = true;
				break;
			}

		}
		assert foundtext == true;
		s = r.readLine();
		assert (s.matches("^\\s*</ul>\\s*$"));

		REHelper re = new REHelper();

		while (true) {
			s = r.readLine();
			String verseText;
			int verse;
			if (s == null) break;

			// End of verses
			if (s.matches("^\\s*<div id=\"pager-Chapter\">\\s*$")) {
				break;
			}

			if (re.strMatches(s, "^\\s*<p><a name=\"(\\d+)\"></a><sup>\\d+</sup>(.*)$")) {
				verse = Integer.valueOf(re.group(1));
				assert (verse != 0);
				verseText = re.group(2);
				parseVerse(c, verse, verseText);
			} else {
				System.err.println("Error processing line of text in " + book + " " + chapter);
				System.err.println(filename);
				System.err.println(s);
				assert (false);
			}
		}
	}

	/**
	 * clean up the verse text and add it.
	 */
	private void parseVerse(Chapter chapter, int verseNum, String verseText) {
		verseText = cleanUnicode(new StringBuilder(verseText), chapter).toString();
		//verseText = cleanHtml(verseText);
		chapter.addVerse(verseNum, cleanVerse(chapter, verseNum, verseText));
	}

	/**
	 * Clean up anything particular to a verse.
	 * CatholicOrgFactory
	 */
	private String cleanVerse(Chapter c, int verseNum, String text) {
		assert text != null;
		text = cleanText(c, text);
		return text;
	}

	/**
	 * Clean up mistakes, etc.  CatholicOrgFactory
	 */
	private String cleanText(Chapter c, String s) {
		if (c.book.translation.shortname.contains("link")) {
			return s;
		} else {
			return removeAllHTML(s).concat("<br/>");
		}
	}

}
