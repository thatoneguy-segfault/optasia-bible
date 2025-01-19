import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;

abstract class BibleGatewayFactoryBase extends BibleFactory {

	protected final int debug = 0;

	public BibleGatewayFactoryBase()
	{
		super();
		copyrightMap = new TreeMap<String, String>();
		copyrightMap.put("ASV", "<p>American Standard Version, ASV<br />Public Domain</p>");
		copyrightMap.put("CEV", "<p>Contemporary English Version, CEV<br />Copyright (c) 1995 American Bible Society.  All rights reserved.</p>");
		copyrightMap.put("CEB", "<p>Common English Bible, CEB<br />2011 Common English Bible All rights reserved.</p>");
		copyrightMap.put("AMP", "<p>Amplified Bible, AMP<br />Copyright (c) 1954, 1958, 1962, 1964, 1965, 1987 by The Lockman Foundation.</p");
		copyrightMap.put("KJ21", "<p>King James 21st Century, KJ21<br />copyright Deuel Enterprises, Inc. 1997 Gary, SD 57237.</p>");
                copyrightMap.put("DARBY", "<p>Darby Translation, DARBY<br />Public Domain</p>");
                copyrightMap.put("DRA", "<p>Douay Rheims 1899 American Edition, DRA<br />Public Domain</p>");
                copyrightMap.put("ESV", "<p>English Standard Version, ESV<br />Copyright (c) 2001 by Crossway Bibles a division of Good News Publishers.</p>");
                copyrightMap.put("GW", "p>God's Word Translation, GW<br />GOD'S WORD is a copyrighted work of God's Word to the Nations. Used by permission.</p>");
                copyrightMap.put("GNT", "<p>Good News Translation, GNT<br />Copyright 1992 American Bible Society. All rights reserved.</p>");
                copyrightMap.put("HCSB", "<p>Holman Christian Standard Bible, HCSB<br />Holman Christian Standard Bible (r) Copyright (c) 2003, 2002, 2000, 1999 by Holman Bible Publishers. All rights reserved.</p>");
                copyrightMap.put("KJV", "<p>King James Version, 1987 printing, KJV<br />Public Domain</p>");
                copyrightMap.put("MSG", "<p>The Message, MSG<br />1993, 1994, 1995, 1996, 2000, 2001, 2002 by NavPress Publishing Group</p>");
                copyrightMap.put("NASB", "<p>New American Standard Bible, NASB<br />Copyright (c) The Lockman Foundation 1999-2008.  All rights reserved.</p>");
                copyrightMap.put("NCV", "<p>New Century Version, NCV<br />Copyright 1987, 1988, 1991 by Thomas Nelson, Inc. Used by permission. All rights reserved.</p>");
                copyrightMap.put("NIRV", "<p>New Century Version, NCV<br />Copyright 1987, 1988, 1991 by Thomas Nelson, Inc. Used by permission. All rights reserved.</p>");
		copyrightMap.put("NIV", "<p>New International Version, NIV<br />THE HOLY BIBLE, NEW INTERNATIONAL VERSION(r), NIV(r) Copyright (c) 1973, 1978, 1984, 2011 by Biblica, Inc.(tm) Used by permission. All rights reserved worldwide.</p>");
                copyrightMap.put("NIV1984", "<p>New International Version, NIV<br />THE HOLY BIBLE, NEW INTERNATIONAL VERSION(r), NIV(r) Copyright (c) 1973, 1978, 1984, 2011 by Biblica, Inc.(tm) Used by permission. All rights reserved worldwide.</p>");
                copyrightMap.put("NIVUK", "<p>New International Version UK, NIVUK<br />Copyright 1973, 1978, 1984 Biblica. Used by permission of Zondervan. All rights reserved.</p>");
                copyrightMap.put("NKJV", "<p>New King James Version, NKJV<br />Copyright (c) 1982 by Thomas Nelson, Inc.  All rights reserved.</p>");
                copyrightMap.put("NLT", "<p>New Living Translation, NLT<br />Copyright (c) 1996.  Tyndale House Publishers, Inc.,<br />Wheaton, Illinois 60189.<br />All rights reserved.</p>");
                copyrightMap.put("TNIV", "<p>Today's New International Version, TNIV<br />Copyright 2001, 2005, International Bible Society</p>");
                copyrightMap.put("WE", "<p>Worldwide English New Testament, WE<br />Copyright 1969, 1971, 1996, 1998 by SOON Educational Publications, Willington, Derby, DE65 6BN, England.</p>");
                copyrightMap.put("WYC", "<p>Wycliffe New Testament, WYC<br />Public Domain</p>");
                copyrightMap.put("YLT", "<p>Youngs Literal Translation, YLT<br />Public Domain</p>");
	}

	protected TreeMap<String, String> copyrightMap;

	public class BGFilenameFilter implements FilenameFilter {
		private String pattern;

		public BGFilenameFilter(String pattern) {
			this.pattern = pattern;
		}

		public boolean accept(File directory, String filename) {
			Boolean result = filename.matches(pattern);
			return result;
		}
	}

	/**
	 * Read a directory
	 * Bible Gateway
	 */
	public Translation readDirectory(Path dir, String shortname, String filepattern, LibraryIndex indexOnly) throws IOException {
		Translation t = new Translation(this.library);

		if (debug >= 1) System.out.println("Reading " + shortname + " " + dir.toString());

		// Namefile contains the full name of the translation.
		File namefile = new File(dir + File.separator + "version.txt");
		assert namefile.exists();
		BufferedReader namereader = new BufferedReader(new FileReader(namefile));
		t.setFullname(namereader.readLine());
		t.setShortname(shortname);
		if (debug >= 1) System.out.println(" " + t.fullname);

		// Read in each chapter
		File dirFile = new File(dir.toString());
		assert dirFile.exists();
		if (debug >= 2) System.out.println(" " + dirFile.getAbsolutePath());
		for (File chapterFile : dirFile.listFiles(new BGFilenameFilter(filepattern))) {
			if (debug >= 2) System.out.println("  Reading " + shortname + " " + chapterFile.toString());
			try {
				readChapterFile(t, new BufferedReader(new InputStreamReader(new FileInputStream(chapterFile), "UTF-8")), indexOnly);
			} catch (IOException e) {
				System.out.println("Error reading " + chapterFile.getName());
				throw e;
			}
		}

		t.setCopyright(copyrightMap.get(shortname));

		return t;
	}

	/**
	 * Identify the portion of the chapter file containing the scripture.
	 * Biblegateway base
	 */
	protected void readChapterFile(Translation t, BufferedReader r, LibraryIndex indexOnly) throws IOException {
		// Unfortunately, the html isn't XML compliant,
		// and doesn't support XmlParser or XmlSlurper

		// The only way I see to identify the text is the
		// <h4> flag.
		while (true) {
			String s = r.readLine();
			if (s == null) break;

			if (s.matches(".*<h4>.*<\\/h4>.*")) {
				parseText(t, s, indexOnly);
			}
		}
	}

	protected void parseText(Translation t, String s, LibraryIndex indexOnly) {
		parseText(t, s, indexOnly, null, null);
	}


	/**
	 * Parse the BibleGateway text
	 * Biblegateway base
	 */
	protected void parseText(Translation t, String s, LibraryIndex indexOnly, String book, String chapter) {
		if (debug >= 1) System.out.println("parseText(" + t.shortname + ", " + book + ", " + chapter + ")");
		// Get Book, chapter, and translation
		// <p><h4>1 Samuel 10</h4><h5> Foo </h5>foo<span id="en-NIV-2421" class="sup">
		// <p><h4>1 Samuel 10</h4><h5> Foo </h5>foo<sup id="en-NIV-2421" class="versenum">
		// <p><h4>1 Chronicles 1</h4> &nbsp&nbsp&nbsp<versenum id=\"1\" />Adam, Seth, Enosh,<p />&nbsp;&nbsp;&nbsp;  <br />&nbsp;<sup class="versenum" id="en-KJ21-10237">2</sup>Kenan, Mahalaleel, Jared,<p />&nbsp;&nbsp;&nbsp;  <br />&nbsp;<sup class="versenum" id="en-KJ21-10238">3</sup>
		// <h4>Genesis 1</h4><h5 class="passage-header">World’s creation in seven days</h5>&nbsp;<sup class="versenum" id="en-CEB-1">1</sup> When God began to create<sup class='footnote' value='[<a href="#fen-CEB-1a" title="See footnote a">a</a>]'>[<a href="#fen-CEB-1a" title="See footnote a">a</a>]</sup> the heavens and the earth— <sup class="versenum" id="en-CEB-2">2</sup> the
		String shortname = null;

		REHelper re = new REHelper();


		if (re.strMatches(s, "^(?:<p>)?<h4>((?:[0-9] )?[A-Za-z ]+) ([0-9]+)<\\/h4>.*?<(?:span|sup) (?:class=\"versenum\" )?+id=\"en-([A-Z0-9]+)-[0-9]+\".*$")) {
			shortname = re.group(3);
			book = re.group(1);
			chapter = re.group(2);
			if (debug >= 2) System.out.println("parseText() type A " + shortname + " " + book + " " + chapter);
		} else { // Some chapters have a chapter title elsewhere.
			// <p> <br />&nbsp;<sup class="versenum" id="en-KJ21-8348">3</sup>and come to the king and speak in this manner unto him." So Joab put the words in her mouth.<p />&nbsp;&nbsp;&nbsp;
			assert (book != null);
			assert (chapter != null);

			if (debug >= 2) System.out.println("parseText() type B");

			if (re.strMatches(s, "^(?:<p>).*?<(?:span|sup) (?:class=\"versenum\" )?+id=\"en-([A-Z0-9]+)-[0-9]+\".*?$")) {
				if (debug >= 2) System.out.println("parseText() type B1");
				shortname = re.group(1);
			} else if (re.strMatches(s, "^(?:<p>).*<h4>((?:[0-9] )?[A-Za-z ]+) ([0-9]+)<\\/h4>(?:[^<]+|<br \\/>)*<versenum id=.*$")) {
				if (debug >= 2) System.out.println("parseText() type B2");
				book = re.group(1);
				chapter = re.group(2);
				shortname = t.shortname; // Unable to verify in this case.
			} else if (re.strMatches(s, "^(?:<p>)?.*?<(?:span|sup) (?:class=\"versenum\" )?+id=\"en-([A-Z0-9]+?)-[0-9]+?\".*?$")) {
				if (debug >= 2) System.out.println("parseText() type B3");
				shortname = re.group(1);
			} else {
				throw new AssertionError(t.shortname + ": No match: " + s);
			}
		}

		book = fixBookName(book);

		assert ((t.shortname.equals("NIV1984") && shortname.equals("NIV")) || (t.shortname.equals(shortname))); // Double-check that we are doing this correctly.

		Book b = t.getBook(book);
		Chapter c = b.createChapter(Integer.parseInt(chapter));
		if (null != indexOnly) {
			indexOnly.addSupport(c);
			return; // no more processing. Only fill index.
		}
		//System.out.println("TEST: reading " + book + " " + chapter); 
		parseChapter(c, s);
	}

	/**
	 * Parse a single line of text that contains the chapter.
	 */
	protected void parseChapter(Chapter c, String s) {
		// Split for verses.
		// <span id="en-NIV-2421" class="sup">1</span>
		// <sup id="en-CEV-213" class="versenum" value='1'>1</sup>
		// <sup class="versenum" id="en-NIV-28371">23</sup>
		// <span id="en-CEB-35508" class="text 1Esd-1-3"><sup class="versenum">3 </sup>
		//def verseSplit = /<(?:span|sup) +(?:class="versenum" *)?id="[a-z]{2}-[A-Z]{3,4}-[0-9]+" +class="(?:sup|versenum)"[^>]*>/
		
		//String verseSplit = "(?:<(?:span|sup) +(?:class=\"versenum\" +|id=\"[a-z]{2}-[A-Z]{3,4}-[0-9]+\" +|class=\"sup\" +)+[^>]*>|<versenum )";
		

		String verseSplit;



		if (c.book.translation.shortname == "NIV") {
			verseSplit = "(?:<h[0-9]>\\s+)?(?:<(?:span|sup) +(?:class=\"versenum\" +|id=\"[a-z]{2}-[A-Z]{3,4}-[0-9]+\" +|class=\"sup\" +|class=\"text [0-9A-Za-z]+-[0-9]+-[0-9]+\")+[^>]*>)";
		} else {
			verseSplit = "(?:<h[0-9]>\\s+)?(?:<(?:span|sup) +(?:class=\"versenum\" +|id=\"[a-z]{2}-[A-Z]{3,4}-[0-9]+\" +|class=\"sup\" +|class=\"text [0-9A-Za-z]+-[0-9]+-[0-9]+\")+[^>]*>|<versenum )";
		}

		// Add each portion as a chapter entry.
		String[] split = s.split(verseSplit);
		if (!(split.length > 1))
			throw new AssertionError("length="+split.length+"\n"+s+"\n"); // there's always at least 1 verse.

		//LinkedList<String> chapterEntries = new LinkedList<String>();

		for (String segment : split) {
			//TODO: Replace c.data with c.add(chapterEntry)
			parseChapterEntry(c.data, c, segment);
			//chapterEntries.addLast(segment);
		}

		//while (!chapterEntries.isEmpty()) {
			//parseChapterEntry(c.data, c, chapterEntries);
		//}
	}

	/**
	 * Recursively parse chapter entries.
	 */
	protected void parseChapterEntry(ArrayList<ChapterEntry> list, Chapter c, /*LinkedList<String> chapterEntries*/ String s) {
		//if (chapterEntries.isEmpty())
			//return;
		//s = chapterEntries.removeFirst();
		if (s == null) return;
		s = cleanUnicode(new StringBuilder(s), c).toString();
		s = cleanHtml(s);
		s = removeCrossref(c, s);
		if (debug >= 10) System.out.println(s);
		//def e = []
		REHelper re = new REHelper();
		if (re.strMatches(s, "^ *([0-9]+) *</(?:span|sup)>(?:<br />)?(.*)$")) { // a single verse
			list.add(new Verse(c, Integer.parseInt(re.group(1)), 0, cleanVerse(c, re.group(2))));
		} else if (re.strMatches(s, "^ *([0-9]+)-([0-9]+) *</(?:span|sup)>(.*)$")) {
			// Multiple verses.  e.g. 21-23
			if (!re.group(3).equals("")) {
				list.add(new Verse(c, Integer.parseInt(re.group(1)),
					Integer.parseInt(re.group(2)), cleanVerse(c, re.group(3))));
			} else { // I don't care for this solution
				list.add(new Verse(c, Integer.parseInt(re.group(1)),
					Integer.parseInt(re.group(2)), ""));
			}
		} else if (re.strMatches(s, "^id=\"([0-9]+)\" +(?:span=\"[0-9]+\" +)?/>(.*)$")) {
			// some verses in MSG
			list.add(new Verse(c, Integer.parseInt(re.group(1)), 0, cleanVerse(c, re.group(2))));
		} else if (re.strMatches(s, "^<font class[^>]+?>(.*?)(?:</font> *?)?(?:<p /> *?)?$")) {
			//chapterEntries.addFirst(re.group(1));
			parseChapterEntry(list, c, re.group(1));
		} else if (re.strMatches(s, "^<span class=\"chapternum\">[0-9]+\\s*(.*)$")) {
			// CEB 1 Esdras
			list.add(new Verse(c, 1, 0, cleanVerse(c, re.group(1))));
		} else if (re.strMatches(s, "^<sup class=\"versenum\">([0-9]+)</sup>(.*)</span>(?:</p>)?\\s*(?:<h[0-9]>)?$")) {
			list.add(new Verse(c, Integer.parseInt(re.group(1)), 0, cleanVerse(c, re.group(2))));
		} else if (s.matches("^.*<h\\d>.*?$")) { // Headings
			s = removeFootNotes(s);
			s = s.replaceAll("<reference\\s*osisRef[^>]*>[^<]*</reference>", "");
			s = s.replaceAll("\\([ ;]*\\)", "");
			if (re.strMatches(s, "^(.*?)<h(\\d)>\\s*?((?:[^<>]|<[bi]>|</[bi]>|<br />)+?)</h\\2>(.*)$")) {
				parseChapterEntry(list, c, re.group(1));
				list.add(new Heading(c, Integer.parseInt(re.group(2)), re.group(3)));
				parseChapterEntry(list, c, re.group(4));
			} else if (re.strMatches(s, "^</?h(\\d)>$")) {
				// do nothing; don't know how to keep the header in this case
			} else {
				throw new AssertionError(String.format("\n%s %s %d Unknown chapter entry [error1]: ",
					c.book.translation.shortname, c.book.title, c.number) + s);
			}
		} else if (re.strMatches(s, "^<h5 class=\"passage-header\">((?:[^<]|<br />)+)</h5>(.*)$")) {
			list.add(new Heading(c, 5, re.group(1)));
			parseChapterEntry(list, c, re.group(2));
		} else if (re.strMatches(s, "^<i>([^<]+)</i>(.*)$")) {
			list.add(new Heading(c, 6, re.group(1)));
			parseChapterEntry(list, c, re.group(2));
		} else if (re.strMatches(s, "^ *<b>((?:[^<]|<i>|</i>)+)</b>(.*)$")) {
			list.add(new Heading(c, 6, re.group(1)));
			parseChapterEntry(list, c, re.group(2));
		} else if (re.strMatches(s, "^<b>([^<]+)</b>\\s*(?:<br />)?\\s*$")) {
			list.add(new Heading(c, 6, re.group(1)));
		} else if (re.strMatches(s, "^(Of David.| Aleph|A psalm.)$")) {
			list.add(new Heading(c, 6, re.group(1)));
		} else if (re.strMatches(s, "^(A psalm.) *$")) {
			list.add(new Heading(c, 6, re.group(1)));
		} else if (re.strMatches(s, "^([A-Za-z ]+<i>(miktam|maskil|shiggaion)</i>[ A-Za-z,;]+\\.) *$")) {
			// Psalm
			list.add(new Heading(c, 6, re.group(1)));
		} else if (re.strMatches(s, "^([A-Za-z\\[\\]]+) *$")) {
			// Song of Songs
			list.add(new Heading(c, 6, re.group(1)));
		} else if (re.strMatches(s, "^(<[bi]>[A-Za-z]+</[bi]> *(<br /> +)* *(&nbsp)*)$")) {
			//Song of Songs
			list.add(new Heading(c, 6, re.group(1)));
		} else if (s.matches("^\\s*$")) { // Whitespace
			// do  nothing
		} else if (s.matches("^<br />$")) { // Whitespace
			// do  nothing
		} else if (s.matches("^\\s*<br\\s*/>\\s*$")) { // Whitespace
			// do  nothing
		} else if (re.strMatches(s, "^(.*)(<p>)(.*)$")) {
			// paragraph
			parseChapterEntry(list, c, re.group(1));
			list.add(new HtmlVerse(c, re.group(2)));
			parseChapterEntry(list, c, re.group(3));
		} else if (re.strMatches(s, "^(.*)(<p />)(.*)$")) {
			parseChapterEntry(list, c, re.group(1));
			list.add(new HtmlVerse(c, re.group(2)));
			parseChapterEntry(list, c, re.group(3));
		} else if (re.strMatches(s, "^<br\\s*/>(.*)$")) {
			// This might be a left-over from a verse of a previous chapter.
			/* TODO: verify verse # */
			list.add(new Verse(c, 1, 0, cleanVerse(c, re.group(1))));
		} else if (re.strMatches(s, "^<reference type=\"x-bookName\">([^<]*)</reference>([^<]*)$")) {
			// An odd thing that happens in GNT at the beginning of some books.
			// TODO: probably should re-incorporate into previous chapter.
			assert re.group(1).length() > 0;
			list.add(new Verse(c, 1, 0, cleanVerse(c, re.group(1) + " " + re.group(2)))); /* TODO: verify verse # */
		} else if (s.matches("^(?:[^<>]|<i>[^<>]+</i>|<br />)+$")) { // an unlabeled? first verse NKJV Song of Songs 2
			list.add(new Verse(c, 1, 0, cleanVerse(c, s)));
		} else if (s.matches("^</item></list>$")) {
			// do nothing // GNT 1 Maccabees 9
		} else if (re.strMatches(s, "^(?:</?speech>)+(.*)$")) {
			parseChapterEntry(list, c, re.group(1));
		} else if (re.strMatches(s, "^(?:<begin-paragraph[^>]*/>)(.*)$")) {
			parseChapterEntry(list, c, re.group(1));
		} else if (s.matches("^\\[$")) { // A stray '[' in GNT
			// do nothing
		} else if (s.matches("^</?div>$")) {
			// whitespace
		} else if (s.matches("^(?:<woj>)?(<br />)?[^<]{10,}.*") && (c.getVerseList().size() == 0)) {
			System.out.println(c.book.translation.shortname+" "+c.book.title+" "+c.number + ": Assumed to be missing number for first verse.");
			list.add(new Verse(c, 1, 0, cleanVerse(c, s)));
		} else  if (s.matches("^( +- +)$")) { // Mistakes
			System.out.println(c.book.translation.shortname+" "+c.book.title+" "+c.number+ ": Warning: Odd characters outside of verse: '$s'");
		} else if (s.matches("^id=\"[0-9]+\"\\s/>(?:[^<>]|<i>|</i>)*(</li>\\s*)?$")) {
			// do nothing // footnote in NIV 2011, CEB
		} else if (s.matches("^<i>$")) {
			// do nothing // NIV 2011
		} else if (s.matches("^\\[\\d+?\\]</sup>\\s*</font>\\s*$")) {
			// do nothing // NIV 2011
		} else if (s.matches("^\\[[0-9]+?\\]</sup>\\s*<sup class='footnote'(?:[^<]|<a|</a>)*?</sup>(?:\\s|</font>|<font[^<>]+?>)*$")) {
			// do nothing // empty verse with only a footnote; NIV 2011
		} else if (re.strMatches(s, "^id=\\\\\"([0-9]+)\\\\\"\\s+/>(.*)$")) {
			// ESV Deuteronomy 1
			list.add(new Verse(c, Integer.parseInt(re.group(1)), 0,
					cleanVerse(c, re.group(2))));
		} else if (re.strMatches(s, "^id=\\\\\"([0-9]+)\\\\\"\\s+span=\"([0-9]+)\"\\s+/>(.*)$")) {
			// GNT Job 11
			list.add(new Verse(c, Integer.parseInt(re.group(1)), Integer.parseInt(re.group(2)),
					cleanVerse(c, re.group(3))));
		} else if (s.matches("^<font[^<>]+?>$")) {
			// do nothing
		} else {
			throw new AssertionError("\n"+c.book.translation.shortname+" "+c.book.title+" "+c.number+"\nUnknown chapter entry: '" + s + "'");
		}
	}

	protected String cleanVerse(final Chapter c, String text) {
		text = cleanText(c, text);
		text = removeFootNotes(text);
		text = text.replaceAll("<p />", "</p>\n<p>");
		return text;
	}


	/**
	 * Clean up mistakes, etc.
	 */
	protected String cleanText(final Chapter c, String s) {
		String errormsg =  "unitTest";
		if (c != null)
			errormsg =  c.book.translation.shortname + " " + c.book.title + " " + c.number;


		// Some errant tag
		Matcher m = Pattern.compile("^(.*?:)\\|sc\\s*([A-Z]+.*?)$").matcher(s);
		if (m.matches()) {
			System.out.println("Found text " + m.group(0) + " in " + errormsg + ", removing letters '|sc'.");
			s = m.replaceAll("$1 $2");
		}

		if (s.contains("|sc")) {
			throw new AssertionError("Script failed to clean text2: ("+c.fullString()+")'"+s+"'");
		}

		// capitalized word was merged with preceeding text
		m = Pattern.compile("([a-z:])([A-Z])").matcher(s);
		if (m.matches()) {
			System.out.println("Found text " + m.group(0) + ", in " + errormsg + ", inserting space before capitol letter");
			s = m.replaceAll("\\$1 \\$2");
		}

		// Fonts should be removed
		//s = s.replaceAll("</font>", "");  // COMPARE
		//s = s.replaceAll("<font[^<>]+?>", "");  // COMPARE

		return s;
	}

	protected String removeFootNotes(String text) {
		// new style footnote reference
		//text = text.replaceAll(/\s*<\s*sup\s*class=['"]footnote["']\s*value=.*?<\/sup>/, "")
		// NOTE: having the extra \s* at the beginning removes
		// necessary spaces from NASB.  I don't know if it was necessary
		// for something else.  (In WYC, 1 Peter 3:1, this allows a 
		// space between a word and the following comma.)
		text = text.replaceAll("<\\s*sup\\s*class=['\"]footnote[\"']\\s*value=.*?</sup>", "");

		// new style footnote list
		// This style of footnotes goes past the end of the line
		text = text.replaceAll("\\s*<div class=['\"]footnotes[\"']><strong>Footnotes:</strong>.*$", "");

		// old style footnote reference
		text = text.replaceAll("\\s?<\\s*sup\\s*>\\s*\\[\\s*<\\s*a\\s+href\\s*=\\s*\"#[^\\]]+\\]\\s*<\\s*/sup\\s*>", "");

		// old style footnote list
		// erase until end of line.
		text = text.replaceAll("\\s*<strong>\\s*Footnotes:</strong>.*$", "");

		// CEV, Psalm 60 has random footnote end marking.
		text = text.replaceAll("</footnote>","");

		// clean NASB footnotes
		text = text.replaceAll("<\\s*sup\\s*>\\s*\\(<a\\s*href\\s*=\\s*\"[^\"]*NASB[^\"]*\"\\s*title\\s*=\\s*\"[^\"]*\"\\s*>[^<]*</a>\\)</sup>", "");

		// clean NASB cross references
		text = text.replaceAll("<p><strong>Cross references:</strong>.*", "");

		// CEB footnote references
		text = text.replaceAll("<sup>[a-z]</sup>", "");

		return text;
	}

	protected String removeCrossref(Chapter c, String text) {
		text = text.replaceAll("<sup\\s*class='xref'.*?</sup>","");
		return text;
	}

	public void unitTest()
	{
		String s;

		// cleanText
		s = "abc|lscdef";

	}

}
