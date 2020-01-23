#!/bin/sh -e

# These variables need to be updated with every version
base="dbNSFP4.0a"                                # Database version
dbZip="$base.zip"                               # Zip file name
db="$base.txt.gz"                                  # Output file

# Check dbNSFP
if [ ! -e "$dbZip" ]
then
	echo "ERROR: Expected dbNSFP zip file '$dbZip' not found"
	exit 1
fi

#---
# Create DB
#---
echo Create file $db

( gunzip -c $base\_variant.chr1.gz | head -n 1 || true ; 
	gunzip -c \
		$base\_variant.chr1.gz \
		$base\_variant.chr2.gz \
		$base\_variant.chr3.gz \
		$base\_variant.chr4.gz \
		$base\_variant.chr5.gz \
		$base\_variant.chr6.gz \
		$base\_variant.chr7.gz \
		$base\_variant.chr8.gz \
		$base\_variant.chr9.gz \
		$base\_variant.chr10.gz \
		$base\_variant.chr11.gz \
		$base\_variant.chr12.gz \
		$base\_variant.chr13.gz \
		$base\_variant.chr14.gz \
		$base\_variant.chr15.gz \
		$base\_variant.chr16.gz \
		$base\_variant.chr17.gz \
		$base\_variant.chr18.gz \
		$base\_variant.chr19.gz \
		$base\_variant.chr20.gz \
		$base\_variant.chr21.gz \
		$base\_variant.chr22.gz \
		$base\_variant.chrX.gz \
		$base\_variant.chrY.gz \
	| grep -v "^#" \
) | bgzip -c > $db

echo TABIX $db.gz
pwd
tabix -s 1 -b 2 -e 2 $db

