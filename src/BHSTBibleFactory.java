import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;

class BHSTBibleFactory extends BibleFactory {

	private TreeMap<String, String> translationMap = new TreeMap<String, String>();
	
	public BHSTBibleFactory() {
		super();
		translationMap.put("BHST","Biblia Hebraica Stuttgartensia Transliterated");
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
				//System.out.println("Reading " + translation + " for indexing");
			//} else {
				//System.out.println("Reading "+translation);
			//}

			Translation t = readDirectory(new Path(inputDirectory, "Hebrew", "BHST"), translationMap.get(translation), translation, indexOnly);
			t.setCopyright(readStringFromFile(new Path(inputDirectory, "Hebrew", "BHST", "copyright.html.part")));
			t.verify();
			return t;
		} else {
			return null;
		}
	}

	public Translation readDirectory(Path inputDirectory, String fullname, String shortname, LibraryIndex indexOnly) throws IOException {
		List<String> filenames = ReadFileNames(inputDirectory,
				"^bhst_(?:[0-9]+_)?[a-z]+_[0-9]+.htm$", "_");

		final Translation t = new Translation(this.library);
		t.setFullname(fullname);
		t.setShortname(shortname);
		BufferedReader r;
		for (String filename : filenames) {
			try {
				r = new BufferedReader(new InputStreamReader(new FileInputStream(Path.path(inputDirectory, filename)), "UTF-8"));
				this.readFile(t, r, indexOnly);
				r.close();
			} catch (IOException e) {
				System.out.println("Error reading file " + filename);
				throw e;
			}
		}

		return t;
	}

	public void readFile(final Translation t, final BufferedReader r, LibraryIndex indexOnly) throws IOException {
		String s;

		s = r.readLine();
		Matcher m = Pattern.compile(".*<div align=\"center\">([^<]+)\\s(\\d+)<\\/div>.*").matcher(s);
		if (!m.matches()) {
			System.out.println("Error: unable to match line '"+s+"'");
			throw new AssertionError("match failed");
		}

		String bookname = fixBookName(m.group(1));
		String newline;
		int chapterNumber = Integer.parseInt(m.group(2));

		Book b = t.getBook(bookname);
		Chapter c = b.getChapter(chapterNumber);

		if (indexOnly != null) {
			return;
		}

		while (true) {
			// Verses are formatted as: <span class="reftext"><a href="http://biblos.com/genesis/1-27.htm"><b>27</b></a></span>&nbsp;vai·yiv·ra e·lo·him et-ha·'a·dam be·tzal·mov be·tze·lem e·lo·him ba·ra o·tov za·char u·ne·ke·vah ba·ra o·tam.

			m = Pattern.compile(".*<span class=\"reftext\"><a href=\"http:\\/\\/biblos.com\\/[^\\/]+\\/\\d+-\\d+.htm\"><b>(\\d+)<\\/b><\\/a><\\/span>\\&nbsp;([^<]+)(<p>)?.*").matcher(s);
			if (m.matches()) {
				int verseNum = Integer.parseInt(m.group(1));
				String verseText = m.group(2);

				if (m.group(3) != null && m.group(3).equals("<p>")) {
					newline = "</br></br>";
				} else {
					newline = "</br>";
				}

				c.addVerse(verseNum, cleanVerse(c, verseText) + newline);
			}

			s = r.readLine();
			if (s == null) {
				break;
			}
		}

	}

	private String cleanVerse(Chapter c, String text) {
		// The BHST has an odd character between each hewbrew character
		text = text.replaceAll("\\xb7", "");
		return cleanHtmlCodes(text);
	}

}
