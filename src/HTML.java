import java.io.PrintStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class HTML {
	private PrintStream stream = null;
        private Pattern openHeader = Pattern.compile("<h(\\d)>");
        private Pattern closeHeader = Pattern.compile("</h(\\d)>");

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

        public String finalCleanup(String s) {
            // Remove empty paragraph markings before 
            s = s.replaceAll("<p */>", "");

            // Add close paragraph marking before all "open header" <h5>
            Matcher openHeaderMatcher = openHeader.matcher(s);
            StringBuilder sb = new StringBuilder();
            while (openHeaderMatcher.find()) {
                openHeaderMatcher.appendReplacement(sb, "</p><h" + openHeaderMatcher.group(1) + ">");
            }
            // append the remaining part of the input string after the last match
            openHeaderMatcher.appendTail(sb);
            s = sb.toString();

            // Add an open paragraph marking after all "close header" </h5>
            Matcher closeHeaderMatcher = closeHeader.matcher(s);
            sb.setLength(0); // reset string buffer
            while (closeHeaderMatcher.find()) {
                closeHeaderMatcher.appendReplacement(sb, "</h" + closeHeaderMatcher.group(1) + "><p>");
            }
            // append the remaining part of the input string after the last match
            closeHeaderMatcher.appendTail(sb);

            return sb.toString();
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

	public void print(Object o) {
            if (null != o) {
                this.print(o.toString());
            }
        }

	public void println(Object o) {
            if (null != o) {
                this.println(o.toString());
            }
        }

        public void print(String s) {
            if (null != s) {
                stream.print(this.finalCleanup(s));
            }
        }

        public void println(String s) {
            if (null != s) {
                stream.println(this.finalCleanup(s));
            }
        }

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
