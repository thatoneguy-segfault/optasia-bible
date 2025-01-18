import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

class Book implements Visitable {
	String title = null;
	Translation translation = null;

	TreeMap<Integer, Chapter> chapters = new TreeMap<Integer, Chapter>();
	// treemap allows the chapters to be created out of order without knowing the number of
	// chapters in advance.

	Book(Translation t, String title) {
		translation = t;
		setTitle(title);
	}

	public Translation getTranslation()
	{
		return translation;
	}

	public Collection<Visitable> getChildren() {
		LinkedList<Visitable> r = new LinkedList<Visitable>();
		for (Chapter c : chapters.values()) {
			r.add(c);
		}
		return r;
	}

	/**
	 * Set the title of this book.  Immutable.
	 */
	void setTitle(String s) {
		if (title == null) {
			title = s;
		} else {
			assert title == s; // cannot change title
		}
	}

	Chapter getChapter(int chapterNumber) {
		return getChapter(chapterNumber, "");
	}

	Chapter getChapter(int chapterNumber, String filename) {
		if (chapters.get(chapterNumber) == null) {
			return createChapter(chapterNumber, filename);
		} else {
			return chapters.get(chapterNumber);
		}
	}

	Chapter createChapter(int chapterNumber) {
		return createChapter(chapterNumber, "");
	}
	
	Chapter createChapter(int chapterNumber, String filename) {
		if (chapters.get(chapterNumber) != null) {
			throw new AssertionError("Attempting to parse second copy of "+translation.shortname+" "+title+" "+chapterNumber+", previously read from "+chapters.get(chapterNumber).filename);
		}
		chapters.put(chapterNumber, new Chapter(this, chapterNumber, filename));
		return chapters.get(chapterNumber);
	}

	//public void accept(DefaultVisitor v) {
		//v.visit(this);
	//}
}
