
#TODO: textutil -convert rtf Genesis/ch*.html

.PHONY: compile
compile:
	ant

.PHONY: help
help:
	#echo "There is no default make rule."
	echo "Targets:"
	echo "  compile"
	echo "  generate (takes the input text and makes the output files in output/)"
	echo "  zip (create zip files from output dir)"
	echo "  clean (delete output dir)"
	echo "  cleanzip (clean and delete zip dir)"

.PHONY: zip
zip:
	mkdir -p zip
	#find ./src/ -maxdepth 1 -type f | xargs zip zip/scripts.zip
	#cd output; zip -r ../zip/bible.zip bible/
	cd output/production/collections; for collection in `ls -d * | sed 's/\///g'`; do zip -r ../../../zip/$$collection.zip $$collection; done
	cd output/production/individual; for bible in `ls -d * | sed 's/\///g'`; do zip -r ../../../zip/$$bible.zip $$bible; done


.PHONY: all_msg
all_msg:
	@echo "Note: 'make all' does not download.  That must be done explicitly."
	@sleep 5

.PHONY: generate
generate: compile
	./Run -p

.PHONY: all
all: all_msg generate zip

.PHONY: clean
clean:
	rm -rf output/*
	rm -rf classes/

.PHONY: cleanzip
cleanzip:
	rm -rf zip/*

.PHONY: cleanall
cleanall: clean cleanzip
