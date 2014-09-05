#!/bin/sh

# # Create geneID-geneName map file
# zcat $HOME/snpEff/data/GRCh37.75/genes.gtf.gz \
# 	| cut -f 3- \
# 	| grep "^gene" \
# 	| cut -f 7 \
# 	| tr \" "\t" \
# 	| cut -f 2,4 \
# 	| sort \
# 	| uniq \
# 	> geneId_geneName.txt

# # Select interations from Reactome (only direct_complex interactions)
# cat ../reactome/interactions/homo_sapiens.interactions.txt \
# 	| grep -P "\tdirect_complex" \
# 	| cut -f 2,5 \
# 	| sed "s/ENSEMBL://g" \
# 	| sort \
# 	| uniq \
# 	> reactome.homo_sapiens.interactions.txt

# Select GTEx IDs that are related to pancreas
cat gtex_tissue.txt | grep Pancreas | cut -f 1 | tr "\n" "," > pancreas_ids.txt

# Combine GTEX + Reactome + BioGrid
./combineGtex.py \
	reactome.homo_sapiens.interactions.txt \
	$HOME/snpEff/epistasis/biogrid.human.uniq.txt \
	`cat pancreas_ids.txt` \
	gtex_norm.txt \

	
