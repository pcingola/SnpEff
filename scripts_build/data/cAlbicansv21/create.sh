#!/bin/sh

# wget http://www.candidagenome.org/download/gff/C_albicans_SC5314/archive/C_albicans_SC5314_version_A21-s02-m09-r08_features_with_chromosome_sequences.gff.gz

zcat C_albicans_SC5314_version_A21-s02-m09-r08_features_with_chromosome_sequences.gff.gz \
	| sed "s/Ca21chr1_C_albicans_SC5314/chr1/g" \
	| sed "s/Ca21chrR_C_albicans_SC5314/chrR/g" \
	| sed "s/Ca21chr2_C_albicans_SC5314/chr2/g" \
	| sed "s/Ca21chr3_C_albicans_SC5314/chr3/g" \
	| sed "s/Ca21chr4_C_albicans_SC5314/chr4/g" \
	| sed "s/Ca21chr5_C_albicans_SC5314/chr5/g" \
	| sed "s/Ca21chr6_C_albicans_SC5314/chr6/g" \
	| sed "s/Ca21chr7_C_albicans_SC5314/chr7/g" \
	| sed "s/Ca19-mtDNA/chrMt/g" \
	> genes.gff



