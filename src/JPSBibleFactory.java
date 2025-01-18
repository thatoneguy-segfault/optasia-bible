import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;

public class JPSBibleFactory extends BibleFactory {

	protected TreeMap<String, String> translationMap;

	public JPSBibleFactory() {
		super();
		translationMap = new TreeMap<String, String>();
		translationMap.put("JPS", "Jewish Publication Society English Tenakh");
	}

	public ArrayList<String> getSupportedTranslations() {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(translationMap.keySet());
		return l;
	}

	public Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException {
		this.library = library;
		if (translationMap.containsKey(translation)) {
			//if (indexOnly != null) {
				//System.out.println("Reading "+translation+" for indexing");
			//} else {
				//System.out.println("Reading "+translation);
			//}

			Translation t = readFiles(new Path(inputDirectory, "Hebrew", "JPS"),
					new Path(inputDirectory, "Hebrew", "JPS", "books.txt"),
					translationMap.get(translation), translation, indexOnly);
			readAbout(t, new Path(inputDirectory, "Hebrew", "JPS", "about.html.part"));
			readCopyright(t, new Path(inputDirectory, "Hebrew", "JPS", "copyright.html.part"));
			t.verify();
			return t;
		} else {
			return null;
		}
	}

	public Translation readFiles(Path inputDirectory, Path filename, String fullname, String shortname, LibraryIndex indexOnly) throws IOException {
		final Translation t = new Translation(this.library);
		t.setFullname(fullname);
		t.setShortname(shortname);
		final BufferedReader r;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(filename.toString()), "UTF-8"));
		} catch (IOException e) {
			System.out.println("Error reading file "+filename.toString());
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
			System.out.println("Error reading file "+filename);
			throw e;
		}

		String s;
		//String heading;
		String bookname = fixBookName(r.readLine());
		//Boolean bookNameAdded = false;

		Book b = t.getBook(bookname);

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

				Chapter c = b.getChapter(chapterNumber);

				if (null != indexOnly) {
					indexOnly.addSupport(c);
				} else {
					c.addVerse(verseNumber, cleanVerse(c, text) + "</br>");
				}
			} else if (s.matches("^\\s*$")) {
				// do nothing
			} else {
				throw new Error(t.shortname+" ("+filename+"): Unable to parse line '"+s+"'");
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
