import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

class BibleStudyToolsFactory2 extends BibleFactory {

	private TreeMap<String, String> translationMap;

	public BibleStudyToolsFactory2() {
		super();
		translationMap = new TreeMap<String, String>();
		translationMap.put("MSGverse", "The Message - with individual verses");
	}

	// BibleStudyTools
	ArrayList<String> getSupportedTranslations() {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(translationMap.keySet());
		return l;
	}

	// BibleStudyTools
	public Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException {
		this.library = library;
		if (translationMap.containsKey(translation)) {
			Translation t = this.readDirectory(
					new Path(inputDirectory, "biblestudytools", translation),
					translationMap.get(translation), translation, indexOnly);
			t.verify();
			return t;
		} else {
			return null;
		}
	}

	// BibleStudyTools
	public Translation readDirectory(Path dir, String fullname, String shortname, LibraryIndex indexOnly) throws IOException {
		Translation t = new Translation(this.library);
		t.setFullname(fullname);
		t.setShortname(shortname);
                File[] files = dir.toFile().listFiles();
                if (null == files) {
                    throw new IOException("Directory does not exist or is not a directory: " + dir.toFile());
                }
		for (File file : dir.toFile().listFiles()) {
			Matcher m = Pattern.compile("^([-A-Za-z0-9]+)_([0-9]+).html$").matcher(file.getName());
			if (m.matches()) {
				String book = m.group(1);
				int chapter = Integer.valueOf(m.group(2));
				assert chapter != 0;
				assert !book.equals("");


				// Fix book names to match other translations.
				book = book.replaceAll("-", " ");
				String[] books = book.split(" ");
				book = "";
				for (int i = 0; i < books.length; i++) {
					if (books[i].matches("^(of|and|the)$")) {
						// do nothing
					} else if (books[i].length() == 1) {
						books[i] = books[i].toUpperCase();
					} else {
						books[i] = books[i].substring(0, 1).toUpperCase()
							+ books[i].substring(1);
					}
					if (i > 0) {
						book += " ";
					}
					book += books[i];
				}

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

		assert (t.shortname.equals("MSGverse"));
		t.setCopyright("<p>The Message, MSG<br />1993, 1994, 1995, 1996, 2000, 2001, 2002 by NavPress Publishing Group</p>");

		return t;
	}

	private void readChapterFile(Translation t, String book, int chapter, BufferedReader r, String filenamel) throws IOException {
		//System.out.println("Reading " + book + " " + chapter);

		if (!BookComparator.bookOrder.containsKey(book)) {
			throw new AssertionError("Unknown book: '"+book+"'");
		}

		Book b = t.getBook(book);
		Chapter c = b.createChapter(chapter);

		// The scripture appears to be nicely nested XML
		// <div class="scripture">
		//   <div id="v-1" class="verse font-small">
		//     <strong>1</strong>
		//     <span class="verse-1">
		//        Adam Seth Enosh
		//     </span>
		//  
		//   </div>
		//   <div id="v-2" class="verse font-small">
		//     <strong>2</strong>
		//     <span class="verse-2">
		//       Kenan Mahalalel Jared
		//     </span>
		//
		//  </div>
		//   

		boolean foundtext = false;
		while (true) {
			String s = r.readLine();
			if (s == null) break;
			if (s.matches("\\s*<div class=\"scripture\">\\s*")) {
				foundtext = true;
				break;
			}

		}
		assert foundtext == true;

		REHelper re = new REHelper();

		String verseText = "";
		int currentVerse = 0;
		while (true) {
			String s = r.readLine();
			if (s == null) break;

			if ((currentVerse != 0) && (re.strMatches(s, "\\s*<span\\s*"))) {
				assert(false); // We do not know how to handle nested spans
			} else if (re.strMatches(s, "\\s*<span class=\"verse-([0-9]+)\">")) {
				// get ready to start reading a verse
				assert (currentVerse == 0);
				currentVerse = Integer.valueOf(re.group(1));
				assert (currentVerse != 0);
			} else if (re.strMatches(s, "\\s*<span class=\"verse-.*")) {
				assert(false);
			} else if (re.strMatches(s, "^\\s*</span>\\s*$")) {
				assert (currentVerse != 0);
				parseVerse(c, currentVerse, verseText);
				verseText = "";
				currentVerse = 0;
			} else if (re.strMatches(s, "^\\s*<div class=\"row hidden-print\">\\s*$")) {
				// need a better way to find the end of the verses.
				break;
			} else if ((currentVerse != 0) && (re.strMatches(s, ".*<.*"))
					&& (!re.strMatches(s, ".*<\\\\n>.*"))) {
				throw new AssertionError("Unexpected HTML tag: " + s);
			} else if (currentVerse != 0) {
				verseText += s.trim() + " ";
			}
		}
		assert (currentVerse == 0);
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
	 * Clean up anything particular to a verse.  BibleStudyTools
	 */
	private String cleanVerse(Chapter c, int verseNum, String text) {
		assert text != null;
		text = cleanText(c, text);
		return text;
	}

	/**
	 * Clean up mistakes, etc.  BibleStudyTools
	 */
	private String cleanText(Chapter c, String s) {
		return s; // do nothing for BibleStudyTools
	}

}
