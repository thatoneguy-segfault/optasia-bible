import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

class GreekBibleFactory extends BibleFactory {

	private TreeMap<String, String> translationMap;

	private TreeMap<String, String> bookNameMap;

	private String newline;

	public GreekBibleFactory() {
		super();
		translationMap = new TreeMap<String, String>();
		bookNameMap = new TreeMap<String, String>();

		newline = System.getProperty("line.separator");

		translationMap.put("NTGbrl", "New Testament Greek, Braille");
		translationMap.put("NTGsr", "New Testament Greek, Screen Reader");

		bookNameMap.put("Matt", "Matthew");
		bookNameMap.put("mark", "Mark");
		bookNameMap.put("luke", "Luke");
		bookNameMap.put("john", "John");
		bookNameMap.put("acts", "Acts");
		bookNameMap.put("rom", "Romans");
		bookNameMap.put("1cor", "1 Corinthians");
		bookNameMap.put("2cor", "2 Corinthians");
		bookNameMap.put("gal", "Galatians");
		bookNameMap.put("eph", "Ephesians");
		bookNameMap.put("phil", "Philippians");
		bookNameMap.put("col", "Colossians");
		bookNameMap.put("1thes", "1 Thessalonians");
		bookNameMap.put("2thes", "2 Thessalonians");
		bookNameMap.put("1tim", "1 Timothy");
		bookNameMap.put("2tim", "2 Timothy");
		bookNameMap.put("titus", "Titus");
		bookNameMap.put("phlm", "Philemon");
		bookNameMap.put("heb", "Hebrews");
		bookNameMap.put("jam", "James");
		bookNameMap.put("1pet", "1 Peter");
		bookNameMap.put("2pet", "2 Peter");
		bookNameMap.put("1john", "1 John");
		bookNameMap.put("2john", "2 John");
		bookNameMap.put("3john", "3 John");
		bookNameMap.put("jude", "Jude");
		bookNameMap.put("rev", "Revelation");
	}

	ArrayList<String> getSupportedTranslations() {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(translationMap.keySet());
		return l;
	}

	public Translation translation(ConcurrentLibraryIface library, String translation, Path inputDirectory, LibraryIndex indexOnly) throws IOException {
		this.library = library;
		if (translationMap.containsKey(translation)) {
			Translation t = readFile(new Path(inputDirectory, "GreekNT", "gntc.txt"), translationMap.get(translation), translation, indexOnly);
			t.verify();
			return t;
		} else {
			return null;
		}
	}

	protected String getBookName(String gname) {
		String name = bookNameMap.get(gname);
		if (name == null) {
			throw new Error("Unable to recognize book name '"+gname+"'");
		}
		return name;
	}

	public Translation readFile(Path filename, String fullname, String shortname, LibraryIndex indexOnly) throws IOException{
		final Translation t = new Translation(this.library);
		t.setFullname(fullname);
		t.setShortname(shortname);
		final BufferedReader r;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(filename.toString()), "UTF-8"));
		} catch (IOException e) {
			System.out.println("Error reading file "+filename.toString());
			throw e;
		}

		Set<String> firstTimeBookChapter = new HashSet<String>();

		String s;
		String notes = "";
		while (true) {
			s = r.readLine();
			if (s == null) {
				break;
			}

			if (s.matches("^\\*.*")) {
				notes = convertBraille(s, shortname); // Save the notes and print it at the beginning of the next chapter.
			} else if (s.matches("^\\s*$")) {
				// do nothing
			} else {
				Matcher m = Pattern.compile("^\\s+([0-9]*[A-Z]*[a-z]+)\\s+(\\d+):(\\d+)\\s+(.*)$").matcher(s);
				if (m.matches()) {
					String bookname = getBookName(m.group(1));
					int chapterNumber = Integer.parseInt(m.group(2));
					int verseNumber = Integer.parseInt(m.group(3));
					String text = m.group(4);

					Book b = t.getBook(bookname);
					Chapter c = b.getChapter(chapterNumber);

					if (null != indexOnly) {
						indexOnly.addSupport(c);
					} else {
						String bookChapter = bookname + chapterNumber;

						if (!firstTimeBookChapter.contains(bookChapter)) {
							// Add a chapter heading
							c.data.add(new Heading(c, 2, bookname + " " + chapterNumber));

							// Add notes
							if (chapterNumber == 1) {
								c.data.add(new HtmlVerse(c, "<p>" + notes + "</p>"));
							}

							firstTimeBookChapter.add(bookChapter);
						}
						c.addVerse(verseNumber, convertBraille(cleanVerse(c, text), shortname) + "</br>");
					}
				} else {
					throw new Error(shortname+" ("+filename.toString()+"): Unable to parse line '"+s+"'"+ System.getProperty("line.separator"));
				}
			}
		}

		r.close();

		t.setCopyright(
		"<p>New Testament Greek</p>" + newline
+"<p>  The Greek New Testament , Fourth Revised Edition, edited by Barbara Aland, Kurt Aland, Johannes Karavidopoulos, Carlo M. Martini, and Bruce M. Metzger in cooperation with the Institute for New Testament Textual Research, M¸nster/Westphalia, Copyright 1993 Deutsche Bibelgesellschaft, Stuttgart. </p>" + newline
+"<p>  Westcott and Hort GNT, 1881. Westcott-Hort Greek Text prepared by Maurice A. Robinson, Ph.D. Department of Biblical Studies and Languages, Southeastern Baptist Theological Seminary, P. O. Box 1889, Wake Forest, North Carolina 27588. The Westcott and Hort GNT is the basis for the New Testaments of the English Revised Version 1885, and the American Standard Version ASV 1901, with formal revisions using EberhardNestle's [23rd] Edition Greek NT resulting in the Revised Standard Version 1952 and the New American Standard Bible 1971.  Accented.</p>" + newline
+"<p>  Robinson-Pierpont Majority Text GNT 1995.  [Byzantine] Produced by Maurice A. Robinson and William G. Pierpont. Public Domain. Accented. The Robinson-Pierpont is a modern (1995) attempt at producing a reliable Majority Text version of the Greek New Testament. (Another Majority Text Greek NT for Thomas Nelson, Inc. is planned by Zane Hodges and Arthur Farstad.)</p>" + newline
+"<p>  Greek New Testament Text of the Greek Orthodox Church.  This public domain text was digitized by the Orthodox Skite St. Spyridon. </p>" + newline
+"<p>  Tischendorf New Testament. Lobegott Friedrich Konstantin von Tischendorf (1815-1874) GNT 8th Edition, 1869-1872. The text is the result of the joint work of Clink Yale and Ulrik Petersen. For details see http://ulrikp.org/Tischendorf.</p>" + newline
+"<p>  The Greek New Testament, Edited from Ancient Authorities, with their Various Readings in Full, and the Latin Version of Jerome, by Samuel Prideaux Tregelles, LL.D. London. Samuel Bagster and Sons: Paternoster Row. C. J. Stewart: King William Street, West Strand. 1857-1879. Transcription of TRG1 and Corrected version edited by Dirk Jongkind, in collaboration with Julie Woodson, Natacha Pfister, and Robert Crellin. Consultant editor: P.J. Williams Tyndale House, Cambridge 2009. for more on the Tregelles project, see their website: http://www.tyndalehouse.com/tregelles/index.html. TNT1 represents the uncorrected version of the Tregelles text. Compiled into BibleWorks by Michael Hanel </p>" + newline
+"<p>  The Trinitarian Bible Society Greek New Testament. The text is essentially the same as The New Testament in the Original Greek According to the Text Followed in The Authorized Version, F.H.A. Scrivener (Cambridge: 1894). See entries for SCR and SCM versions).  von Soden Greek New Testament. Soden, Hermann von, Die Schriften des Neuen Testaments in ihrer altesten erreichbaren Textgestalt. 4 volumes. Teil 1, Abteilung 1-3, Berlin: Verlag von Alexander Duncker, 1902-1910; Teil 2, Text mit Apparat, Gottingen: Vandenhoeck und Ruprecht, 1913. (Text is found on pages 1-893 of the Teil 2 volume.) Electronic Edition Version 1.0 December 21, 2005 Edited by George C. Yale. The text has been corrected against a facsimile copy of von Soden. The text thus mostly conforms with the printed von Soden. Even in cases of clear typographical errors, the text has been retained as it was printed. </p>" + newline
+"<p>  The fifth text in this collection is a comparative Greek New Testament</p>" + newline);

		if (shortname.equals("NTGbrl")) {
			t.setAbout(getGreekAbout() + getBrailleAbout());
		} else if (shortname.equals("NTGsr")) {
			t.setAbout(getGreekAbout() + getScreenReaderAbout());
		} else {
			throw new AssertionError("Unknown translation type '" + shortname + "'");
		}

		return t;
	}

	private String convertBraille(String text, String shortname) {
		if (shortname.equals("NTGbrl")) {
			return text;
		} else if (shortname.equals("NTGsr")) {
			//s = s.replaceAll(/&[lr]dquo;/, "&quot;")
			text = text.replaceAll("y", "ps");

			text = text.replaceAll("&amp;", "ch");
			text = text.replaceAll("\\?", "th");

			text = text.replaceAll(" :", " hay");
			text = text.replaceAll("=:", "=hay");
			text = text.replaceAll("\\[:", "[hay");
			text = text.replaceAll("^:", "hay");

			text = text.replaceAll(":", "ay");

			text = text.replaceAll(" o", " ho");
			text = text.replaceAll("=o", "=ho");
			text = text.replaceAll("\\[o", "[ho");
			text = text.replaceAll("^o", "ho");

			text = text.replaceAll("\\bw\\b", "Oh");

			text = text.replaceAll("w", "oh");


			return text;
		} else {
			throw new AssertionError("Unknown translation type '" + shortname + "'");
		}
		
	}

	private String cleanVerse(Chapter c, String text) {
		return cleanHtmlCodes(text);
	}


	private String getGreekAbout() {
	return 
"<h3>Comparative Greek New Testament -- a comparison of the actual printed text of four editions of the Greek New Testament.</h3>" + newline
+"<p>(Taken from http://www.proveallthings.org/articles/comparative_greek_new_testament.htm)</p>" + newline
+"<p>The Comparative Greek New Testament is not an extensive list of various manuscript readings. It is simply a comparison of the actual printed text of four editions of the Greek New Testament. The information was originally taken from web site http://www.centauri.com.br/ictus/greek.htm (although it appears many other places) (this web site no longer exists), and it has been edited and expanded here under Phonetics. You can also print out a worksheet for learning how to write the Greek alphabet.</p>" + newline
+ newline
+ newline
+"<h4>Orthography  </h4>" + newline
+"<p>The orthography follows the Stephanus 1550 as it appears in the edition of George Ricker Berry. Characteristic of Berry's orthography was his uniform inclusion of the final Nu or Sigma in words which might add or drop them before a consonant or vowel.</p>" + newline
+ newline
+"<h4>Versification  </h4>" + newline
+" <p>Verse numbering follows closely the King James Version of 1611.  In sixteen places, the verses are divided differently, and are noted with a single carot, ^ at: Matthew 14:26; Mark 3:20; 6:28; 12:15; Luke 6:18; Acts 3:1; 3:20;13:33; 17:33; 24:3; First Corinthians 14:19; Ephesians 3:18; Philippians 1:17; 2:8; First Thessalonians 2:7; Philemon 1:12. </p>" + newline
+"  <p>Two more differences noted with a carot, ^ are: Matthew 23:13-14 are transposed in M and K; Romans 16:25-27 appears at the end of Romans 14:23 in M. Additional versification irregularities include: Luke 17:36 appears in K only. Acts 8:37; 15:34; 24:7 are not in A or M. Portions of some verses are missing in various texts, such as Acts 4:5-6; First John 5:7. In addition, the enumeration of verses varies in Second Corinthians 13:12-13 and Third John 14. All of these can be discerned in the text.</p>" + newline
+newline
+newline
+"<h4>Diacritical Marks</h4>" + newline
+"  <p>At the time of the writing of the New Testament, such documents would have been written in all capital letters, with no divisions between the words. (ITWOULDAPPEARLIKETHIS)  The words in this comparative text are divided and they appear in cursive characters, which renders it much easier to read and to search. Centuries later, breathers, accents, capitalization, punctuation, and other diacritical markings were added. Because a single word inflection may have variations in accent, and because punctuation may interrupt word searches, such markings are omitted from this text as a practical hindrance to word searches. Of course, this creates a difficulty with words which are spelled the same though they have different markings and meanings. The trade of one problem for the other was necessary, and this was the most practical course to take.</p>" + newline
+newline
+newline
+newline
+"<h4>Titles and Colophons</h4>" + newline
+"  <p>The introductory titles and closing colophons may be considered editorial additions, but are included here for completeness. Introductory titles are marked with an asterisk * . Closing colophons are marked with double carots, ^^.</p>" + newline
+newline
+newline
+"<h4>Variant Textual Tag Codes</h4>" + newline
+"  <p>The following tags precede those words which are peculiar to one printed text. Where the four texts do not differ, there are no tags.</p>" + newline
+newline
+newline
+"  <p>T = (Textus Receptus) Stephens, 1550, editio regia (royal edition) (reproduced, 1897).  The text used is George Ricker Berry's edition found in The Interlinear Literal Translation of the Greek New Testament (New York: Hinds & Noble, 1897). This text is virtually identical to editions of Disiderius Erasmus (1516, 1519, 1522, 1535), Complutensian Polyglot (1522, 1564, 1573, 1574, 1584, 1590, 1609, 1619, 1620, 1628, 1632), Simon Colinaeus (1534), Robert Stephens (1546, 1549, 1550, 1551), Theodore Beza (1565, 1582, 1588, 1598), and Bonaventure and Abraham Elzevir (1624, 1633, 1641). The edition first named \"Textus Receptus\" was Elzevir 1633 \"textum ergo habes, nunc ab omnibus receptum.\"</p>" + newline
+newline
+" <p>K = (KJV 1611) Scrivener 1894, 1902 (reproduced, 1976).  This is the text of \"E Kaine Diatheke\" Braille Greek, : kain: dia?:k:, The New Testament, The Greek Text Underlying the English Authorized Version of 1611 (London: Trinitarian Bible Society, 1976). This is an unedited reprint of F.H.A. Scrivener's \"The New Testament in the Original Greek according to the Text followed in the Authorized Version\" (Cambridge: University Press, 1894, reprint edition 1902). This is a reconstruction of the Greek text underlying the English 1611, it largely follows Beza 1598, and though sometimes the KJV text follows no Greek manuscript whatsoever, Scrivener nowhere constructs a Greek reading without some manuscript evidence. Though this edition generally follows the \"Byzantine\" texts, it nevertheless agrees with the modern \"Alexandrian\" editions in many places.</p>" + newline
+newline
+newline
+" <p>M = Majority 1911 / 1929 / 1982. In The Greek New Testament according to the Majority Text, (Nashville: Thomas Nelson, 1982; 1985) Zane C. Hodges and Arthur L. Farstad developed a method of identifying and weighing manuscript evidence. This is a critical text constructed from the apparatus of other critical editions (Freiherr Von Soden, Die Schriften des Neuen Testaments in ihrer altesten erreichbaren Textgestalt , Gottingen: Vandenhoeck und Ruprecht, 1911, and Herman C. Hoskier, Concerning the Text of the Apocalypse , London: Bernard Quaritch, 1929). This same information was later used by Maurice A. Robinson and William G. Pierpont in The New Testament in the Original Greek According to the Byzantine / Majority Textform (Original Word Publishers, Atlanta, 1991, 1995). They disagree with Hodges and Farstad regarding the evaluation of manuscript evidence, but nevertheless agree on 99.75 percent of the text.</p>" + newline
+newline
+newline
+newline
+" <p>B = [TKM=] Byzantine. Where T [Stephanus / Textus Receptus], K [Scrivener / King James] , and M [Hodges, Farstad / Robinson, Pierpont] agree, we have simplified the marking with a B for Byzantine.</p>" + newline
+newline
+newline
+" <p>A = Alexandrian 1975. This is the text of The Greek New Testament , 3rd edition (Kurt Aland, Matthew Black, Carlo M. Martini, Bruce M. Metzger, Allen Wikgren, United Bible Society, 1975, corrected 1983, the same as the Nestle-Aland Novum Testamentum Graece, 26th edition, Deutsche Bibelstiftung, Stuttgart, 1979). It is mostly in agreement with the original Westcott and Hort Text of the Nineteenth Century. It is largely the text used in all modern translations of the Bible, such as the New American Standard Version and the New International Version. Most of the Alexandrian variants from the Textus Receptus are matters of spelling and word order, but that does not reduce the significance of selected variants.</p>" + newline
+newline
+newline
+newline
+"<h4>Possible Combinations of Textual Codes</h4>" + newline
+"  <p>These are all of the possible combinations of tags which are actually found in the comparative text: T, TK, B= [TKM], K, M, MT, MK, A, AM, AMT, AMK, AK, ATK</p>" + newline;
	}



	private String getScreenReaderAbout() {
	return 
"<h2> Key to Greek Alphabet for use with screen readers</h2>" + newline
+newline
+"<p>Below are the names of the letters of the Greek alphabet, followed by the letter or letters used in the Greek New Testament text included in the Optasia Bible Library for use with screen readers, along with some notes about pronounciation.</p>" + newline
+newline
+"<ul>" + newline
+newline
+"<li>Alpha, a" + newline
+"<br/>Pronounciation, short A sound" + newline
+"<br/></li>" + newline
+newline
+"<li>Beta, b" + newline
+"<br/>Pronounciation, same as B in English text" + newline
+"<br/></li>" + newline
+newline
+"<li>Gamma, g" + newline
+"<br/>Pronounciation, Same as hard G sound in English" + newline
+"<br/></li>" + newline
+newline
+"<li>Delta, d" + newline
+"<br/>Pronounciation, D" + newline
+"<br/></li>" + newline
+newline
+"<li>Epsilon, e" + newline
+"<br/>Pronounciation, short E " + newline
+"<br/></li>" + newline
+newline
+"<li>Zeta, z" + newline
+"<br/>Pronounciation, Z" + newline
+"<br/></li>" + newline
+newline
+"<li>Eta, ay" + newline
+"<br/>Pronounciation, Long A sound" + newline
+"<br/>When the eta is at the beginning of a word, or stands alone as a definite article , it is spelled and pronounced, hay" + newline
+"<br/></li>" + newline
+newline
+"<li>Theta, th" + newline
+"<br/>Pronounciation, th" + newline
+"<br/></li>" + newline
+newline
+"<li>Iota, i" + newline
+"<br/>Pronounciation, short i" + newline
+"<br/></li>" + newline
+newline
+"<li>Kappa, k" + newline
+"<br/>Pronounciation, k" + newline
+"<br/></li>" + newline
+newline
+"<li>Lambda, l" + newline
+"<br/>Pronounciation, l" + newline
+"<br/></li>" + newline
+newline
+"<li>Mu, m" + newline
+"<br/>Pronounciation, m" + newline
+"<br/></li>" + newline
+newline
+"<li>Nu, n" + newline
+"<br/>Pronounciation, n" + newline
+"<br/></li>" + newline
+newline
+"<li>Xi, x" + newline
+"<br/>Pronounciation, x, as in English often has the sound of z" + newline
+"<br/></li>" + newline
+newline
+"<li>Omicron, o" + newline
+"<br/>Pronounciation, short o" + newline
+"<br/>When the omicron is used as a definite article it is spelled and pronounced, haw, or Haws" + newline
+"<br/></li>" + newline
+newline
+"<li>Pi, p" + newline
+"<br/>Pronounciation, p" + newline
+"<br/>Also, the name of this Greek letter is pronounced, P" + newline
+"<br/></li>" + newline
+newline
+"<li>Rho, r" + newline
+"<br/>Pronounciation, r" + newline
+"<br/></li>" + newline
+newline
+"<li>Sigma, s" + newline
+"<br/>Pronounciation, s" + newline
+"<br/></li>" + newline
+newline
+"<li>Tau, t" + newline
+"<br/>Pronounciation, t" + newline
+"<br/></li>" + newline
+newline
+"<li>Upsilon, u" + newline
+"<br/>Pronounciation, u" + newline
+"<br/></li>" + newline
+newline
+"<li>Phi, f" + newline
+"<br/>Pronounciation, f, or ph" + newline
+"<br/></li>" + newline
+newline
+"<li>Chi, ch" + newline
+"<br/>Pronounciation, ch" + newline
+"<br/>Also the Greek letter is pronounced, Key" + newline
+"<br/></li>" + newline
+newline
+"<li>Psi, ps" + newline
+"<br/>Pronounciation, ps" + newline
+"<br/></li>" + newline
+newline
+"<li>Omega, oh" + newline
+"<br/>Pronounciation, long o" + newline
+"<br/></li>" + newline
+"</ul>" + newline;
	
	}




	private String getBrailleAbout() {
	return
"<h3>Braille Structure</h3>"+newline
+"     <p>Each verse begins on a new line.  Arabic numerals in Braille code with traditional number ssign are used for chapter and verse numbers.  A colon, dots 2,5, is between chapter number and verse number and two spaces are before and after each reference.  Some texts, like the Tregelles texts, are also divided into paragraphs.  When a new verse begins a new paragraph, there are four blank spaces after the verse reference instead of two.  </p>"+newline
+newline
+"<h3>Symbols Used in These Files</h3>"+newline
+"<ul style=\"list-style: none;\">"+newline
+"<li>  =a alpha</li>"+newline
+"<li>  =b beta</li>"+newline
+"<li>  =g gamma</li>"+newline
+"<li>  =d delta</li>"+newline
+"<li>  =e epsilon</li>"+newline
+"<li>  =z zeta </li>"+newline
+"<li>  =: eta</li>"+newline
+"<li>  =? theta </li>"+newline
+"<li>  =i iota </li>"+newline
+"<li>  =k kappa</li>"+newline
+"<li>  =l lamda </li>"+newline
+"<li>  =m mu</li>"+newline
+"<li>  =n nu</li>"+newline
+"<li>  =x xi </li>"+newline
+"<li>  =o omicron</li>"+newline
+"<li>  =p pi </li>"+newline
+"<li>  =r rho</li>"+newline
+"<li>  =s sigma</li>"+newline
+"<li>  =t tau</li>"+newline
+"<li>  =u upsilon</li>"+newline
+"<li>  =f phi</li>"+newline
+"<li>  =&amp; chi</li>"+newline
+"<li>  =y psi</li>"+newline
+"<li>  =w omega</li>"+newline
+"</ul>"+newline
+newline
+"<h3>Accented vowel chart</h3>"+newline
+newline
+"<p>First appears the vowel symbol, unaccented, and then, in order, the vowel with acute, grave, and finally circumflex.  For the entries where the vowel plus circumflex does not exist, the last entry is blank.  One entry appears per line.</p>"+newline
+newline
+"<ul style=\"list-style: none;\">"+newline
+"<li>  a &gt; ( * </li>"+newline
+"<li>  e $ c </li>"+newline
+"<li>  : = ! &lt; </li>"+newline
+"<li>  i ] / %</li>"+newline
+"<li>  o [ + ^</li>"+newline
+"<li>  u \\ ) v</li>"+newline
+"<li>  w j q #</li>"+newline
+"</ul>"+newline
+newline
+"     <p>Note that ^ for O-circumflex is not part of traditional Braille Greek coding, as O-circumflex is generally not used.  Here, it only occurs in Matthew 24:45 in the Greek Orthodox text.  </p>"+newline
+newline
+"<h3>Extra symbols </h3>"+newline
+"<ul style=\"list-style: none;\">"+newline
+"<li>  =9 iota subscript </li>"+newline
+"<li>  =h rough breather (before the vowel it stands with)  </li>"+newline
+"<li>  =0 smooth breather (before the vowel it stands with) </li>"+newline
+"<li>  =@ diaresis or trema, before vowel it belongs to.   </li>"+newline
+"<li>  =. capital sign </li>"+newline
+"</ul>"+newline
+"<h3> Punctuation Marks </h3>"+newline
+"<ul style=\"list-style: none;\">"+newline
+"<li>  =4 period </li>"+newline
+"<li>  =2 question mark </li>"+newline
+"<li>  =1 comma </li>"+newline
+"<li>  =\" colon </li>"+newline
+"<li>  =,7 left bracket </li>"+newline
+"<li>  =7' right bracket </li>"+newline
+"</ul>"+newline;
	}
}
