import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class Verse extends ChapterEntry implements Visitable {
	public int startnumber;
	public int endnumber; // == 0 if not multi-numbered
	public Chapter chapter;
	public String text;

	public Collection<Visitable> getChildren() {
		return new LinkedList<Visitable>();
	}

	public Verse(Chapter c, int startnumber, int endnumber, String text) {
		assert text != null; // "${c.book.translation.shortname} ${c.book.title} ${c.number}:$startnumber"

		// Odd bug in MSG
		if ( startnumber == 26
			&& c.number == 35
			&& c.book.translation.shortname.equals("MSG")
			&& c.book.title.equals("2 Chronicles")
			&& endnumber == 1) {
			endnumber = 0;
		}
		chapter = c;
		//TODO: Allow fields.
		this.startnumber = startnumber;
		this.endnumber = endnumber;

		if (text.matches("^.*<(?:span|sup).*$")) {
			throw new AssertionError("Unexpected verse text, span|sup: " + this.fullString() + ": '" + text + "'");
		}
		assert !(text.matches(".*<div class?=\"footnotes\".*"));
		assert !(text.matches(".*<strong>Footnotes:</strong>.*"));
		this.text = text;
	}

	public String toString() {
		return text;
	}

	public String fullString() {
		return chapter.fullString() + ":" + Integer.toString(startnumber);
	}

}
