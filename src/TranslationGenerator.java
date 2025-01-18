import java.io.IOException;

public class TranslationGenerator implements Runnable {

	// used by multiple functions
	private final ConcurrentLibraryIface myLibrary;

	// used only by run().
	private final String myShortname;
	private final Path myOutputDirectory;
	private final LibraryIndex myLibraryIndex;
	private final boolean myProduction; // is this a "production" run?
	private final boolean runIndex; // Only process the Translation's Library Index

	private TranslationGenerator(ConcurrentLibraryIface library, String shortname, Path outputDir, LibraryIndex libIndex, boolean production, boolean runIndex) {
		this.myLibrary = library;
		this.myShortname = shortname;
		this.myOutputDirectory = outputDir;
		this.myLibraryIndex = libIndex;
		this.myProduction = production;
		this.runIndex = runIndex;
	}

	public static TranslationGenerator CreateTranslationIndexGenerator(ConcurrentLibraryIface library, String shortname, LibraryIndex libIndex) {
		return new TranslationGenerator(library, shortname, null, libIndex, false, true);
	}

	public static TranslationGenerator CreateTranslationReadWriteGenerator(ConcurrentLibraryIface library, String shortname, Path outputDirectory, boolean production) {
		return new TranslationGenerator(library, shortname, outputDirectory, null, production, false);
	}

	@Override
	public void run() {
		try {
			if (this.runIndex) {
				generateIndex(this.myShortname, this.myLibraryIndex);
			} else {
				generateAndWrite(this.myShortname, this.myOutputDirectory, this.myProduction);
			}
		} catch (IOException e) {
			System.out.println("Thread generated IOException: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (Throwable t) {
			System.out.println("Thread generated Exception: " + t.getMessage());
			t.printStackTrace();
			System.exit(1);
		}
	}

	private void generateIndex(String shortname, LibraryIndex libIndex) throws IOException {
		generate(shortname, libIndex);
	}

	private void generateAndWrite(String shortname, Path outputDirectory, boolean production) throws IOException {
		write(shortname, outputDirectory, production);
	}

	//
	// Create a Translation class given the shortname.
	//
	private Translation generate(String shortname, LibraryIndex indexOnly) throws IOException {
		System.out.println("Processing translation "+shortname + (null != indexOnly ? " for indexing" : ""));

		BibleFactory f = myLibrary.getBibleFactory(shortname);
		assert f != null;
		Translation t = f.translation(myLibrary, shortname, myLibrary.getInputDirectory(), indexOnly);
		assert t != null;
		if (null != indexOnly) {
			System.out.println("Indexing " + shortname + " complete");
		}
		return t;
	}

	// Read in a translation and write it out.
	// For efficiency of a full production output, allow more than
	// one output while only reading it in once.
	//
	private void write(String translation, Path outputDirectory, boolean production) throws IOException {
		Translation t = generate(translation, null);
		System.out.println("Writing: "+t.summarize());
		if (production) {
			// write the individual translation (not crosslinked)
			FilePrinter printer = new FilePrinter(new Path(outputDirectory, "production", "individual"), null);
			printer.visit(t);

			// write the translation for each collection
			for (String c : myLibrary.getCollectionNames()) {
				if (myLibrary.getCollectionTranslationNames(c).contains(translation)) {
					printer = new FilePrinter(new Path(outputDirectory, "production", "collections", c), myLibrary.getProductionIndex(c));
					printer.visit(t);
				}
			}
		} else {
			FilePrinter p = new FilePrinter(new Path(outputDirectory, "nonproduction"), myLibrary.getNonProductionIndex());
			p.visit(t);
		}
	}

	public String info() {
		return myShortname + (myProduction ? " production " : " nonproduction ") + (runIndex ? " Index Only " : " full processing ");
	}

}
