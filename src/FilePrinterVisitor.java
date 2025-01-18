import java.io.IOException;

public abstract class FilePrinterVisitor {

	// FilePrinter
	public abstract void visitTranslation(Translation t) throws IOException;
	public abstract void visitBook(Book b) throws IOException;
	public abstract void visitChapter(Chapter c) throws IOException;

	public void visit(Visitable v) throws IOException {
		if (v instanceof Translation) {
			visitTranslation((Translation) v);
		} else if (v instanceof Book) {
			visitBook((Book) v);
		} else if (v instanceof Chapter) {
			visitChapter((Chapter) v);
		} else {
			throw new Error("Unrecognized Visitable type");
		}
	}

	public void visitChildren(Visitable v) throws IOException {
		for (Visitable child : v.getChildren()) {
			visit(child);
		}
	}

}
