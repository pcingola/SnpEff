#!/bin/sh

base="dbNSFP2.4"
db="$base.txt"

echo Create file $db
head -n 1 $base\_variant.chr1 > $db

cat $base\_variant.chr1 \
		$base\_variant.chr2 \
		$base\_variant.chr3 \
		$base\_variant.chr4 \
		$base\_variant.chr5 \
		$base\_variant.chr6 \
		$base\_variant.chr7 \
		$base\_variant.chr8 \
		$base\_variant.chr9 \
		$base\_variant.chr10 \
		$base\_variant.chr11 \
		$base\_variant.chr12 \
		$base\_variant.chr13 \
		$base\_variant.chr14 \
		$base\_variant.chr15 \
		$base\_variant.chr16 \
		$base\_variant.chr17 \
		$base\_variant.chr18 \
		$base\_variant.chr19 \
		$base\_variant.chr20 \
		$base\_variant.chr21 \
		$base\_variant.chr22 \
		$base\_variant.chrX \
		$base\_variant.chrY \
	| grep -v "^#" \
	>> $db

echo BGZIP $db
bgzip $db

echo TABIX $db.gz
tabix -s 1 -b 2 -e 2 $db.gz

