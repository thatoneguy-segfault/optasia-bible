#!/bin/bash
# The purpose of this script is to return non-zero on the first failure of tidy.
# But, it doesn't seem to do that correctly, so...

shopt -s globstar
set -e # exit on first failed command
cmd="tidy -modify -quiet -clean -bare -access"
output_file=clean_html.output
date | tee ${output_file}
for f in output/**/*.html; do
    echo ${cmd} ${f} | tee -a ${output_file}
    ${cmd} ${f} 2>&1 | tee -a ${output_file}
done

