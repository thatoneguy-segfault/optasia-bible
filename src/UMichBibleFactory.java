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

class UMichBibleFactory extends BibleFactory {

	protected int debug = 0;

	private TreeMap<String, String> translationMap = new TreeMap<String, String>();

	public UMichBibleFactory() {
		super();
		translationMap.put("RSV", "Revised Standard Version");
	}

	ArrayList<String> getSupportedTranslations() {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(translationMap.keySet());
		return l;
	}

	public Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException {
		this.library = library;
		if (translationMap.containsKey(translation)) {
			System.out.println("Reading "+translation);
			Translation t = this.readDirectory(new Path(inputDirectory, "alt_downloads", translation.toLowerCase()), translationMap.get(translation), translation, indexOnly);
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

		File dirFile = new File(dir.toString());
		assert(dirFile.isDirectory());
		for (File file : dirFile.listFiles(new FilenameFilter(){ // anonymous inner class
					public boolean accept(File acceptDir, String acceptName) {
						//return acceptName.matches("^"+shortname.toLowerCase()+"-idx\\?type=DIV1\\&byte=[0-9]+$");
						return acceptName.matches("^"+shortname.toLowerCase()+"-idx.type=DIV1&byte=[0-9]+$");
					}})) {
			try {
				if (debug >= 1) System.out.println("Reading file "+file.getCanonicalPath());
				readBookFile(t, new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")), file.getName(), indexOnly);
			} catch (IOException e) {
				System.out.println("Error reading file "+file.getCanonicalPath());
				throw e;
			}
		}

		assert (t.shortname.equals("RSV"));
		t.setCopyright("<p>Revised Standard Version, RSV<br />Copyright 1946, 1952, 1971 (the Apocrypha is copyrighted 1957, 1977) by the Division of Christian Education of the  National Council of the Churches of Christ in the USA.</p>");

		return t;
	}

	private void readBookFile(Translation t, BufferedReader r, String filename, LibraryIndex indexOnly) throws IOException {
		while (true) {
			String s = r.readLine();
			if (s.matches("^<h3.*")) {
				assert s.matches(".*"+t.fullname+".*");
				s = r.readLine();
				assert s.matches("^<hr>$"); // the next line contains all of the content
				s = r.readLine();

				Matcher m = Pattern.compile("^The <cite>[^<>]+</cite>[^<>]+<a[^<]+</a>[^<>]+<hr><h2>([^<>]+)</h2><hr>(.*)$").matcher(s);
				m.matches();
				assert m.matches();
				String book = m.group(1);
				assert book.length() > 0;
				assert book.length() < 50;
				book = fixBookName(book);
				String next = m.group(2);
				String verse;
				String chapter;
				Chapter c;
				Book b;

				while (true) {
					// tokenize by verse / chapter
					m = Pattern.compile("^<h3>([A-Za-z_ 0-9]+)\\.(\\d+)</h3>(.*?)<hr>(.*)$").matcher(next);
					if (m.matches()) {
						assert m.group(1).length() < 50;
						// m[0][1] not used
						chapter = m.group(2);
						assert chapter.length() > 0;
						verse = m.group(3);
						next = m.group(4);

						b = t.getBook(book);
						try {
							c = b.createChapter(Integer.valueOf(chapter), filename);
						} catch (AssertionError e) {
							if (book.equals("Esther") && filename.endsWith("3888691") && chapter.equals("10")) {
								c = b.createChapter(17, filename);
							} else if (book.equals("Esther") && filename.matches(".*3888691$") && chapter.equals("11")) {
								c = b.createChapter(18, filename);
							} else {
								throw e;
							}
						}
						//print "read ${book} ${chapter}\n"

						if (null != indexOnly) {
							indexOnly.addSupport(c);
						} else {
							readChapter(t, b, c, verse);
							//assert "Read only one chapter" == "TEST"
						}


					} else  {
						assert next.matches("^</body></html>$");
						return;
					}

				}
			}
		}
	}

	private void parseChapterEntry(Translation t, Book b, Chapter c, String text, int versenum, int verseend) {
		//print "TEST parsing '${text.substring(0, 5)}...${text.substring(text.length()-5)}'\n"
		if (0 == versenum) {
			c.data.add(new Heading(c, 6, text));
		} else {
			c.data.add(new Verse(c, versenum, verseend, cleanVerse(c, text)));
		}
	}

	private void readChapter(Translation t, Book b, Chapter c, String chapterString) {
		int versenum = 0, verseend = 0;
		//print "TEST ${chapterString.substring(0, 50)}\n"

		c.data.add(new Heading(c, 5, b.title+" "+Integer.toString(c.number)));

		Pattern p = Pattern.compile("\\[<b>(\\d+)(?:-(\\d+))?</b>\\]");
		Matcher m = p.matcher(chapterString);

		int lastEnd = 0;

		while (m.find()) {
			//print "TEST verse ${m.group(1)} starts at ${m.end()}: '${chapterString.substring(m.end(), m.end()+50)}'\n"
			if (0 != lastEnd) {
				parseChapterEntry(t, b, c, chapterString.substring(lastEnd, m.start()), versenum, verseend);
			}
			lastEnd = m.end();

			versenum = 0;
			verseend = 0;
			//print "TEST verse end = '${m.group(2)}'\n"

			versenum = Integer.valueOf(m.group(1));
			assert (versenum > 0 || m.group(1).equals("0")) && versenum < 200;
			if (null != m.group(2)) {
				verseend = Integer.valueOf(m.group(2));
				assert verseend > 0 && verseend < 200 && versenum > 0;
			}



		}
		assert lastEnd != 0;
		parseChapterEntry(t, b, c, chapterString.substring(lastEnd), versenum, verseend);

		return;
	}

	private String cleanVerse(Chapter c, String text) {
		return text;
	}
}
