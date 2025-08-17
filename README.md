# optasia-bible
Optasia Bible generation code

## License and Copyright
All input material remains under original License and Copyright.

Copyright 2007-2025 Optasia Ministry.

## Optasia Bible

This project takes Bible text from various sources and outputs them into a uniform format.  It is able to package all translations together, or package only a few, or each translation separately.

Changes to this code can change how the Optasia Bibles are displayed or linked together.

## Input material

The Bible input text is stored as a git submodule with private github protections to protect the copyright and license of the Bibles from public access.  The generation code of this project is not restricted.

## Dependancies

`java`, `ant`, unix-like environment (make, sed, zip, xargs, sh)

`tidy` if you want to clean up the html after the conversion.  Tidy is available on linux with `apt`.

## Usage

```
$ ./Run -h
generate [translations]
        If no translations are listed, OptasiaLibrary collection is processed with no crosslinks.
        -h|--help       : Show usage information
        -q|--query      : List the supported translations and collections.
        -t|--test       : Call the test function. -- for developer use only.
        -x|--crosslink  : <supported|generated|collection_name|none> what to crosslink the generated translations to  default='none'
        -g|--generate   : <collection_name|supported> Any command line arguments replace this list.  default='OptasiaLibrary'
        -n|--nthreads   : <number> Number of threads to run.  default='2'
        -p|--production : generate all supported translations without crosslink; then generate each collection with crosslink.  Overrides other generation arguments.
The following 36 translations are supported:
AMP, ASV, BHS, BHST, CEB, CEV, DARBY, DRA, ESV, GNT, GW, HCSB, JPS, KJ21, KJV, MSG, MSGverse, NAB, NASB, NCV, NIRV, NIV, NIV1984, NIVUK, NJB, NJBLink, NKJV, NLT, NRSV, NTGbrl, NTGsr, RSV, TNIV, WE, WYC, YLT

The following 6 translations are provided by the 'EarlyLanguageLibrary' Optasia Library
[BHS, BHST, JPS, NRSV, NTGbrl, NTGsr]
The following 16 translations are provided by the 'OptasiaLibrary' Optasia Library
[AMP, ASV, CEV, ESV, GNT, HCSB, KJV, MSG, NASB, NIRV, NIV, NIV1984, NKJV, NLT, NRSV, TNIV]
```

## Examples

##### Generate a single translation

```
./Run NIV

```
The html will be in `output/nonproduction/NIV/`

##### Generate three translations with crosslinks between them

```
./Run --crosslink generated AMP ASV BHS
```
This will crosslink between the generated translations.   Another crosslink option is "supported", which would generate the files with crosslinks for every supported translation, even though we are only generating these three.  The default crosslink option is "none".  The other crosslink option is the name of a translation collection, e.g. "EarlyLanguageLibrary" or "OptasiaLibrary".

##### Generate the Early Language Library collection

```
./Run --crosslink EarlyLanguageLibrary --generate EarlyLanguageLibrary
```

##### Create everything.  Get it ready to share.

```
./Run -p
make zip
```

The `-p` will run the program in what I call "production mode" which makes it generate everything.  Then, `make zip` will create all of the zip files and put them in `zip/`


