#!/bin/sh

cat dbNSFP3.2a_variant.chr* \
	| $HOME/snpEff/scripts_build/dbNSFP_sort.pl 7 8 \
	> dbNSFP3.1a_hg19.txt

