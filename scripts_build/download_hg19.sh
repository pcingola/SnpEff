#!/bin/sh -e

#-------------------------------------------------------------------------------
#
# Download hg19 annotations
#
# Script created by Sarmady, Mahdi
#
#	If you remember, a while ago I asked if you can add transcript versions 
#	to RefSeq annotations. I found a way to add them without having you change 
#	anything. Today I basically downloaded (from ucsc) a modified version of 
#	refGene table with transcript versions concatenated to their names (by 
#	getting version numbers from gbCdnaInfo table as  described here 
#	http://www.biostars.org/p/52066/ ). I tested it with a huge vcf and it 
#	works flawlessly.
#
#-------------------------------------------------------------------------------

REF=hg19

mkdir -p data/$REF || true
cd data/$REF/

#---
# Download latest datasets
#---

# Genome sequence
wget -nc http://hgdownload.cse.ucsc.edu/goldenPath/$REF/bigZips/chromFa.tar.gz

# Protein sequences
wget -nc ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/mRNA_Prot/human.protein.faa.gz

# CDS sequences
wget -nc ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/mRNA_Prot/human.rna.fna.gz

# RefLink
wget -nc http://hgdownload.cse.ucsc.edu/goldenPath/$REF/database/refLink.txt.gz
gunzip -f refLink.txt.gz

#---
# Create FASTA file
#---
rm -rvf chr
mkdir chr 
cd chr
tar -xvzf ../chromFa.tar.gz

FASTA=../$REF.fa
echo Creating FASTA file
rm -vf $FASTA
cat chr[1-9].fa    >> $FASTA
cat chr??.fa       >> $FASTA
cat chr[A-Z].fa    >> $FASTA
cat chr???*.fa     >> $FASTA

cd -

# Compress genome file
$HOME/tools/pigz/pigz -f hg19.fa
cp hg19.fa.gz $HOME/snpEff/data/genomes/

#---
# Download Gene information
#---

echo "Query MySql database"

(
echo "use hg19;"
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
) | mysql --user=genome --host=genome-mysql.cse.ucsc.edu -A hg19 > genes.refseq

# Compress file
gzip -f genes.refseq

#---
# Create CDS and protein files
#---

# Protein fasta
zcat human.protein.faa.gz \
	| ../../scripts_build/hg19_proteinFasta2NM.pl refLink.txt \
	| ../../scripts_build/hg19_proteinFastaReplaceName.pl genes.refseq.gz \
	> protein.fa
gzip -f protein.fa

# CDS fasta
zcat human.rna.fna.gz | sed "s/^>gi|[0-9]*|ref|\(.*\)|.*/>\1/" > cds.fa 
gzip -f cds.fa

