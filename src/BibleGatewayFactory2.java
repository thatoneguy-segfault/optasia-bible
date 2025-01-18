import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * In 2010, Biblegateway changed how they layed out their files.
 * There are many similarities.
 */
class BibleGatewayFactory2 extends BibleGatewayFactoryBase {
	BibleGatewayFactory2() {
		super();
		translationMap = new TreeMap<String, String>();
		
		//"CEB":"Common-English-Bible",
		translationMap.put("KJ21",
				"21st-Century-King-James-Version-KJ21-Bible");
		translationMap.put("DARBY",
				"Darby-Translation-Bible");
		translationMap.put("DRA",
				"Douay-Rheims-1899-American-Edition-DRA-Bible");
		translationMap.put("ESV",
				"English-Standard-Version-ESV-Bible");
		translationMap.put("GW",
				"GODS-WORD-Translation-GW-Bible");
		translationMap.put("GNT",
				"Good-News-Translation-GNT-Bible");
		translationMap.put("HCSB",
				"Holman-Christian-Standard-Bible-HCSB");
		translationMap.put("KJV",
				"King-James-Version-KJV-Bible");
		translationMap.put("MSG", "Message-MSG-Bible");
		translationMap.put("NASB",
				"New-American-Standard-Bible-NASB");
		translationMap.put("NCV",
				"New-Century-Version-NCV-Bible");
		translationMap.put("NIRV",
				"New-International-Readers-Version-NIRV-Bible");
		translationMap.put("NIV",
				"New-International-Version-NIV-Bible");
		translationMap.put("NIV1984",
				"New-International-Version-NIV-Bible-1984");
		translationMap.put("NIVUK",
				"New-International-Version-UK-NIVUK-Bible");
		translationMap.put("NKJV",
				"New-King-James-Version-NKJV-Bible");
		translationMap.put("NLT",
				"New-Living-Translation-NLT-Bible");
		translationMap.put("TNIV",
				"Todays-New-International-Version-TNIV-Bible");
		translationMap.put("WE",
				"Worldwide-English-New-Testament-WE");
		translationMap.put("WYC",
				"Wycliffe-New-Testament-WYC");
		translationMap.put("YLT",
				"Youngs-Literal-Translation-YLT-Bible");
	//KJ21 DARBY DRA ESV GW GNT HCSB KJV MSG NASB NCV NIRV NIV
	//NIVUK NKJV NLT TNIV WE WYC YLT
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
			String filepattern = "[A-Z0-9]+_(?:[0-9]_)?[A-Za-z_]+_[0-9]+\\.html";
			Translation t = this.readDirectory(
					new Path(inputDirectory, "biblegateway2", translationMap.get(translation)),
					translation, filepattern, indexOnly);
			if (null == indexOnly) {
				fixErratta(t);
			}
			t.verify();
			return t;
		} else {
			return null;
		}
	}

	/**
	 * Identify the portion of the chapter file containing the scripture.
	 * BibleGateway2
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

			Matcher m = Pattern.compile("^.*<h2 id=\"passage_heading\">((?:[0-9] )?[A-Za-z ]+) ([0-9]+)\\&nbsp;\\(['0-9A-Za-z ]+\\)<\\/h2.*$").matcher(s);

			if (m.matches()) {
				assert (book == null);
				assert (chapter == null);
				book = m.group(1);
				chapter = m.group(2);

				// TODO: a bit of a hack
				book = book.replaceAll("Songofsongs|(Song of Solomon)", "Song of Songs");
			}

			if (s.matches("^.*<div class=\"result-text-style-normal\">.*$")) {
				s = r.readLine(); // assume scripture is on the next line
				if (!s.matches("^.*<h4>.*<\\/h4>.*$")) {
					assert (book != null && chapter != null);
				}
				//TODO_01032016: strip out <h4> heading before parsing.
				parseText(t, s, indexOnly, book, chapter);
			}
		}
	}

	protected void fixErratta(Translation t) {
		if (t.shortname.equals("TNIV")) {
			Book b;
			Chapter c;

			// TNIV Psalm 136 should end with "His Love
			// Endures Forever"
			b = t.books.get("Psalm");
			c = b.chapters.get(136);

			ChapterEntry last = c.data.get(c.data.size() - 1);
			if (!last.toString().contains("His love endures forever")) {
				c.data.add(new HtmlVerse(c, "<h5>His love endures forever.</h5>"));
			}
			c = b.chapters.get(137);
			for (int i = 0; i < 5; i++) {
				if (c.data.get(i).toString().equalsIgnoreCase("<h5>His love endures forever.</h5>")) {
					c.data.remove(i);
					break;
				}
			}
		}
	}
	
}
