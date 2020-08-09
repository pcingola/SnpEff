#!/bin/bash -eu
set -o pipefail

# Scripts dir
scripts=$(cd $(dirname "$0"); pwd -P)
echo "0='$0'"
echo "SCRIPTS='$scripts'"

# Change this 
version="4.1a"

# Go to dbNSFP dir
cd $HOME/snpEff/db/GRCh38/dbNSFP

# File names
prefix_input="dbNSFP${version}_variant.chr"
prefix_split="dbNSFP.split"
output="dbNSFP${version}.sort_hg19.txt"

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
