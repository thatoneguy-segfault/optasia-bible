
#TODO: textutil -convert rtf Genesis/ch*.html

.PHONY: compile
compile:
	ant

.PHONY: help
help:
	#echo "There is no default make rule."
	@echo "Targets:"
	@echo "  compile"
	@echo "  generate (takes the input text and makes the output files in output/)"
	@echo "  clean_html (run an html-cleaner program on our output)"
	@echo "  zip (create zip files from output dir)"
	@echo "  all (make everything)"
	@echo "  clean (delete output dir)"
	@echo "  cleanzip (clean and delete zip dir)"
	@echo "example: make clean cleanzip; make all"

.PHONY: zip
zip:
	mkdir -p zip
	#find ./src/ -maxdepth 1 -type f | xargs zip zip/scripts.zip
	#cd output; zip -r ../zip/bible.zip bible/
	cd output/production/collections && for collection in `ls -d * | sed 's/\///g'`; do zip -r ../../../zip/$$collection.zip $$collection; done
	cd output/production/individual && for bible in `ls -d * | sed 's/\///g'`; do zip -r ../../../zip/$$bible.zip $$bible; done


.PHONY: all_msg
all_msg:
	@echo "Note: 'make all' does not download.  That must be done explicitly."
	@sleep 5

.PHONY: generate
generate: compile
	./Run -p

.PHONY: all
all: all_msg generate clean_html verify_clean_html zip

.PHONY: verify_clean_html
verify_clean_html:
	grep "This document has errors that must be fixed before" clean_html.output >/dev/null && grep --no-group-separator -e "This document has errors that must be fixed before" -e "using HTML Tidy to generate a tidied up version" -e '^tidy' clean_html.output | grep -C1  "This document has errors that must be fixed before" && false
	@echo "tidy was able to clean the html"

.PHONY: clean_html
clean_html: clean_html.sh
	#find output/ -name '*.html' -type f -print -exec tidy -modify -quiet -clean -bare -access '{}' \+ 2>&1 | tee clean_html.output
	./clean_html.sh



.PHONY: clean
clean:
	rm -rf output/*
	rm -rf classes/
	rm -f clean_html.output

.PHONY: cleanzip
cleanzip:
	rm -rf zip/*

.PHONY: cleanall
cleanall: clean cleanzip
