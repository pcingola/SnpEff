#!/bin/bash -e

# Change this 
version="3.5c"

# File names
prefix_input="dbNSFP${version}_variant.chr"
prefix_split="dbNSFP.split"
output="dbNSFP${version}.sort_hg19.txt"

# Scripts dir
scripts=`dirname "$0"`

echo "Splitting input file '$input' by chromosome"
cat $prefix_input* \
	| $scripts/dbNSFP_split_by_chr.pl 7 8 \

# Create sorted output file
echo "Creating output file: '$output'"
rm -vf "$output"

for f in `ls $prefix_split.*.txt`
do
	echo "    Sorting file '$f'"
	cat $f \
		| $scripts/dbNSFP_sort.pl 0 1 \
		>> "$output"
done
