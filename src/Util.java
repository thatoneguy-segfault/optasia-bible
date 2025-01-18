import java.util.ArrayList;
import java.util.Collection;

public abstract class Util
{
	public static String join(String sep, Collection<String> strings) {
		StringBuilder b = new StringBuilder();
		int i = 0;
		for (String s : strings) {
			if (i != 0) {
				b.append(sep);
			}
			b.append(s);
			i++;
		}
		return b.toString();
	}

	public static String join(String sep, ArrayList<String> strings) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < strings.size(); i++) {
			if (i != 0) {
				b.append(sep);
			}
			b.append(strings.get(i));
		}
		return b.toString();
	}

	public static String join(String sep, String[] strings) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			if (i != 0) {
				b.append(sep);
			}
			b.append(strings[i]);
		}
		return b.toString();
	}
}
