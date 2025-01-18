/**
 * Write only the chapter title and verse content to an HTML file.
 *
 * No verse links or anything else.
 * Useful for standalone books or entire sections of the Bible.
 */
public class HTMLContentSearchWriter extends HTMLWriter {
	public HTMLContentSearchWriter(HTML out, boolean doXlink) {super(out, doXlink);}
	public void visitBook(Book b) {
		out.header(b.title);

		visitChildren(b);
	}

	public void visitChapter(Chapter c) {
		out.header(c.book.title + " " + c.number, 3);
		visitChildren(c);
	}

	public void visitVerse(Verse v) {
		if (v.endnumber > 0) {
			int s = v.startnumber;
			int e = v.endnumber;
			out.print(String.format("<sup>%d-%d</sup> ", s,e));
		} else {
			int s = v.startnumber;
			out.print(String.format("<sup>%d</sup> ", s));
		}
		out.println(v.toString());
	}
	public void visitVerseContent(VerseContent v) { out.println(v); visitChildren(v); }
	public void visitHeading(Heading h) { out.println(h); visitChildren(h); }
}
