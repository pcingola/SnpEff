#!/bin/sh -e

#-------------------------------------------------------------------------------
#
# Download hg38 annotations
#
# Script based on Sarmady, Mahdi's version for hg19
#
#-------------------------------------------------------------------------------

REF=hg38

mkdir -p data/$REF || true
cd data/$REF/

mkdir ORI || true

#---
# Download latest datasets
#---

# Genome sequence
wget -nc http://hgdownload.cse.ucsc.edu/goldenPath/$REF/bigZips/$REF.fa.gz
mv $REF.fa.gz ../genomes/

# CDS sequences
wget -nc http://hgdownload.cse.ucsc.edu/goldenPath/hg38/bigZips/refMrna.fa.gz
mv refMrna.fa.gz ORI/

#---
# Download Gene information
#---

echo "Query MySql database"

(
echo "use hg38;"
echo "select rg.bin as '#bin'"
echo "		, CONCAT(rg.name,'.',gi.version) as 'name'"
echo "		, rg.chrom"
echo "		, rg.strand"
echo "		, rg.txStart"
echo "		, rg.txEnd"
echo "		, rg.cdsStart"
echo "		, rg.cdsEnd"
echo "		, rg.exonCount"
echo "		, rg.exonStarts"
echo "		, rg.exonEnds"
echo "		, rg.score"
echo "		, rg.name2"
echo "		, rg.cdsStartStat"
echo "		, rg.cdsEndStat"
echo "		, rg.exonFrames"
echo "	from refGene rg"
echo "	inner join gbCdnaInfo gi"
echo "		on rg.name=gi.acc"
echo ";"
) | mysql --user=genome --host=genome-mysql.cse.ucsc.edu -A hg38 > genes.refseq

# Compress file
gzip -f genes.refseq

#---
# Create CDS and protein files
#---

# CDS fasta
gunzip -c ORI/refMrna.fa.gz | tr " " "." > cds.fa
gzip -f cds.fa

