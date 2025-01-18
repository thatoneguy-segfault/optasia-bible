public abstract class HTMLContentWriterVisitor {

	// HTMLContentWriter
	public abstract void visitChapter(Chapter c);
	public abstract void visitVerse(Verse v);
	public abstract void visitVerseContent(VerseContent c);
	public abstract void visitHeading(Heading c);

	public void visit(Visitable v) {
		if (v instanceof Chapter) {
			visitChapter((Chapter) v);
		} else if (v instanceof Verse) {
			visitVerse((Verse) v);
		} else if (v instanceof VerseContent) {
			visitVerseContent((VerseContent) v);
		} else if (v instanceof Heading) {
			visitHeading((Heading) v);
		} else {
			throw new Error("Unrecognized Visitable type");
		}
	}

	public void visitChildren(Visitable v) {
		for (Visitable child : v.getChildren()) {
			visit(child);
		}
	}

}
