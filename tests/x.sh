#!/bin/sh


cat \
	| sed "s/Exon_/EXON_/g" \
	| sed "s/exon_/EXON_/g" \
	| sed "s/Transcript_/TRANSCRIPT_/g" \
	| sed "s/mRNA/protein_coding/g" \
