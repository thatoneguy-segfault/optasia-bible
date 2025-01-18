import java.util.ArrayList;

/*
 * An unchangeable view into a LibraryIndex.  Useful for multithreaded
 * programs.
 */
public interface LibraryIndexView {
	public ArrayList<String> getTranslations();
	public boolean supports(String translation);
	public boolean supports(String translation, String book);
	public boolean supports(String translation, String book, int chapter);
	public boolean supports(Translation translation, BookCategory cat);
	public boolean supports(String translation, BookCategory cat);
	public boolean supports(Translation t);
	public boolean supports(Book b);
	public boolean supports(Chapter c);
}
