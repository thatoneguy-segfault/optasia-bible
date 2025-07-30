
# this is a hack and ugly

LSB.html: convert.py
	mypy convert.py --strict
	python3 ./convert.py
	tidy -modify LSB.html

.PHONY: clean
clean:
	rm LSB.html
