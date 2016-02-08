#!/bin/sh

cat dbNSFP3.1a_variant.chr* \
	| ./dbNSFP_sort_hg19.pl \
	> dbNSFP3.1a_hg19.txt

