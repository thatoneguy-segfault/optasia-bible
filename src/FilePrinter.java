import java.util.Collection;
import java.io.IOException;

/**
 * Create directory structure, table of contents, etc.
 */
public class FilePrinter extends FilePrinterVisitor {
	Path outputDirectory; // = new Path("/tmp");
	Path translationOutputDirectory; // = new Path("/tmp");
	LibraryIndexView index;

	public FilePrinter(Path outputDirectory, LibraryIndexView index) {
		this.outputDirectory = outputDirectory;
		this.index = index;
	}

	protected static Path getChapterFileName(Chapter c) {
		return new Path("ch" + Integer.toString(c.number) + ".html");
	}

	protected static Path getChapterFileName(int i) {
		return new Path("ch" + Integer.toString(i) + ".html");
	}



	protected void writeAbout(Translation t, Path filename) throws IOException {
		if (t.getAbout() == null) {
			return;
		}
		HTML html = HTML.create(new Path(translationOutputDirectory, filename), "About " + t.fullname);
		html.header("About "+t.fullname);
		html.println(t.getAbout());
		html.header("Copyright", 2);
		html.println(t.getCopyright());
		html.header("Navigation Links", 3);
		html.println("<p>");
		if (index != null) {
			html.printLinkln(new Path("..", "bible.html"), "Library");
		}
		html.printLinkln(new Path("index.html"), "Table of Contents");
		html.println("</p>");
		html.end();
	}

	protected void writeCopyright(Translation t, Path filename) throws IOException {
		HTML html = HTML.create(new Path(translationOutputDirectory, filename), "Copyright Information");
		html.header("Copyright Information");
		html.println(t.getCopyright());
		html.header("Navigation Links", 3);
		html.println("<p>");
		if (index != null) {
			html.printLinkln(new Path("..", "bible.html"), "Library");
		}
		html.printLinkln(new Path("index.html"), "Table of Contents");
		html.println("</p>");
		html.end();
	}

	protected interface BibleLinkFilter
	{
		public boolean printLink(String other);
	}

	protected void p_writeBibleLinks(HTML html, String shortname, Path relativePath, Path filename, BibleLinkFilter printLinkClosure) {
		if (index != null) {
			html.header("Optasia Bible Translations", 3);
			html.println("<p>");
			for (String other : index.getTranslations()) {
				if (printLinkClosure.printLink(other)) {
					Path link = new Path(relativePath, other, filename.toString().replaceAll(shortname, other));
					html.printLinkln(link, other);
				} else {
					html.println(other);
				}
			}
			html.println("</p>");
		}
	}

	/** Write links to the other translations for same location.
	 * @param filename all translation shortnames are regex replaced.
	 */
	protected void writeBibleLinks(HTML html, Path relativePath, Path filename, final Translation current) {
		p_writeBibleLinks(html, current.shortname, relativePath,
			filename,
			new BibleLinkFilter() {
				public boolean printLink(String other) {
					return !other.equals(current.shortname);
				}
			});
	}

	protected void writeBibleLinks(HTML html, Path relativePath, Path filename, final Translation current, final BookCategory cat) {
		p_writeBibleLinks(html, current.shortname, relativePath,
			filename,
			new BibleLinkFilter() {
				public boolean printLink(String other) {
					return (!other.equals(current.shortname)) && index.supports(other, cat);
				}
			});
	}

	protected void writeBibleLinks(HTML html, Path relativePath, Path filename, final Book b) {
		p_writeBibleLinks(html, b.translation.shortname, relativePath,
			filename,
			new BibleLinkFilter() {
				public boolean printLink(String other) {
					return !(other.equals(b.translation.shortname))
						&& index.supports(other, b.title);
				}
			});
	}

	protected void writeBibleLinks(HTML html, Path relativePath, Path filename, final Chapter c) {
		p_writeBibleLinks(html, c.book.translation.shortname,
			relativePath, filename,
			new BibleLinkFilter() {
				public boolean printLink(String other) {
					return !(other.equals(c.book.translation.shortname))
						&& index.supports(other, c.book.title, c.number);
				}
			});
	}

	protected void writeTableOfContents(Translation t, Path filename) throws IOException {
		HTML toc = HTML.create(new Path(translationOutputDirectory, filename), "Table of Contents: " + t.getTitlename());
		toc.header(t.fullname);
		toc.header("Books", 3);
		toc.println("<ul>");
			for (Book book : t.books.values()) {
				toc.printLinkln(new Path(book.title, "index.html"), book.title, "<li>", "");
			}
		toc.println("</ul>");

		toc.header("Search Categories", 3);
		toc.println("<ul>");
			for (BookCategory category : BookCategory.values()) {
				toc.printLinkln(new Path(category.toString() + ".html"), category.toString(), "<li>", "");
			}
		toc.println("</ul>");
		toc.header("Navigation Links", 3);
		toc.println("<p>");
		if (index != null) {
			toc.printLinkln(new Path("..", "bible.html"), "Library");
		}
		toc.printLinkln(new Path("copyright.html"), "Copyright");
		if (t.getAbout() != null) {
			toc.printLinkln(new Path("about.html"), "About");
		}
		toc.println("</p>");
		writeBibleLinks(toc, new Path(".."), filename, t);
		toc.end();
	}

	// A helper function for printing the Previous or Next link.
	protected void prevnextLinks(HTML html, Book l, Path path, String name) {
		if (l != null) {
			html.printLinkln(path, name);
		} else {
			html.println(name);
		}
	}

	protected void writeBookTableOfContents(Book prev, Book b, Book next, Path filename) throws IOException {
		HTML toc = HTML.create(new Path(translationOutputDirectory, b.title, filename),
				b.title + " Table of Contents: " + b.translation.getTitlename());
		toc.header(b.title);
		toc.header("Chapters", 3);
		for (int chapterNumber : b.chapters.keySet()) {
			toc.printLinkln(getChapterFileName(chapterNumber), Integer.toString(chapterNumber));
		}

		toc.header("Search Categories", 3);
		toc.println("<p>");
		toc.printLinkln(new Path(b.title+"_text.html"), "Book Text");
		toc.println("</p>");
		toc.header("Navigation Links", 3);
		toc.println("<p>");
		toc.printLinkln(new Path("..", b.translation.shortname + ".html"), "Table of Contents");
		prevnextLinks(toc, prev, new Path("..", (prev == null ? "" : prev.title), "index.html"), "Previous");
		prevnextLinks(toc, next, new Path("..", (next == null ? "" : next.title), "index.html"), "Next");
		if (index != null) {
			toc.printLinkln(new Path("..", "..", "bible.html"), "Library");
		}
		toc.printLinkln(new Path("..", "copyright.html"), "Copyright");
		if (b.getTranslation().getAbout() != null) {
			toc.printLinkln(new Path("..", "about.html"), "About");
		}
		toc.println("</p>");

		writeBibleLinks(toc, new Path("..", ".."), new Path(b.title, filename), b);

		toc.end();
	}

	protected void writeChapter(Chapter c, Path filename) throws IOException {
		HTML ch = HTML.create(new Path(translationOutputDirectory, c.book.title, filename),
			c.book.title + " " + c.number + ": " + c.book.translation.getTitlename());
		HTMLContentWriter w = new HTMLContentWriter(ch, index != null);
		w.visitChapter(c);
		writeBibleLinks(ch, new Path("..", ".."), new Path(c.book.title, filename), c);
		ch.end();
	}

	protected void writeSearchableBook(Book prev, Book b, Book next, Path filename) throws IOException {
		// Print out a searchable file for each book.
		HTML html = HTML.create(new Path(translationOutputDirectory, b.title, filename),
			b.title + ": " + b.translation.getTitlename());
		HTMLContentSearchWriter w = new HTMLContentSearchWriter(html, index != null);
		w.visitBook(b);

		html.header("Navigation Links", 3);
		html.println("<p>");
		html.printLinkln(new Path("..", b.translation.shortname + ".html"), "Table of Contents");
		html.printLinkln(new Path(b.title + ".html"), "Book");
		String prevtitle = (prev == null ? "" : prev.title);
		String nexttitle = (next == null ? "" : next.title);
		prevnextLinks(html, prev, new Path("..", prevtitle, prevtitle+ "_text.html"), "Previous");
		prevnextLinks(html, next, new Path("..", nexttitle, nexttitle+ "_text.html"), "Next");
		if (index != null) {
			html.printLinkln(new Path("..", "..", "bible.html"), "Library");
		}
		html.printLinkln(new Path("..", "copyright.html"), "Copyright");
		if (b.getTranslation().getAbout() != null) {
			html.printLinkln(new Path("..", "about.html"), "About");
		}
		html.println("</p>");
		writeBibleLinks(html, new Path("..", ".."), new Path(b.title, filename), b);
		html.end();
	}

	public void visitTranslation(Translation t) throws IOException {
		translationOutputDirectory = new Path(outputDirectory, t.shortname);

		writeTableOfContents(t, new Path("index.html"));
		writeTableOfContents(t, new Path(t.shortname + ".html"));
		writeCopyright(t, new Path("copyright.html"));
		writeAbout(t, new Path("about.html"));

		Collection<Book> booksCollection = t.books.values();
		Book[] bookArray = booksCollection.toArray(new Book[booksCollection.size()]);
		for (int i = 0; i < bookArray.length; i++) {
			Book prev = (i != 0 ? bookArray[i-1] : null);
			Book next = (i == bookArray.length-1 ? null : bookArray[i+1]);
			writeBookTableOfContents(prev, bookArray[i], next, new Path("index.html"));
			writeBookTableOfContents(prev, bookArray[i], next,
					new Path(bookArray[i].title + ".html"));
			writeSearchableBook(prev, bookArray[i], next,
					new Path(bookArray[i].title + "_text.html"));
		}

		// write out each category.
		for (BookCategory category : BookCategory.values()) {
			HTML html = HTML.create(new Path(translationOutputDirectory, category.toString() + ".html"),
				category.toString() + ": " + t.getTitlename());
			HTMLContentSearchWriter w = new HTMLContentSearchWriter(html, index != null);
			html.header(category.toString());
			html.println("<p>The following books are included in this search category:");
			html.println("<ul>");
			Collection<Book> categoryBooks = t.getBooks(category);
			for (Book book : categoryBooks) {
				html.println("<li> " + book.title);
			}
			html.println("</ul>");
			html.println("</p>");
			for (Book book : categoryBooks) {
				w.visitBook(book);
			}

			html.header("Navigation Links", 3);
			html.println("<p>");
			html.printLinkln(new Path(t.shortname + ".html"), "Table of Contents");
			if (index != null) {
				html.printLinkln(new Path("..", "bible.html"), "Library");
			}
			html.printLinkln(new Path("copyright.html"), "Copyright");
			if (t.getAbout() != null) {
				html.printLinkln(new Path("about.html"), "About");
			}
			html.println("</p>");
			writeBibleLinks(html, new Path(".."), new Path(category.toString() + ".html"), t, category);
			html.end();

		}

		visitChildren(t);
	}

	public void visitBook(Book b) throws IOException {
		visitChildren(b);
	}

	public void visitChapter(Chapter c) throws IOException {
		writeChapter(c, getChapterFileName(c));
		//Do not call visitChildren()
	}

}
