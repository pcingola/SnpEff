#!/bin/sh -e 

# Genes to use
#cat GPCRs.txt | grep -v "$^" | tr "\n" "," > genes.txt
cat t2d_genes.txt | grep -v "$^" | tr "\n" "," | tr -d " \t"  > genes.txt

#---
# Prepare GTEx IDs file
#---
rm -vf gtex.ids || true
touch gtex.ids

for tissue in Brain Liver Pancreas Skeletal Subcutaneous Visceral
do

	tis=`echo $tissue | tr " -" "__" | tr -s "_" | tr "[a-z]" "[A-Z]"`
	echo $tissue $tis

	cat $HOME/snpEff/db/GTEx/gtex_ids.txt \
		| cut -f 1,4,7 \
		| grep -i $tissue \
		>> gtex.ids
done

# Create comma separated list of IDs
cat gtex.ids | cut -f 1 | grep -v "$^" | tr "\n" "," > gtexIds.comma.txt

cat gtex.ids \
	| cut -f 3 \
	| grep -v "$^" \
	| tr " " "_" \
	| tr "-" "_" \
	| tr -s "_" \
	| tr "\n" "," \
	| tr -cd "[a-z][A-Z][0-9],._" \
	> gtexLabels.comma.txt

# Select genes with expression (at least minValCount of the experiments)
./heatMap.py gtex_norm.txt `cat gtexIds.comma.txt` `cat gtexLabels.comma.txt` `cat genes.txt` > heatMap.txt

# Sort file by name
head -n 1 heatMap.txt > header.txt
grep -v -f header.txt heatMap.txt | sort > tmp.txt
cat header.txt tmp.txt > heatMap.txt

#---
# Invoke R script
#---
Rscript heatMap.r
Rscript boxPlots.r
