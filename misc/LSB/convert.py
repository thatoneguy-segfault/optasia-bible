# python3
import re

def to_html(input_filename: str, output_filename: str) -> None:
    optasia_notice: str = """<p>Notice: This nondramatic literary work is provided to you by Optasia Ministry, Inc., a nonprofit organization incorporated under the laws of the State of Iowa for the purpose of providing copies of previously published, nondramatic literary work in accessible formats exclusively for use by persons who are blind as allowed by section 121, chapter 1 of title 17 of the United States Code, also known as the Chafee Amendment.</p>

<p>Any further reproduction or distribution in a format other than a specialized format is an infringement of copyright. Beneficiaries of Optasia Ministry, Inc., services are not authorized to further distribute this material to any other entity or person.</p>

<p>Under the Marakesh Treaty, persons in certain countries other than the United States may receive Optasia resources. The same conditions and limitations apply.</p>
"""

    with open (input_filename, "r") as infile, open(output_filename, "w") as outfile:
        outfile.write("<html><head><title>Legacy Standard Bible</title></head><body>\n")
        header: bool = False

        re_section_start = re.compile(r"<S[HBSNF]>")
        re_section_end = re.compile(r"</S[HBNSF]>")

        re_linestart = re.compile(r"^(?:<(?:A|C|V|P|PM)>)+{{\d+::\d+}}(\d+)<T>[+]?")
        # "4. "

        # <CC>{{18::13}}1<T><PN>“<RA><$R<RFN>18<RNC>13<RNV>1</RFN><RA>Job 12:9$RE>Behold, my eye has seen all {this}; <PO>My ear has heard and understood it
        re_linestart2 = re.compile(r"^<C[CP]>{{\d+::\d+}}(\d+)<T>")

        # <FA><N1><$F<FN>01<FNC>1<FNV>2</FN><N1>Or {by}$E>
        # and let them be for <N1><$F<FN>01<FNC>1<FNV>15</FN><N1>Or {luminaries}, {light-bearers}$E>lights in the
        # <N4><$F<FN>27<FNC>5<FNV>25</FN><N4>Or (and half-shekels) (sing: (peres)), from verb "to divide"$E>
        re_trans = re.compile(r"<N\d><[$]F<FN>\d+<FNC>\d+<FNV>\d+</FN><N\d>([^<>$]+)[$]E>")

        # According to <FA><NA><$F<FN>19<FNC>PSALM 22<FNV>Title</FN><NA>Lit (The Doe of the Dawn); possibly a title for a tune$E>Aijeleth
        # <NA><$F<FN>19<FNC>PSALM 42<FNV>Title</FN><NA>Possibly (Contemplative), (Didactic), (Skillful Psalm)$E>Mask
        # A <NB><$F<FN>19<FNC>PSALM 60<FNV>Title</FN><NB>Possibly (Epigrammatic Poem), (Atonem
        re_trans2 = re.compile(r"<N[A-Z]><[$]F<FN>19<FNC>PSALM \d+<FNV>Title</FN><N[A-Z0-9]>([^<>$]+)[$]E>")


        # <RA><$R<RFN>01<RNC>1<RNV>1</RFN><RA>Ps 102:25; Is 40:21; John 1:1, 2; Heb 1:10$RE>In the beginning <RB><$R<RFN>01<RNC>1<RNV>1</RFN><RB>Ps 89:11;
        re_ref = re.compile(r"<R[A-Z]><[$]R<RFN>\d+<RNC>\d+<RNV>\d+</RFN><R[A-Z]>([^<>$]+)[$]RE>")
        # replace opening & closing with a space

        re_smartquote = re.compile(r"[“”]")

        re_delete = re.compile(r"(<FA>|<P[NORM]>[+-]?|<RS>[-+]?|</RS>|<[HL]LL?[-+]?|<B>|</B>|<SHI>|</SHI>|<L[EB]>|</BR>|<\\>|</>)|<,>")

        for line in infile:
            if (not header) and re.fullmatch("\\$START\n", line):
                header = True
                continue
            elif header and re.fullmatch("END\\$\n", line):
                header = False
                outfile.write(optasia_notice)
                continue
            elif header:
                outfile.write("<br/>")
                line = re.sub(r"^[-*]+", ' ', line, count=1)
                line = re.sub(r"[-*]+\n", "\n", line, count=1)
                outfile.write(line)
                continue


            #print ("DAVE")
            line = line.replace("<BN>", "<h1>")
            line = line.replace("</BN>", "</h1>")

            line = line.replace("<CN>", "<h2>")
            line = line.replace("</CN>", "</h2>")

            #line = line.replace("<SH>", "<h3>")
            #line = line.replace("</SH>", "</h3>")

            line = re_section_start.sub("<h3>", line)
            line = re_section_end.sub("</h3>", line)

            line = re_section_end.sub("<h3>", line)



            # Verses
            if line.startswith("<P"):
                outfile.write("<br/>")
            line = re_linestart.sub("\\1. ", line, count=1)
            line = re_linestart2.sub("\\1. ", line, count=1)

            line = re_delete.sub('', line)

            # Different translations of a word
            line = re_trans.sub("{\\1 } ", line)
            line = re_trans2.sub("{\\1 } ", line)

            # Verse References
            line = re_ref.sub(" \\1 ", line)

            line = re_smartquote.sub('"', line)



            line = line.replace("{", "(")
            line = line.replace("}", ")")

            outfile.write(line)
        outfile.write("</body></html>\n")
        

def main() -> None:
    input_filename: str = "../../../optasia-bible-input/LegacyStandardBible/LSB.txt"
    output_filename: str = "LSB.html"
    to_html(input_filename, output_filename)
    print ("DONE")

main()
