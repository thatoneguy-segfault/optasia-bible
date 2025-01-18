import java.util.TreeMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

class Translation implements Visitable {
	public String fullname = null;
	public String shortname = null;
	public ConcurrentLibraryIface library = null;
	public String about = "";
	public String copyright = null; // Initialize to null to require the copyright

	public TreeMap<String, Book> books = new TreeMap<String, Book>(new BookComparator());

	private Translation() {
	}

	public Translation (ConcurrentLibraryIface library) {
		this.library = library;
	}

	public Collection<Visitable> getChildren() {
		LinkedList<Visitable> r = new LinkedList<Visitable>();
		for (Book b : books.values()) {
			r.add(b);
		}
		return r;
	}

	void setFullname(String s) {
		if (fullname == null) {
			fullname = s;
		} else {
			assert (fullname.equals(s)); // "Cannot change fullname";
		}
	}

	/**
	 * Set the shortname, e.g. "NIV"
	 * If already set, make sure it matches what we've been using.
	 */
	void setShortname(String s) {
		if (shortname == null) {
			shortname = s;
		} else {
			assert (shortname.equals(s)); // "Cannot change shortname";
		}
	}

	/**
	 * The name to display in HTML titles.
	 */
	String getTitlename() {
		return fullname;
	}

	/**
	 * Get book by name.
	 * If not found, creates new book with the name.
	 */
	Book getBook(String name) {
		if (books.containsKey(name)) {
			Book b = books.get(name);
			return b;
		} else {
			Book b = new Book(this, name);
			books.put(name, b);
			return b;
		}
	}

	/**
	 * Get all books matching a category.
	 */
	List<Book> getBooks(BookCategory cat) {
		ArrayList<Book> bookMatch = new ArrayList<Book>();
		for (Book b : books.values()) {
			if (BookDescription.bookDescription.get(b.title).contains(cat)) {
				bookMatch.add(b);
			}
		}
		return bookMatch;
	}

	String summarize() {
		int numBooks = books.size();
		int numChapters = 0;
		for (Book b : books.values()) {
			numChapters += b.chapters.size();
		}
		return this.shortname+": "+numBooks+" books, "+numChapters+" chapters";
	}

	void verify() {
		assert (this.copyright != null);
		for (Book b : books.values()) {
			for (Chapter c : b.chapters.values()) {
				int prev = 0;
				ArrayList<Verse> v = c.getVerseList();
				for (int i=0; i < v.size(); i++) {
					if (!((v.get(i).endnumber == 0) || (v.get(i).endnumber > v.get(i).startnumber))) {
						System.out.println("WARNING: " + c.fullString() +"  Error: prev="+Integer.toString(prev)+", startnumber="+Integer.toString(v.get(i).startnumber)+", endnumber="+Integer.toString(v.get(i).endnumber));
					}
					if (v.get(i).startnumber != prev+1) {
						System.out.println(c.fullString() + " Warning: verse "+prev+" followed by verse "+v.get(i).startnumber);
					}
					prev = v.get(i).endnumber != 0 ? v.get(i).endnumber : v.get(i).startnumber;
				}
			}
		}
	}

	public String getAbout() {
		return this.about;
	}
	
	public void setAbout(String about) {
		this.about = about;
	}

	public String getCopyright() {
		return "<p>Optasia Bible software copyright 2012, Optasia Ministry</p>" +
			"<p>Notice:  This nondramatic literary work is provided to you by Optasia Ministry, Inc., a nonprofit organization incorporated under the laws of the State of Iowa for the purpose of providing copies of previously published, nondramatic literary work in specialized formats exclusively for use by persons who are blind as allowed by section 121, chapter 1 of title 17 of the United States Code, also known as the Chafee Amendment.</p><p>Any further reproduction or distribution in a format other than a specialized format is an infringement of copyright.  Beneficiaries of Optasia Ministry, Inc., services are not authorized to further distribute this material to any other entity or person.</p>" +
			this.copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	//public void accept(FilePrinterVisitor v) {
		//v.visit(this);
	//}
}
