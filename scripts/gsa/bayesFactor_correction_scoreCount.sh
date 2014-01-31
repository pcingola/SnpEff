#!/bin/sh

in="$1"
out="$2"

Rscript `dirname $0`/bayesFactor_correction_scoreCount.r "$in" "$out" -1
