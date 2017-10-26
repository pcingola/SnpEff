#!/bin/sh

version="3.2a"

cat dbNSFP${version}_variant.chr* \
	| $HOME/snpEff/scripts_build/dbNSFP_sort.pl 7 8 \
	> dbNSFP${version}_hg19.txt

