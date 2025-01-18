import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;

abstract class BibleFactory {
	protected ConcurrentLibraryIface library;
	abstract ArrayList<String> getSupportedTranslations();
	abstract Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException;

	/**
	 * replace prohibited characters with html equivalent
	 */
	protected String cleanHtmlCodes(String s) {
		s = s.replaceAll("&", "&amp;"); // This must be first
		s = s.replaceAll("\"", "&quot;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}

	/**
	 * Clean html artifacts to improve Braille.
	 */
	protected String cleanHtml(String s) {
		s = s.replaceAll("&nbsp;", "");
		s = s.replaceAll("&ndash;", " -- ");
		s = s.replaceAll("&[lr]dquo;", "&quot;");
		s = s.replaceAll("&lsquo;", "&rsquo;");

		// Replace "Small Caps" e.g. LORD

		s = s.replaceAll("<span style=\"font-variant.*?\">((?:[^<]|<i>|<\\/i>)+?)<\\/span>", "$1");
		s = s.replaceAll("<span class=\"small-caps\">([^<]+?)<\\/span>", "$1");


		// Remove certain tags
		s = s.replaceAll("<speech>|</speech>", "");


		return s;
	}

	/**
	 * Remove everything inside <>
	 */
	protected String removeAllHTML(String s) {
		StringBuffer b = new StringBuffer();
		int i = 0;
		int nestLevel = 0;
		while (true) {
			if (i == s.length()) {
				assert (nestLevel == 0);
				break;
			}

			if (s.charAt(i) == '<') {
				nestLevel = nestLevel + 1;
			} else if (s.charAt(i) == '>') {
				nestLevel = nestLevel -1;
			} else if (nestLevel == 0) {
				b.append(s.charAt(i));
			}
			
			i = i+1;
		}
		return b.toString();
	}

	/**
	 * Replace invalid book names with "valid" ones.
	 */
	protected String fixBookName(String s) {
		s = s.replaceAll("_", " ");
		//TODO Replace with a better book naming scheme
		if (s.equals("Psalms")) {
			return "Psalm";
		} else if (s.equals("Additions to the Book of Esther")) {
			return "Esther";
		} else if (s.equals("Song of Solomon")) {
			return "Song of Songs";
		} else if (s.equals("The Song of Songs")) {
			return "Song of Songs";
		} else if (s.equals("Wisdom of Jesus Son of Sirach")) {
			return "Sirach";
		} else if (s.equals("Ecclesiasticus   Sirach")) {
			return "Sirach";
		} else if (s.equals("151 Psalms")) {
			return "Psalm 151";
		} else if (s.equals("2 Thessalonian")) {
			return "2 Thessalonians";
		} else if (s.equals("Wisdom of Solomon")) {
			return "Wisdom";
		} else if (s.equals("The Proverbs")) {
			return "Proverbs";
		} else if (s.equals("Acts of Apostles")) {
			return "Acts";
		}
		return s;
	}


	private String cleanUnicodeError(StringBuilder s)
	{
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < s.length(); i++)
		{
			int c = s.codePointAt(i);
			if (c <= 0x7f) {
				result.append((char) c);
				i++;
			} else {
				result.append(String.format("&#%d", c));
			}
		}
		return result.toString();
	}

	protected StringBuilder cleanUnicode(StringBuilder s, Chapter chapter)
	{
		StringBuilder result = new StringBuilder();
		// First convert non-ascii characters into HTML-style decimal
		for (int i = 0; i < s.length(); i++)
		{
			int c = s.codePointAt(i);
			if (c <= 0x7f) {
				result.append((char) c);
			} else {
				switch(c) {
				case 8211:
				case 8212: result.append(" - "); break; // NIV Genesis 1:5
				case 160: result.append(""); break; // TNIV Genesis 1:31
				case 8220: result.append(""); break; // TNIV Psalm 107 errant quot
				case 65533: result.append(""); break; // TNIV Ecclesiastes 3:13 error
				case 194: result.append(" "); break; // TNIV Mark 15
				//case 8220:
				case 8221:
				case 8216: result.append("&quot;"); break; // NLT Genesis 1
				case 8217: result.append("'"); break; // NLT Genesis 10
				case 233: result.append("e"); break; // NLT Genesis 19 "fiance'"
				case 189: result.append(" and 1/2"); break; // NLT Exodus 27, 1/2
				case 188: result.append(" and 1/4"); break; // NLT Exodus 30, 1/4
				case 190: result.append(" and 3/4"); break; // NLT Numbers 7, 3/4
				case 8531: result.append(" and 1/3"); break; // NLT Ezekiel 45, 1/3
				case 8532: result.append(" and 2/3"); break; // NLT Ezekiel 45, 2/3
				case 150: result.append("-"); break; // NKJV Exodus 29
				case 145: result.append("'"); break; // NKJV 2 Samuel 18
				case 1488: case 1489: case 1490: case 1491: case 1492: case 1493:
				case 1494: case 1495: case 1496: case 1497: case 1498: case 1499:
				case 1500: case 1501: case 1502: case 1503: case 1504: case 1505:
				case 1506: case 1507: case 1508: case 1509: case 1510: case 1511:
				case 1512: case 1513:
				case 1514: result.append(""); break; // NKJV Psalm 119: Not able to translate
				case 226: case 128:
				case 148: result.append(" "); break; // GNT Genesis 1:3
				case 195: case 131:
				case 174: result.append(" "); break; // KJ21 1 Corinthians 11:13 (error in original)
				//case 195:
				case 402:
				//case 194:
				case 8218:
				case 185: result.append(" "); break; // DARBY 1 Samuel 2 (error in original)
				case 183: result.append(" "); break; // DRA
				case 8970:
				case 8971: result.append("&quot;"); break; // GW
				case 147: result.append("-"); break; // GNT 1 Maccabees 8
				case 169: result.append("ea"); break; // NKJV 1 Samuel 28; "seance"
				case 144: case 215: case 8226: case 732: case 8482: case 8250: case 339:
				case 382: case 161: case 162: case 164: case 166: case 167: case 168:
				case 170: result.append(""); break; //NJKV psalm 119 marks
				case 8230: result.append("..."); break; // NIV 2011
				case 239: result.append("i"); break; // an "i" with 2 dots instead of 1
				case 235: result.append("e"); break; // an "e" with 2 dots above it
				case 1474:
				case 1473: result.append(""); break; // CEB Psalm marks
				case 381: result.append("e"); break; // MSG-biblestudytools 2 Chronicles 33 verse 6, e with accent
				case 208: result.append("-"); break; // MSG-biblestudytools Ezekiel 41:20
				default:
					System.out.println(cleanUnicodeError(s));
					throw new AssertionError(String.format("%s %s %d, Unknown Unicode Character: &#%d",
						chapter.book.translation.shortname,
						chapter.book.title, chapter.number, c));
				}
			}
		}
		return result;
	}


	/**
	 * ReadFileNames - Read file names in a directory that match the filter
	 * Sort the file names numerically, assuming numeric fields are
	 * separated by @sep
	 *
	 * Non-numeric portions of the names are sorted alphabetically.
	 * e.g. brst_genesis_10.htm, brst_genesis_2.htm (sep='_')
	 * 
	 */
	//protected List<String> ReadFileNames(String directoryName, Pattern filter, String sep="_") {
	protected List<String> ReadFileNames(Path directoryName, String filter, final String sep) {
		final File dir = new File(directoryName.toString());
		String[] unfiltered_names = dir.list(null);
		List<String> filenames = new ArrayList<String>();
		for (String fname : unfiltered_names) {
			if (fname.matches(filter)) {
				filenames.add(fname);
			}
		}

		// Sort the file names
		Collections.sort(filenames, new Comparator<String>() {
			public int compare(String a, String b) {
				String[] A = a.split(sep);
				String[] B = b.split(sep);

				// If the strings are significantly different,
				// use a standard string comparison.
				if (A.length != B.length) {
					return a.compareTo(b);
				}

				int x;

				for (int i = 0; i < A.length; i++) {
					try { // Try integer comparison
						int a_int = Integer.parseInt(A[i]);
						int b_int = Integer.parseInt(B[i]);
						x = a_int - b_int;
					} catch (Exception e) { // String compare
						x = A[i].compareTo(B[i]);
					}
					if (x != 0) {
						return x;
					}
				}

				return 0;
			}
		});

		return filenames;
	}

	/**
	 * readStringFromFile() Read a text from a file, return as a string
	 */
	protected String readStringFromFile(Path filename) throws IOException {
		final BufferedReader r;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(filename.toString()), "UTF-8"));
		} catch (IOException e) {
			System.out.println("Error reading file " + filename);
			throw e;
		}

		String text = "";
		String s;
		while (true) {
			s = r.readLine();
			if (s == null) {
				break;
			}
			text += s;
		}
		return text;
	}
}
