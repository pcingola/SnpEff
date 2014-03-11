#!/bin/sh

base="dbNSFP2.4"
db="$base.txt"

echo Create file $db
head -n 1 $base_variant.chr1 > $db

cat $base_variant.chr1 \
		$base_variant.chr2 \
		$base_variant.chr3 \
		$base_variant.chr4 \
		$base_variant.chr5 \
		$base_variant.chr6 \
		$base_variant.chr7 \
		$base_variant.chr8 \
		$base_variant.chr9 \
		$base_variant.chr10 \
		$base_variant.chr11 \
		$base_variant.chr12 \
		$base_variant.chr13 \
		$base_variant.chr14 \
		$base_variant.chr15 \
		$base_variant.chr16 \
		$base_variant.chr17 \
		$base_variant.chr18 \
		$base_variant.chr19 \
		$base_variant.chr20 \
		$base_variant.chr21 \
		$base_variant.chr22 \
		$base_variant.chrX \
		$base_variant.chrY \
	| grep -v "^#" \
	>> $db

echo BGZIP $db
bgzip $db

echo TABIX $db.gz
tabix -s 1 -b 2 -e 2 $db.gz

