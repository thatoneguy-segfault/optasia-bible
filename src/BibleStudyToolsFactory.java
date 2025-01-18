import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

class BibleStudyToolsFactory extends BibleFactory {

	private TreeMap<String, String> translationMap;

	public BibleStudyToolsFactory() {
		super();
		translationMap = new TreeMap<String, String>();
		translationMap.put("NRSV", "New Revised Standard Version");
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
					new Path(inputDirectory, "biblestudytools", translation.toLowerCase()),
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
				if (book.equals("Psalms")) {
					book = "Psalm";
				}


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

		assert (t.shortname.equals("NRSV"));
		t.setCopyright("<p> New Revised Standard Version Bible, NRSV<br />Copyright 1989, Division of Christian Education of the National Council of the Churches of Christ in the United States of America. All rights reserved.</p>");

		return t;
	}

	private void readChapterFile(Translation t, String book, int chapter, BufferedReader r, String filenamel) throws IOException {
		book = fixBookName(book);

		if (!BookComparator.bookOrder.containsKey(book)) {
			throw new AssertionError("Unknown book: '"+book+"'");
		}

		// The scripture appears to be a single line that contains
		// a <input ... name="NavFirstChapter"> tag.
		boolean foundtext = false;
		while (true) {
			String s = r.readLine();
			if (s == null) break;

			if (s.matches(".*name=\"NavFirstChapter\".*BLOCKQUOTE.*")) {
				assert foundtext == false;
				parseText(t, book, chapter, s);
				foundtext = true;
			}
		}
		assert foundtext == true;
	}

	private void parseText(Translation t, String book, int chapter, String s) {
		Book b = t.getBook(book);
		Chapter c = b.createChapter(chapter);
		parseChapter(c, s);
	}

	private void parseChapter(Chapter chapter, String s) {
		// remove extranious stuff at beginning and end
		s = s.replaceAll("^.*<BLOCKQUOTE> *", "");
		s = s.replaceAll("</BLOCKQUOTE>.*$", ""); // Also removes footnotes

		s = s.replaceAll("<A HREF=\"#F[0-9]+\"><FONT SIZE=\"1\"><SUP>F[0-9]+</SUP></FONT></A> *", ""); // remove footnote references.

		// Split for verses.  In NRSV, <B><I> is unique to only be followed by a number and </I></B>
		String verseSplit = "<B><I>";

		// Add each portion as a chapter entry.
		String[] split = s.split(verseSplit);
		assert split.length > 1; // there's always at least 1 verse.
		for (String chapterEntry : split) {
			parseChapterEntry(chapter.data, chapter, chapterEntry);
		}
	}

	/**
	 * Recursively parse chapter entries.  BibleStudyTools
	 */
	private void parseChapterEntry(ArrayList<ChapterEntry> data, Chapter c, String s) {
		if (s == null) return; // base case

		s = cleanUnicode(new StringBuilder(s), c).toString();
		s = cleanHtml(s);
		REHelper re = new REHelper();

		if (s.matches("^\\s*$")) { // whitespace
			// do nothing;
		} else if (re.strMatches(s, "^([0-9]+)</I></B>(.*)$")) {
			int verseNum = Integer.parseInt(re.group(1));
			String verseText = re.group(2);
			if (verseText.equals("")) {
				System.out.println("Warning " + c.fullString()+":"+verseNum+" has no text");
				return;
			}
			data.add(new Verse(c, verseNum, 0, cleanVerse(c, verseNum, verseText)));
		} else if (re.strMatches(s, "^(.*)(<[pP]>)(.*)$")) {
			// paragraph
			parseChapterEntry(data, c, re.group(1));
			data.add(new HtmlVerse(c, re.group(2)));
			parseChapterEntry(data, c, re.group(3));
		} else {
			throw new AssertionError(c.fullString() + " Unrecognized chapter entry: '" + s + "'");
		}
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
