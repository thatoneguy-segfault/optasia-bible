import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Set;

class BookComparator implements Comparator<String> {
	protected static LinkedHashMap<String, Integer> bookOrder = new LinkedHashMap<String, Integer>();
	static {
		Set<String> books = BookDescription.bookDescription.keySet();
		int i = 0;
		for (String b : books) {
			bookOrder.put(b, i);
			i++;
		}
	}

	public int compare(String s1, String s2) {
		if (!bookOrder.containsKey(s1))
			throw new AssertionError(s1 + " is not a known bookname");
		if (!bookOrder.containsKey(s2))
			throw new AssertionError(s2 + " is not a known bookname");
		return bookOrder.get(s1) - bookOrder.get(s2);
	}
}
