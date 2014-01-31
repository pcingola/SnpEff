#!/bin/sh

# Create a file with genomes databases list
# Remove the first two lines (titles)
mkdir -p galaxy/tool-data
(
	echo "# SnpEff databases"
	echo "# List created using command: java -jar snpEff.jar databases"
	echo "# Version    Description"
	java -jar snpEff.jar databases \
		| tail -n +3 \
		| cut -f 1,2 \
		| awk '{ print $1, "\t", $2, " ", $1 }' \
		| sort -k 2
) > galaxy/tool-data/snpEff_genomes.loc

# Copy to 'sample'
cp galaxy/tool-data/snpEff_genomes.loc galaxy/tool-data/snpEff_genomes.loc.sample
