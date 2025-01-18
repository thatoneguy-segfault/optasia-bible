/**
 * Write the content to an HTML file.
 */
class HTMLContentWriter extends HTMLWriter {
	public HTMLContentWriter(HTML out, boolean doXlink) {super(out, doXlink);}

	protected void prevnextLinks(Chapter c, int chapterNumber, String name) {
                if (c.book.chapters.containsKey(chapterNumber)) {
                        out.printLinkln(FilePrinter.getChapterFileName(chapterNumber), name);
                } else {
                        out.println(name);
                }
        }

	public void visitChapter(Chapter c) {
		visitChildren(c);

		out.header("Verse Links", 3);
		out.println("<p>");
		for (Visitable child : c.getChildren()) {
			if ( child instanceof Verse) {
				printVerseLinks((Verse)child);
			}
		}
		out.println("</p>");

		out.header("Navigation Links", 3);
		out.println("<p>");
		out.printLinkln(new Path("..", "index.html"), "Table of Contents");
		out.printLinkln(new Path("index.html"), "Book");
		prevnextLinks(c, c.number - 1, "Previous");
		prevnextLinks(c, c.number + 1, "Next");
		if (doXlink) {
			out.printLinkln(new Path("..", "..", "bible.html"), "Library");
		}
		out.printLinkln(new Path("..", "copyright.html"), "Copyright");
		out.println("</p>");
	}

	/**
	 * Print a link to every verse.
	 */
	public void printVerseLinks(Verse v) {
		for (int i = v.startnumber; i <= (v.endnumber > 0 ? v.endnumber : v.startnumber); i++) {
			out.printInternalLinkln("#verse" + Integer.toString(i), Integer.toString(i), "", "");
		}
	}

	public void visitVerse(Verse v) {
		if (v.endnumber > 0) {
			int s = v.startnumber;
			int e = v.endnumber;
			out.print(String.format("<sup><a name=\"verse%d_%d\">%d-%d</a></sup> ",
				s,e,s,e));
		} else {
			int s = v.startnumber;
			out.print(String.format("<sup><a name=\"verse%d\">%d</a></sup> ", s,s));
		}
		out.println(v);
	}

	public void visitVerseContent(VerseContent v) { out.println(v); visitChildren(v); }
	public void visitHeading(Heading h) { out.println(h); visitChildren(h); }
}
