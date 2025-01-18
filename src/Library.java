import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.util.Arrays;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Library implements ConcurrentLibraryIface {
	private final Path inputDirectory;
	private final LibraryIndex index = new LibraryIndex();
	private final ArrayList<BibleFactory> factories = new ArrayList<BibleFactory>();

	private final TreeMap<String, ArrayList<String> > collectionTree;
	private final TreeMap<String, String> collectionTitles;

	private int nThreads = 1;
	private volatile ThreadPool threadPool = null;

	// collection -> LibraryIndex
	private final TreeMap<String, LibraryIndex> productionIndex = new TreeMap<String, LibraryIndex>();

	public Library(Path inputDirectory) {
		this.inputDirectory = inputDirectory;

		collectionTree = new TreeMap<String, ArrayList<String> >();
		collectionTree.put("OptasiaLibrary",
				new ArrayList<String>(Arrays.asList(
					new String[]{"AMP", "ASV", "CEV", "ESV", "GNT", "HCSB", "KJV", "MSG", "NASB", "NIRV", "NIV", "NIV1984", "NKJV", "NLT", "NRSV", "TNIV"})));

		collectionTree.put("EarlyLanguageLibrary",
				new ArrayList<String>(Arrays.asList(
					new String[]{"NTGbrl", "NTGsr", "NRSV", "BHS", "BHST", "JPS"})));

		collectionTitles = new TreeMap<String, String>();
		collectionTitles.put("OptasiaLibrary", "Optasia Bible Library");
		collectionTitles.put("OriginalLanguageLibrary", "Optasia Bible Original or Early Language Library");

		factories.add(new BibleGatewayFactory());
		factories.add(new BibleGatewayFactory2());
		factories.add(new BibleGatewayFactory3());
		factories.add(new BibleStudyToolsFactory());
		factories.add(new BibleStudyToolsFactory2());
		factories.add(new UMichBibleFactory());
		factories.add(new GreekBibleFactory());
		factories.add(new BHBibleFactory());
		factories.add(new JPSBibleFactory());
		factories.add(new BHSTBibleFactory());
		factories.add(new NCCBBibleFactory());
		factories.add(new CatholicOrgFactory());
	}

	public void SetNumThreads(int n) {
		System.out.println("Running library with " + n + " threads.");
		nThreads = n;
		if (threadPool != null) {
			throw new AssertionError("Error: Unable to re-initialize ThreadPool");
		}
		threadPool = new ThreadPool(nThreads);
	}

	public ArrayList<String> getSupportedTranslations() {
		ArrayList<String> supported = new ArrayList<String>();
		for (BibleFactory f : factories) {
			supported.addAll(f.getSupportedTranslations());
		}
		return supported;
	}

	private Set<String> collections() {
		return collectionTree.keySet();
	}

	private ArrayList<String> collection(String name) {
		return collectionTree.get(name);
	}

	public void generateNormal(List<String> translations, List<String> crosslink, Path outputDirectory) throws IOException {
		System.out.println("Generating non-production");
		this.generateIndexes(crosslink);
		for (String t: translations) {
			TranslationGenerator g = TranslationGenerator.CreateTranslationReadWriteGenerator(this, t, outputDirectory, false);
			threadPool.execute(g);
		}
		threadPool.waitAll();
		this.writeLibraryTOC(outputDirectory, false);
		threadPool.shutdown();
	}

	public void generateProduction(Path outputDirectory) throws IOException {
		System.out.println("Generating production");
		this.generateProductionIndexes();
		List<String> supported = getSupportedTranslations();

		for (String t : supported) {
			TranslationGenerator g = TranslationGenerator.CreateTranslationReadWriteGenerator(this, t, outputDirectory, true);
			threadPool.execute(g);
		}
		threadPool.waitAll();
		System.out.println("Printing table of contents.");
		this.writeLibraryTOC(outputDirectory, true);
		System.out.println("Processing Production Optasia complete.");
		threadPool.shutdown();
	}

	private void generateProductionIndexes() {
		// note: this could be made more efficient for multiple collections
		ThreadPool indexThreadPool = new ThreadPool(8);
		for (String c : collections()) {
			LibraryIndex li = new LibraryIndex();
			productionIndex.put(c, li);
			for (String t : collection(c)) {
				TranslationGenerator g = TranslationGenerator.CreateTranslationIndexGenerator(this, t, li);
				//threadPool.execute(g);
				indexThreadPool.execute(g);
			}
		}
		//threadPool.waitAll();
		indexThreadPool.waitAll();
		indexThreadPool.shutdown();
	}

	// public because of querry()
	public void generateIndexes(List<String> translations) {
		for (String t : translations) {
			TranslationGenerator g = TranslationGenerator.CreateTranslationIndexGenerator(this, t, this.index);
			threadPool.execute(g);
		}
		threadPool.waitAll();
	}


	private void writeLibraryTOC(Path outputDirectory, Path dirExtension, String title, LibraryIndex index) throws IOException {
		// Write a library Table of Contents link.
		HTML html = HTML.create(new Path(outputDirectory, dirExtension, "bible.html"), title);
		html.header(title);
		html.header("Translations", 3);
		html.println("<ul>");
			for (String t : index.getTranslations()) {
				html.printLinkln(new Path(t, "index.html"), t, "<li>", "");
			}
		html.println("</ul>");
		html.end();
	}

	private void writeLibraryTOC(Path outputDirectory, boolean production) throws IOException {
		// Write a library Table of Contents link.
		if (production) {
			for (String c : collections()) {
				writeLibraryTOC(outputDirectory, new Path("production", "collections", c),  collectionTitles.get(c), productionIndex.get(c));
			}
		} else {
			writeLibraryTOC(outputDirectory, new Path("nonproduction"), "Optasia Bible Library", this.index);
		}
	}

	//
	// Querry translations.
	//
	public void querry(ArrayList<String> translations) {
		List<String> supported = new ArrayList<String>(getSupportedTranslations());
		java.util.Collections.sort(supported);
		System.out.println("The following "+Integer.toString(supported.size())+" translations are supported:");
		System.out.println(Util.join(", ", supported));
		System.out.println();
		for (String c : collectionTree.keySet()) {
			System.out.println("The following "+Integer.toString(collectionTree.get(c).size())+" translations are provided by the '"+c+"' Optasia Library");
			List<String> collectionTranslations = new ArrayList<String>(collectionTree.get(c));
			java.util.Collections.sort(collectionTranslations);
			System.out.println(collectionTranslations);
		}
		System.out.println("");
		for (String t : translations) {
			if (supported.contains(t)) {
				System.out.println(t+" is supported.");
			} else {
				System.out.println(t+" is NOT supported.");
			}
			for (String c : collectionTree.keySet()) {
				if (collectionTree.get(c).contains(t)) {
					System.out.println(t+" is in the "+c+" collection.");
				} else {
					System.out.println(t+" is NOT in the "+c+" collection.");
				}
			}
		}
	}

	public void printIndexSummary(PrintStream s) {
		this.index.printSummary(s);
	}



	////////// ConcurrentLibraryIface ////////////
	private final Object libraryLock = new Object();

	@Override // ConcurrentLibraryIface
	public Path getInputDirectory() {
		return this.inputDirectory;
	}

	@Override // ConcurrentLibraryIface
	public Set<String> getCollectionNames() {
		synchronized (libraryLock) {
			Set<String> names = collections();
			return names;
		}
	}

	@Override // ConcurrentLibraryIface
	public ArrayList<String> getCollectionTranslationNames(String collectionName) {
		synchronized (libraryLock) {
			ArrayList<String> names = collection(collectionName);
			return names;
		}
	}

	@Override // ConcurrentLibraryIface
	public LibraryIndexView getNonProductionIndex() {
		synchronized (libraryLock) {
			return this.index;
		}
	}

	@Override // ConcurrentLibraryIface
	public LibraryIndexView getProductionIndex(String collectionName) {
		synchronized (libraryLock) {
			return this.productionIndex.get(collectionName);
		}
	}

	@Override // ConcurrentLibraryIface
	public BibleFactory getBibleFactory(final String shortname) {
		synchronized (libraryLock) {
			boolean found = false;
			BibleFactory factory = null;
			for (BibleFactory f : factories) {
				if (f.getSupportedTranslations().contains(shortname)) {
					assert(!found);
					found = true;
					factory = f;
				}
			}
			if (factory == null) {
				System.out.println("Error: translation "+shortname+" not supported");
				System.exit(1);
			}
			return factory;
		}
	}

}
