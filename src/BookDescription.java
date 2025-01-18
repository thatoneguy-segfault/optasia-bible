import java.util.LinkedHashMap;
import java.util.EnumSet;
import java.util.HashSet;

public class BookDescription {

	// LinkedHashMap so that the books are ordered.
	public static LinkedHashMap<String, EnumSet> bookDescription;
	
	static {
		bookDescription = new LinkedHashMap<String, EnumSet>();
		bookDescription.put("Genesis", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.PENTETEUCH));
		bookDescription.put("Exodus", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.PENTETEUCH));
		bookDescription.put("Leviticus", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.PENTETEUCH));
		bookDescription.put("Numbers", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.PENTETEUCH));
		bookDescription.put("Deuteronomy", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.PENTETEUCH));
		bookDescription.put("Joshua", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("Judges", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("Ruth", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("1 Samuel", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("2 Samuel", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("1 Kings", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("2 Kings", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("1 Chronicles", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("2 Chronicles", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("1 Esdras", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("2 Esdras", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Ezra", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("Nehemiah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("Tobit", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Judith", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Esther", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS));
		bookDescription.put("Greek Esther", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS,BookCategory.DEUTEROCANONICAL));
		bookDescription.put("1 Maccabees", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("2 Maccabees", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("3 Maccabees", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("4 Maccabees", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.FORMER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Job", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.WRITINGS));
		bookDescription.put("Psalm", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.WRITINGS));
		bookDescription.put("Psalm 151", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.WRITINGS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Prayer of Manasseh", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.WRITINGS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Proverbs", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.WRITINGS));
		bookDescription.put("Ecclesiastes", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.WRITINGS));
		bookDescription.put("Song of Songs", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.WRITINGS));
		bookDescription.put("Wisdom", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.WRITINGS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Sirach", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.WRITINGS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Isaiah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Jeremiah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Letter of Jeremiah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Lamentations", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Baruch", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Ezekiel", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Daniel", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Prayer of Azariah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Susanna", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Bel and the Dragon", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS, BookCategory.DEUTEROCANONICAL));
		bookDescription.put("Hosea", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Joel", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Amos", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Obadiah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Jonah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Micah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Nahum", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Habakkuk", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Zephaniah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Haggai", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Zechariah", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Malachi", EnumSet.of(BookCategory.BIBLE, BookCategory.OLD_TESTAMENT, BookCategory.LATTER_PROPHETS));
		bookDescription.put("Matthew", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.GOSPELS));
		bookDescription.put("Mark", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.GOSPELS));
		bookDescription.put("Luke", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.GOSPELS));
		bookDescription.put("John", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.GOSPELS));
		bookDescription.put("Acts", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT));
		bookDescription.put("Romans", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("1 Corinthians", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("2 Corinthians", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("Galatians", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("Ephesians", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("Philippians", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("Colossians", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("1 Thessalonians", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("2 Thessalonians", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("1 Timothy", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("2 Timothy", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("Titus", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("Philemon", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.PAUL_EPISTLES));
		bookDescription.put("Hebrews", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.GENERAL_EPISTLES));
		bookDescription.put("James", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.GENERAL_EPISTLES));
		bookDescription.put("1 Peter", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.GENERAL_EPISTLES));
		bookDescription.put("2 Peter", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.GENERAL_EPISTLES));
		bookDescription.put("1 John", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.GENERAL_EPISTLES));
		bookDescription.put("2 John", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.GENERAL_EPISTLES));
		bookDescription.put("3 John", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.GENERAL_EPISTLES));
		bookDescription.put("Jude", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT, BookCategory.EPISTLES, BookCategory.GENERAL_EPISTLES));
		bookDescription.put("Revelation", EnumSet.of(BookCategory.BIBLE, BookCategory.NEW_TESTAMENT));
	}

	static HashSet<String> books(BookCategory category) {
		HashSet<String> bookSet = new HashSet<String>();

		for (String book : BookDescription.bookDescription.keySet()) {
			if (BookDescription.bookDescription.get(book).contains(category)) {
				bookSet.add(book);
			}
		}

		return bookSet;
	}

	// TODO: Maybe we should internally alias, but keep name
	// consistant with the translation?
	public static String GetBookName(String alias) {
		if (bookDescription.containsKey(alias)) {
			return alias;
		} else if (alias.equals("The Book of Wisdom")) {
			return "Wisdom";
		} else {
			throw new Error("Unrecognized book name: " + alias);
		}
	}
}
