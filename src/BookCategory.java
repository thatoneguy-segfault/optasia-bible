enum BookCategory {
	BIBLE("Entire Bible"), OLD_TESTAMENT("Old Testament"), NEW_TESTAMENT("New Testament"), PENTETEUCH("Penteteuch (Genesis - Deuteronomy)"), FORMER_PROPHETS("Former Prophets (Joshua - Esther)"), WRITINGS("Writings (Job - Song of Songs)"), LATTER_PROPHETS("Latter Prophets (Isaiah - Malachi)"), GOSPELS("Gospels (Matthew - John)"), EPISTLES("Epistles (Romans - Jude)"), PAUL_EPISTLES("Paul's Epistles (Romans - Philemon)"), GENERAL_EPISTLES("General Epistles (Hebrews - Jude)"), DEUTEROCANONICAL("Deuterocanonical"/*TODO*/);

	String name;
	BookCategory(String name) {
		this.name = name;
	}
	public String toString() {return name;}
}
