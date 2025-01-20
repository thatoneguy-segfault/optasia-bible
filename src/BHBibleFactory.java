import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;

class BHBibleFactory extends BibleFactory {

	private TreeMap<String, String> translationMap = new
		TreeMap<String, String>();

	public BHBibleFactory() {
		super();
		translationMap.put("BHS",
				"Braille Biblia Hebraica Stuttgartensia");
	
	}

	ArrayList<String> getSupportedTranslations() {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(translationMap.keySet());
		return l;
	}

	public Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException {
		this.library = library;
		if (translationMap.containsKey(translation)) {
			//if (indexOnly != null) {
				//System.out.println("Reading " + translation + " for indexing" );
			//} else {
				//System.out.println("Reading " + translation);
			//}

			Translation t = readFiles(new Path(inputDirectory,  "Hebrew", "BHS"), new Path(inputDirectory,  "Hebrew", "BHS", "books.txt"), translationMap.get(translation), translation, indexOnly);
			readAbout(t, new Path(inputDirectory, "Hebrew","BHS","Key to Hebrew Braille.html.part"));
			readCopyright(t, new Path(inputDirectory, "Hebrew","BHS","Credit.html.part"));
			t.verify();
			return t;
		} else {
			return null;
		}
	}

	public Translation readFiles(Path inputDirectory, Path filename, String fullname, String shortname, LibraryIndex indexOnly) throws IOException {
		final Translation t = new Translation(library);
		t.setFullname(fullname);
		t.setShortname(shortname);
		final BufferedReader r;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(filename.toString()), "UTF-8"));
		} catch (IOException e) {
			System.out.println("Error reading file " + filename);
			throw e;
		}

		String s;
		while (true) {
			s = r.readLine();
			if (s == null) {
				break;
			}
			readFile(new Path(inputDirectory, s), t, indexOnly);
		}

		return t;

	}


	public void readFile(Path filename, Translation t, LibraryIndex indexOnly) throws IOException {
		final BufferedReader r;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(filename.toString()), "UTF-8"));
		} catch (IOException e) {
			System.out.println("Error reading file " + filename);
			throw e;
		}

		String s;
		String heading = null;
		String bookname = fixBookName(r.readLine());
		Boolean bookNameAdded = false;

		Book b = t.getBook(bookname);
		Chapter c = null;

		while (true) {
			s = r.readLine();
			if (s == null) {
				break;
			}

			// Verses are formatted as "  1:1 verse_text"

			Matcher m = Pattern.compile("^\\s+([0-9]+):([0-9]+)\\s+(.*)$").matcher(s);
			if (m.matches()) {
				int chapterNumber = Integer.parseInt(m.group(1));
				int verseNumber = Integer.parseInt(m.group(2));
				String text = m.group(3);

				c = b.getChapter(chapterNumber);


				if (null != indexOnly) {
                                        if (!indexOnly.supports(c)) {
                                            indexOnly.addSupport(c);
                                        }
				} else {
					if (null != heading) {
						c.data.add(new Heading(c, 1, cleanVerse(c, heading)));
						heading = null;
					}
					c.addVerse(verseNumber, cleanVerse(c, text) + "</br>");
				}
			} else if (s.matches("^\\s*$")) {
				// do nothing
			} else {
				if (heading != null) {
					heading += "<br/>";
				}
				heading = s;
			}
		}

		r.close();
	}

	private String cleanVerse(Chapter c, String text) {
		return cleanHtmlCodes(text);
	}

	private void readAbout(Translation t, Path filename) throws IOException {
		t.setAbout(readStringFromFile(filename));
	}

	private void readCopyright(Translation t, Path filename) throws IOException {
		t.setCopyright(readStringFromFile(filename));
	}

}
