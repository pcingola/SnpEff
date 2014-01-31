#!/bin/sh

# Download from NCBI

for num in 406684590 406684589 406684588 406684587 406684586 406684585 406684584 406684583 406684582 406684581 406684580 406684579 406684578 406684577 406684576 406684575 406684574 406684573 406684572 406684571 406684570 406684569 406684568 406684567 406684566 406684565 406684564
do
	wget -O genes.$num.gb "http://www.ncbi.nlm.nih.gov/sviewer/viewer.cgi?tool=portal&sendto=on&log\$=seqview&db=nuccore&dopt=gbwithparts&sort=&val=$num&extrafeat=976&maxplex=1"
done

# Create one file
cat \
	genes.406684590.gb \
	genes.406684589.gb \
	genes.406684588.gb \
	genes.406684587.gb \
	genes.406684586.gb \
	genes.406684585.gb \
	genes.406684584.gb \
	genes.406684583.gb \
	genes.406684582.gb \
	genes.406684581.gb \
	genes.406684580.gb \
	genes.406684579.gb \
	genes.406684578.gb \
	genes.406684577.gb \
	genes.406684576.gb \
	genes.406684575.gb \
	genes.406684574.gb \
	genes.406684573.gb \
	genes.406684572.gb \
	genes.406684571.gb \
	genes.406684570.gb \
	genes.406684569.gb \
	genes.406684568.gb \
	genes.406684567.gb \
	genes.406684566.gb \
	genes.406684565.gb \
	genes.406684564.gb
	> genes.gb

# Compress
gzip -v genes.*.gb genes.gb


