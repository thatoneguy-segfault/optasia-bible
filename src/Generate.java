/**
 * Author: David Vos
 * Copyright Optasia Ministry 2007-2012
 *
 * Use of this program is restricted to Optasia Ministry personell only.
 * Any use of this script or its output must be used with permission of
 * the author.  HTML of downloaded files copywrite by BibleGateway.com,
 * Bible Content coprighted by owners of copyright for that particular version.
 * See respective licenses.
 *
 * This program takes downloaded Bibles from BibleGateway and formats
 * them in a manner easier to navigate for the blind.  This format primiarily
 * targets the PacMate platform, but is sufficiently generic that it works
 * for other platforms as well.  The design is such that it is a simple
 * html-driven framework so that standard screen-reading software can interact
 * with a web browser to provide the content and navigation.
 *
 * 2.0.1: provide command line support for separate distributions.
 * 2.1.0: provide BibleStudyTools reader.  Support NRSV.
 *        Change safe function to replace ' with _
 *	  Improved command line to allow control over what translations, etc.
 *        Provided command line support for individual and library printing
 * 2.1.1: Provide support for Apocrypha.
 *        Support 2010 BibleGateway format.
 * 2.1.2: Split files, added CVS support
 *        Add NIV 2011
 * 2.1.3: Add RSV
 *        Created fix bookname function
 * 2.1.4: Add Greek New Testament
 * 2.1.5: 2/2012, Braille Hebrew Bible; Hebrew Transliteration
 *        Added support for About page
 *        Copyright is now generated by the Bible Factory; allows verification
 *        JPS translation added
 *        CEB added
 * 2.2.0: 3/2012, Official Early Language support; Greek and Hebrew
 * 2.2.1: 3/2012, Renamed Original Language Library
 * 2.2.2: 5/2012, Fixed footnote references in NASB that led to words being
 *                merged.  May result in a few extra spaces between a word
 *                and punctuation.
 * 2.2.3: 7/2012, Added Greek About page for Braille and Screen Reader
 * 2.3: 3/2013-9/2013, Java port
 *        12/2013: multithreaded
 * 2.3.1: Fixed NTG titles
 * 2.3.2: Added NAB
 * 2.3.3: 12/27/15, Fixed MSG copyright, added MSGverse
 * 2.3.4: 1/1/16, Added chapter headings for book-text and search
 *        categories
 * 2.4.0: 3/2016, Catholic Bible support: NJB
 * 2.4.1: 4/2016, Fixed minor error in TNIV Psalm 136
 *                Translation names are sorted when printed with -h
 *
 *
 * TODO: The Voice by Thomas Nelson
 * TODO: New English Bible NET
 * TODO: New English Translation NEB
 * TODO: Catholic - Christian Community Bible
 * TODO: RSV C2 - catholic
 * TODO: Other Catholic bibles?
 *
 *
 *   TODO: For some versions such as NIV, in a book-text or search
 *   category, the chapter heading appears more than once.  Likely the
 *   original version source had the chapter heading and we don't
 *   strip it out.
 *       grep TODO_01032016
 *
 *
 *   TODO: fix links to verses e.g. 1-2; see MSG 2 Samuel 12
 *   TODO: MSG Acts 27 verses 11-12
 *   TODO  some verses only have (?:&nbsp)+  usually a duplicate verse 1
 *            see NCV Proverbs 13
 *   TODO: see comments "COMPARE"
 *   TODO: see TNIV Psalm 111 verse 1 (duplicate verse 1)
 *
 *
 * TODO:
 * RTF for each book; Stand alone RTF bibles, not linked into Optasia
 * Divide Catholic/Eastern Orthodox
 * "This Translation Includes Deutero.."
 *
 */


//TODO: footnotes (Don't work well in braille)
//TODO: Strong's numbers

//TODO: Extraneous lines in poetry (e.g. Psalms) due to input format

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.io.IOException;

public class Generate {

	private static Path defaultOutputDirectory = new Path("output");
	private static Path defaultInputDirectory = new Path("../optasia-bible-input");


	/**
	 * Create a generate() to prevent namespace mistakes.
	 */
	public static void generate(Path inputDirectory, Path outputDirectory, String[] args) throws IOException {
		String opt_generate = "OptasiaLibrary";
		String opt_crosslink = "none";
		int opt_nthreads = 2;

		CliBuilder cli = new CliBuilder("generate [translations]\n\tIf no translations are listed, "+opt_generate+" collection is processed with no crosslinks.");
		cli.add("h", "help", "Show usage information");
		cli.add("q", "query", "List the supported translations and collections.");
		cli.add("t", "test", "Call the test function. -- for developer use only.");
		cli.addStringArgAny("x", "crosslink",
				"<supported|generated|collection_name|none> what to crosslink the generated translations to",
				opt_crosslink);
		cli.addStringArgAny("g", "generate",
				"<collection_name|supported> Any command line arguments replace this list.",
				opt_generate);
		cli.addStringArgAny("n", "nthreads", "<number> Number of threads to run.", String.valueOf(opt_nthreads));
		cli.add("p", "production", "generate all supported translations without crosslink; then generate each collection with crosslink.  Overrides other generation arguments.");

		cli.parseArgs(args);


		Library library = new Library(inputDirectory);
		if (!library.getCollectionNames().contains(opt_generate)) {
			System.out.println("Error: default value '"+opt_generate+"' is not a collection: "+Util.join(", ", library.getCollectionNames()));
			System.exit(0);
		}

		if (cli.isSet("h")) {
			cli.printUsage();
			library.querry(cli.getOtherArgs());
			System.exit(1);
		} else if (cli.isSet("t")) { //test
			LibraryIndex.unitTest();

			System.out.println("Test complete");
			System.exit(0);
		}

		if (cli.isSet("g")) {
			opt_generate = cli.value("g");
		}
		if (cli.isSet("x")) {
			opt_crosslink = cli.value("x");
		}
		if (cli.isSet("n")) {
			try {
				opt_nthreads = Integer.valueOf(cli.value("n"));
			} catch (NumberFormatException e) {
				cli.printUsage();
				System.err.println("Error: number required for nthreads: " + cli.value("n"));
				System.exit(1);
			}
		}
		library.SetNumThreads(opt_nthreads);

		ArrayList<String> supported = library.getSupportedTranslations();

		// What translations to generate?
		ArrayList<String> translations = cli.getOtherArgs();
		if (translations.size() == 0) {
			if (opt_generate.equals("supported")) {
				translations = library.getSupportedTranslations();
			} else if (library.getCollectionNames().contains(opt_generate)) {
				translations = library.getCollectionTranslationNames(opt_generate);
			} else {
				System.out.println("genarate option '"+opt_generate+"' not supported");
				cli.printUsage();
				System.exit(1);
			}
		}

		// Which translations to crosslink to?
		ArrayList<String> crosslink = null;
		if (opt_crosslink.equals("supported")) {
			crosslink = supported;
		} else if (opt_crosslink.equals("generated")) {
			crosslink = translations;
		} else if (opt_crosslink.equals("none")) {
			crosslink = new ArrayList<String>();
		} else if (library.getCollectionNames().contains(opt_crosslink)) {
			crosslink = library.getCollectionTranslationNames(opt_crosslink);
		} else {
			System.out.println("crosslink option '"+opt_crosslink+"' not supported");
			cli.printUsage();
			System.exit(1);
		}

		if (cli.isSet("q")) {
			library.querry(cli.getOtherArgs());
			System.out.println("Crosslinking to the following translations:");
			System.out.println(Util.join(", ", crosslink));
			System.out.println();

			library.generateIndexes(crosslink);
			System.out.println("Index: ");
			library.printIndexSummary(System.out);
			System.exit(0);
		}


		if (cli.isSet("p")) { // Production run.  Ignore user xlink and translations.
			// This tries to be fancy and avoid processing a translation more than once
			// E.g. only process the NIV once, then write it out for each collection
			// and stand-alone
			library.generateProduction(outputDirectory);
		} else { // Normal run
			library.generateNormal(translations, crosslink, outputDirectory);
		}
	}

	public static void main(String[] args) throws IOException {
		generate(defaultInputDirectory, defaultOutputDirectory, args);
		System.out.println("Exiting main()");
		//System.exit(0);
		return;
	}

}
