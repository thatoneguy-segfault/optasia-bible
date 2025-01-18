import java.util.Collection;
import java.util.LinkedList;

/**
  * HtmlVerse: contain arbitrary Html that may need to appear in the
  * middle of a verse.  Or something.
  */
public class HtmlVerse extends ChapterEntry implements VerseContent {
	protected String text;
	protected Chapter chapter;
	public HtmlVerse(Chapter c, String text) {this.chapter = c; this.text = text;}
	public String toString() { return text; }
	public Collection<Visitable> getChildren() {return new LinkedList<Visitable>();}

}
