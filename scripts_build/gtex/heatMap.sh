#!/bin/sh -e 

rm -vf gtex.ids || true
touch gtex.ids

for tissue in Pancreas Skeletal Subcutaneous Liver Visceral
do

	tis=`echo $tissue | tr " -" "__" | tr -s "_" | tr "[a-z]" "[A-Z]"`
	# echo $tissue $tis

	cat $HOME/snpEff/db/GTEx/gtex_ids.txt \
		| cut -f 1,4,7 \
		| grep -i $tissue \
		>> gtex.ids

done

# Create comma separated lists
cat gtex.ids | cut -f 1 | grep -v "$^" | tr "\n" "," > gtexIds.comma.txt
cat GPCRs.txt | grep -v "$^" | tr "\n" "," > genes.txt

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

# Invoke R script
Rscript heatMap.r
