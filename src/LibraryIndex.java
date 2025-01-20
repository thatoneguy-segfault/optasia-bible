import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.io.PrintStream;


public class LibraryIndex implements LibraryIndexView {
	/** An index of the translations, including what books and chapters
	they provide. **/

	private Index index = new Index();

	private Object indexLock = new Object();

	private class ChapterList extends ArrayList<Integer> {
		// Define type
	}

	private class BookMap extends LinkedHashMap<String, ChapterList > {
		// Define type
	}

	private class Index extends LinkedHashMap<String, BookMap> {
		// Define type
	}

	public ArrayList<String> getTranslations() {
		synchronized (indexLock) {
			ArrayList<String> translations = new ArrayList<String>();
			translations.addAll(index.keySet());
			Collections.sort(translations);
			return translations;
		}
	}


	public void addSupport(String translation, String book, int chapter) {
		synchronized (indexLock) {
			BookMap bmap = this.index.get(translation);
			if (null == bmap) {
				bmap = new BookMap();
				index.put(translation, bmap);
			}
			ChapterList ca = bmap.get(book);
			if (null == ca) {
				ca = new ChapterList();
				bmap.put(book, ca);
			}
			ca.add(chapter);
		}
	}

	public void addSupport(Chapter c) {
		addSupport(c.book.translation.shortname, c.book.title, c.number);
	}

	public boolean supports(String translation) {
		synchronized (indexLock) {
			return supportsNB(translation);
		}
	}


	private boolean supportsNB(String translation) {
		return (null != this.index.get(translation));
	}

	public boolean supports(String translation, String book) {
		synchronized (indexLock) {
			return supportsNB(translation, book);
		}
	}

	private boolean supportsNB(String translation, String book) {
		return supportsNB(translation)
			&& (null != this.index.get(translation).get(book));
	}

	public boolean supports(String translation, String book, int chapter) {
		synchronized (indexLock) {
			return supportsNB(translation, book, chapter);
		}
	}

	private boolean supportsNB(String translation, String book, int chapter) {

                
                // We shouldn't be asking about translations
                // if we haven't even indexed them.
                //assert this.index.containsKey(translation);

		return supportsNB(translation, book)
			&& this.index.get(translation).get(book).contains(chapter);
	}

	public boolean supports(Translation translation, BookCategory cat) {
		synchronized (indexLock) {
			return supportsNB(translation.shortname, cat);
		}
	}

	public boolean supports(String translation, BookCategory cat) {
		synchronized (indexLock) {
			return supportsNB(translation, cat);
		}
	}

	private boolean supportsNB(String translation, BookCategory cat) {
		for (String b : BookDescription.books(cat)) {
			if (supportsNB(translation,b)) {
				return true;
			}
		}
		return false;
	}

	public boolean supports(Translation t) {
		synchronized (indexLock) {
			return supportsNB(t);
		}
	}

	private boolean supportsNB(Translation t) {
		return supportsNB(t.shortname);
	}

	public boolean supports(Book b) {
		synchronized (indexLock) {
			return supportsNB(b);
		}
	}

	private boolean supportsNB(Book b) {
		return supportsNB(b.translation.shortname, b.title);
	}

	public boolean supports(Chapter c) {
		synchronized (indexLock) {
			return supportsNB(c);
		}
	}

	private boolean supportsNB(Chapter c) {
		return supportsNB(c.book.translation.shortname, c.book.title, c.number);
	}

	public void printIndex(PrintStream s) {
		synchronized (indexLock) {
			for (Map.Entry<String, BookMap> indexEntry : index.entrySet()) {
				s.println(indexEntry.getKey()); // Translation
				for (Map.Entry<String, ChapterList>  bookMapEntry : indexEntry.getValue().entrySet()) {
					s.println("\t" + bookMapEntry.getKey()); // Book
					for (int chapter : bookMapEntry.getValue()) {
						s.println("\t\t" + Integer.toString(chapter));
					}
				}
			}
		}
	}

	//
	// It wouldn't make sense to skip chapters.
	//
	public void verify() {
		for (Map.Entry<String, BookMap> indexEntry : index.entrySet()) {
			String t = indexEntry.getKey();
			BookMap bmap = indexEntry.getValue();
			for (Map.Entry<String, ChapterList> bookMapEntry : bmap.entrySet()) {
				String b = bookMapEntry.getKey();
				ChapterList ca = bookMapEntry.getValue();
				Collections.sort(ca);
				for (int i = 0; i < ca.size() - 1; i++) {
					if (ca.get(i) + 1 != ca.get(i+1)) {
						System.out.println("It appears that a chapter was skipped in "+t+" "+b+" between '"+ca.get(i)+"' and '"+ca.get(i+1)+"'");
					}
					assert (ca.get(i) + 1 == ca.get(i+1));
				}
			}
		}
	}


	public void printSummary(PrintStream s) {
		for (Map.Entry<String, BookMap> indexEntry : index.entrySet()) {
                        int nChapters = 0;
                        for (Map.Entry<String, ChapterList> chapterEntry: indexEntry.getValue().entrySet()) {
                            nChapters += chapterEntry.getValue().size();
                        }

			s.println(indexEntry.getKey() + " " + indexEntry.getValue().size() + " books, " + nChapters + " chapters");
		}
		verify();
	}

	public static void unitTest() {
		LibraryIndex i = new LibraryIndex();

		i.addSupport("NIV", "Genesis", 1);
		i.addSupport("NIV", "Genesis", 2);
		i.addSupport("NIV", "Genesis", 3);
		i.addSupport("NIV", "Exodus", 1);
		i.addSupport("TNIV", "Genesis", 1);
		assert(i.supports("NIV", "Genesis", 1));
		assert(i.supports("NIV", "Genesis", 2));
		assert(i.supports("TNIV", "Genesis", 1));
		assert(i.supports("NIV", "Exodus", 1));
		assert(!i.supports("NIV", "Genesis", 4));
		assert(!i.supports("NIV", "Mark", 1));
		assert(!i.supports("TNIV", "Mark", 1));
		try {
			i.supports("ABC", "Genesis", 1);
			throw new Error("Unit Test Fail");
		} catch (AssertionError a) {
		}
		assert(i.supports("NIV", BookCategory.OLD_TESTAMENT));
		assert(!i.supports("NIV", BookCategory.NEW_TESTAMENT));
		//i.printIndex(System.out)
	}
}
