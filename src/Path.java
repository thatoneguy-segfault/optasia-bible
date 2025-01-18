import java.io.File;
import java.util.ArrayList;

/**
 * Make system independant path construction easier
 */
public class Path {

	ArrayList<String> myPath;

	public Path() {
		myPath = new ArrayList<String>();
	}
	
	public Path(Object... names) {
		myPath = new ArrayList<String>();
		append(names);
	}

	public File toFile() {
		return new File(toString());
	}

	public void append(Object... names) {
		for (Object name : names) {
			if (name instanceof String) {
				myPath.add((String)name);
			} else if (name instanceof Path) {
				myPath.addAll(((Path) name).myPath);
			} else {
				throw new IllegalArgumentException("Unexpected type " + name.getClass().getSimpleName());
			}
				
		}
	}

	public String toString() {
		return toString(File.separator);
	}

	public String toURLString(){
		return toString("/");
	}

	public String toString(String separator) {
		StringBuilder fullPath = new StringBuilder();
		for (int i = 0; i < myPath.size(); i++) {
			if (i != 0) {
				fullPath.append(separator);
			}
			fullPath.append(myPath.get(i));
		}
		return fullPath.toString();
	}

	public static String path(Object... names) {
		Path p = new Path(names);
		return p.toString();
	}
}
