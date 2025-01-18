import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Original code to read the Bible Gateway format.
 */
class BibleGatewayFactory extends BibleGatewayFactoryBase {
	BibleGatewayFactory() {
		super();
		translationMap = new TreeMap<String, Integer>();
		translationMap.put("ASV", 8); // American Standard Version
		translationMap.put("CEV", 46); // Contemporary English Version
		translationMap.put("AMP", 45); // Amplified Bible

		//"NIV":31,  // New International Version
		//"TNIV":72,  // Today's New International Version
		//"NASB":49,  // New American Standard Version
		//"KJV":9,  // King James Version
		//"NLT":51,  // New Living Translation
		//"NKJV":50,  // New King James Version
		//"ESV":47,  // English Standard Version
		//"MSG":65,  // The Message
		//"HCSB":77,  // Holman Christian Standard Bible
		//"NIRV":76,  // New International Reader's Version
	}

	// BibleGateway names their translations by number
	TreeMap<String,Integer> translationMap;

	ArrayList<String> getSupportedTranslations() {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(translationMap.keySet());
		return l;
	}

	Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException {
		this.library = library;

		if (translationMap.containsKey(translation)) {
			//String filepattern = "^index.html\\\\?book_id=\\d+\\\\&chapter=\\d+\\\\&version=\\d+$";
			// Note: The '?' seems to work weird in Win7
			String filepattern = "^index.html.book_id=\\d+\\&chapter=\\d+\\&version=\\d+$";

			Translation t = this.readDirectory(
					new Path(inputDirectory, "biblegateway", Integer.toString(translationMap.get(translation))),
					translation, filepattern, indexOnly);
			t.verify();
			return t;
		} else {
			return null;
		}
	}
}



