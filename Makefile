
#TODO: textutil -convert rtf Genesis/ch*.html

.PHONY: compile
compile:
	ant

.PHONY: fail
fail:
	echo "There is no default make rule."
	echo "Targets:"
	echo "  clean (delete output dir)"
	echo "  distclean (clean and delete package dir)"
	echo "  package (create zip files from output dir)"

.PHONY: package
package:
	mkdir -p package
	find ./src/ -maxdepth 1 -type f | xargs zip package/scripts.zip
	#cd output; zip -r ../package/bible.zip bible/
	cd output/production/collections; for collection in `ls -d * | sed 's/\///g'`; do zip -r ../../../package/$$collection.zip $$collection; done
	cd output/production/individual; for bible in `ls -d * | sed 's/\///g'`; do zip -r ../../../package/$$bible.zip $$bible; done


.PHONY: all_msg
all_msg:
	@echo "Note: 'make all' does not download.  That must be done explicitly."
	@sleep 5

.PHONY: all
all: all_msg generate package

clean:
	rm -rf output/*
	rm -rf classes/

distclean: clean
	rm -rf package/*
