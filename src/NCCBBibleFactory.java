import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.FileInputStream;

/**
 * National Conference of Catholic Bishops / United States Catholic
 * Conference
 */
class NCCBBibleFactory extends BibleFactory {

	protected int debug = 0;

	private TreeMap<String, String> translationMap = new TreeMap<String, String>();

	public NCCBBibleFactory() {
		super();
		translationMap.put("NAB", "New American Bible");
	}

	ArrayList<String> getSupportedTranslations() {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(translationMap.keySet());
		return l;
	}

	public Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException {
		this.library = library;
		assert ("NAB".equals(translation));
		if (translationMap.containsKey(translation)) {
			System.out.println("Reading "+translation);
			Translation t = this.readDirectory(new Path(inputDirectory, "NewAmericanBible", "NAM"), translationMap.get(translation), translation, indexOnly);
			t.verify();
			return t;
		} else {
			return null;
		}
	}

	public Translation readDirectory(Path dir, final String fullname, final String shortname, final LibraryIndex indexOnly) throws IOException {
		if (debug >= 1) System.out.println("Reading directory " + dir.toString());
		Translation t = new Translation(this.library);
		t.setFullname(fullname);
		t.setShortname(shortname);


		final String bookFileNames = "(amos|baruch|1chronicles|2chronicles|colossians|1corinthians|2corinthians|daniel|deuteronomy|ecclesiastes|ephesians|esther|exodus|ezekiel|ezra|galatians|genesis|habakkuk|hebrews|haggai|hosea|isaiah|james|job|judith|jeremiah|judges|joel|john|1john|2john|3john|jonah|joshua|jude|1kings|2kings|lamentations|luke|leviticus|malachi|1maccabees|2maccabees|micah|mark|matthew|nahum|nehemiah|numbers|obadiah|philippians|philemon|proverbs|psalms|1peter|2peter|romans|ruth|revelation|sirach|1samuel|2samuel|songs|tobit|1thessalonians|2thessalonians|titus|1timothy|2timothy|wisdom|zechariah|zephaniah)";

		File dirFile = new File(dir.toString());
		assert(dirFile.isDirectory());
		for (File file : dirFile.listFiles(new FilenameFilter(){ // anonymous inner class
					public boolean accept(File acceptDir, String acceptName) {
						return acceptName.matches("^"+bookFileNames+"[0-9]+.htm$");
					}})) {
			try {
				if (debug >= 1) System.out.println("Reading file "+file.getCanonicalPath());
				readBookFile(t, new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")), file.getName(), indexOnly);
			} catch (IOException e) {
				System.out.println("Error reading file "+file.getCanonicalPath());
				throw e;
			}
		}

		assert (t.shortname.equals("NAB"));
		t.setCopyright("New American Bible Copyright 1991, 1986, 1970 Confraternity of Christian Doctrine, Inc., Washington, DC. All rights reserved. Neither this work nor any part of it may be reproduced, distributed, performed or displayed in any medium, including electronic or digital, without permission in writing from the copyright owner.  </p>");

		return t;
	}

	private void readBookFile(Translation t, BufferedReader r, String filename, LibraryIndex indexOnly) throws IOException {
		String bookName = null;
		int chapterNumber = 0;
		Pattern bookNamePattern = Pattern.compile("^<BLOCKQUOTE><center><b><font size=[0-9]+ color=\"#[A-Z0-9]{6}\">([^<]+)</font><BR>$");
		Pattern chapterNumberPattern = Pattern.compile("^<I>Chapter ([0-9]+)</I></b></center>$");
		Pattern versePattern = Pattern.compile("^[\\\\]?\\s*<DT>?\\s*((?:[A-Z]:)?[0-9].*)$");
		Book b = null;
		Chapter c = null;
		REHelper re = new REHelper();
		while (true) {
			String s = r.readLine();
			if (null == s)
				break;

			if (null == bookName) {
				assert (null == b && null == c);
				if (re.strMatches(s, bookNamePattern)) {
					bookName = BookDescription.GetBookName(re.group(1));
					b = t.getBook(bookName);
				}
			} else if (chapterNumber == 0 && b != null) {
				assert (null == c);
				if (re.strMatches(s, chapterNumberPattern)) {
					chapterNumber = Integer.valueOf(re.group(1));
					c = b.createChapter(Integer.valueOf(chapterNumber), filename);
                                        if (indexOnly != null) {
                                            indexOnly.addSupport(c);
                                            break;
                                        }
				}
			} else {
				if (re.strMatches(s, versePattern)) {
					assert (null != b && null != c);
					parseVerses(c, re.group(1));
				} else if (s.contains("<DT")) {
					String msg = "Unable to read verses. (" + t.shortname + " " + filename + ")" + s;
					throw new Error(msg);
				}
			}

		}
	}

	private void parseVerses(Chapter c, String text) {
		assert (null != c);
		REHelper re = new REHelper();

		String[] verses = text.split("<DT>");

		for (String v : verses) {
			if (v.equals("")) {
				continue;
			} else if (re.strMatches(v, "^\\s*([0-9]+) *(?i:<DD>)(.*)$")) {
				int verseNumber = Integer.valueOf(re.group(1));
				c.addVerse(verseNumber, cleanVerse(c, re.group(2)) + "</br>");
			} else if (re.strMatches(v, "^[A-Z]:([0-9]+) *(?i:<DD>)(.*)$")) {
				int verseNumber = Integer.valueOf(re.group(1));
				c.addVerse(verseNumber, cleanVerse(c, re.group(2)) + "</br>");
			} else if (re.strMatches(v, "^[0-9]+\\s+$")) {
				// do nothing
			} else {
				String msg = "Unable to read verse. (" + c.fullString() + " " + c.filename + ") " + v;
				throw new Error(msg);
			}
		}
	}

	private String cleanVerse(Chapter c, String text) {
		// remove footnotes
		text = text.replaceAll("(?i:<SUP><A HREF=\"#[0-9]+\">[0-9]+</A></SUP>)", "");
		text = text.replaceAll("(?i:<A HREF=\"#[0-9]+\"><SUP>[0-9]+</SUP></A>)", "");
		return text;
	}
}
