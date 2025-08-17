import java.io.PrintStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

public class HTML {
	private PrintStream stream = null;

	private HTML() {}

	public static HTML create(Path filename, String title) throws IOException {
		File file = new File(safe(filename.toString()));
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}

		FileOutputStream stream = new FileOutputStream(file);
		return create(stream, title);
	}

	public static HTML create(OutputStream stream, String title) throws IOException {
		HTML h = new HTML();
		h.stream = new PrintStream(stream, true, "UTF-8");
		h.println("<!DOCTYPE html>");
		h.printlns("<html>", "<head>", "<title>", title, "</title>", "</head>", "<body>");
		return h;
	}

	public static String safe(String s) {
		s = s.replaceAll(" ", "_");
		s = s.replaceAll("'", "_");
		s = s.replaceAll("[-()]", "");
		return s;
	}

	public void header(String h) {
		header (h, 1);
	}

	public void header(String h, int i) {
		this.print("<h"+Integer.toString(i)+">");
		this.print(h);
		this.println("</h"+Integer.toString(i)+">");
	}

	public void print(Object o) { stream.print(o); }
	public void println(Object o) { stream.println(o); }

	public void print(Object... objects) {
		for (Object o : objects) {
			this.print(o);
		}
	}
	public void printlns(Object... objects) {
		for (Object o : objects) {
			this.println(o);
		}
	}
	public void println(Object... objects) {
		for (Object o : objects) {
			this.print(o);
		}
		stream.println();
	}

	public void printSafe(String s) {
		this.print(safe(s));
	}

	public void printLinkln(Path target, String title) {
		printLinkln(target, title, "", "");
	}

	public void printLinkln(Path target, String title, String prefix, String postfix) {
		this.print(prefix, "<a href=\"");
		this.printSafe(target.toURLString());
		this.println("\">", title, "</a>", postfix);
	}

	public void printInternalLinkln(String target, String title, String prefix, String postfix) {
		this.print(prefix, "<a href=\"");
		this.printSafe(target);
		this.println("\">", title, "</a>", postfix);
	}

	public void printLink(Path target, String title) {
		printLink(target, title, "", "");
	}

	public void printLink(Path target, String title, String prefix, String postfix) {
		this.print(prefix, "<a href=\"");
		this.printSafe(target.toURLString());
		this.print("\">", title, "</a>", postfix);
	}

	public void printSafe(String... objects) {
		for (String o : objects) {
			this.printSafe(o);
		}
	}

	public void end() {
		this.println("</body>");
		this.println("</html>");
	}

	public void close() {
		stream.close();
	}
}
