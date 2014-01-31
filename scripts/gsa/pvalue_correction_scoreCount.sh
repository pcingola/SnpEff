#!/bin/sh

in="$1"
out="$2"

Rscript `dirname $0`/pvalue_correction_scoreCount.r "$in" "$out" -1
