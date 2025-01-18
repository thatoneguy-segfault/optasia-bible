import java.util.Collection;
import java.util.LinkedList;

public class Heading extends ChapterEntry implements VerseContent {
	protected int level;
	protected String heading;
	protected Chapter chapter;

	public Heading(Chapter c, int level, String heading) {
		this.chapter = c;
		this.level=level;
		this.heading=heading;
	}
	public String toString() {
		return String.format("<h%d>%s</h%d>", level, heading, level);
	}

	public Collection<Visitable> getChildren() {return new LinkedList<Visitable>();}
}
