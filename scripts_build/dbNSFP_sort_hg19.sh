#!/bin/sh

cat dbNSFP3.1a_variant.chr* \
	| $HOME/snpEff/scripts_build/dbNSFP_sort_hg19.pl \
	> dbNSFP3.1a_hg19.txt

