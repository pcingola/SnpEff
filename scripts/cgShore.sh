#!/bin/sh

# Download data
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/cpgIslandExt.txt.gz

# Annotate Shores & Shelves
gunzip -c cpgIslandExt.txt | cut -f 2- | ./cgShore.pl > cpgIslands_Shores_Shelf.bed
gzip cpgIslands_Shores_Shelf.bed
