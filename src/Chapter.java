import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedList;

public class Chapter implements Visitable {
	public Book book = null;
	public int number;
	public String filename = null;

	public ArrayList<ChapterEntry> data = new ArrayList<ChapterEntry>();

	public Chapter(Book b, int chapter, String filename) {
		book = b;
		number = chapter;
		this.filename = filename;
	}

	public Collection<Visitable> getChildren() {
		LinkedList<Visitable> r = new LinkedList<Visitable>();
		for (ChapterEntry d : data) {
			r.add(d);
		}
		return r;
	}

	public void add(ChapterEntry entry) {
		this.data.add(entry);
	}

	public void addHtml(String text) {
		 HtmlVerse html = new HtmlVerse(this, text);
		 add(html);
	}

	public void addVerse(int verseNumber, String text) {
		addVerse(verseNumber, 0, text);
	}

	protected Pair<Integer, Integer> lastVerseNumber() {
		Verse v = null;
		for (ChapterEntry e : data) {
			if (e instanceof Verse) {
				v = (Verse)e;
			}
		}
		if (v == null) {
			return new Pair<Integer,Integer>(-1, -1);
		}
		return new Pair<Integer, Integer>(v.startnumber, v.endnumber);
	}

	public void addVerse(int verseStart, int verseEnd, String text) {
		Pair<Integer, Integer> tmpVerseNum = new Pair<Integer, Integer>(verseStart, verseEnd);
		if (tmpVerseNum.equals(lastVerseNumber())) {
			addHtml(text); // don't add another verse with the same #
		} else {
			this.data.add(new Verse(this, verseStart, verseEnd, text));
		}
	}

	//
	// Get all of the verses in this chapter.
	//
	public ArrayList<Verse> getVerseList() {
		ArrayList<Verse> v = new ArrayList<Verse>();
		for (ChapterEntry entry : data) {
			if (entry instanceof Verse) {
				v.add((Verse) entry);
			}
		}
		return v;
	}

	///
	// Return a string telling full location
	//
	String fullString() {
		return this.book.translation.shortname+" "+this.book.title+" "+this.number;
	}
}
