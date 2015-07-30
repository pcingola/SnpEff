#!/bin/sh -e

#-------------------------------------------------------------------------------
#
# Download hg19 annotations
#
# Script created by Sarmady, Mahdi
#
#	If you remember, a while ago I asked if you can add transcript versions 
#	to KnownGenes annotations. I found a way to add them without having you change 
#	anything. Today I basically downloaded (from ucsc) a modified version of 
#	refGene table with transcript versions concatenated to their names (by 
#	getting version numbers from gbCdnaInfo table as  described here 
#	http://www.biostars.org/p/52066/ ). I tested it with a huge vcf and it 
#	works flawlessly.
#
#-------------------------------------------------------------------------------

REF=hg19kg

mkdir -p data/$REF || true
cd data/$REF/

#---
# Use same FASTA as hg19
#---

cd $HOME/snpEff/data/genomes/
ln -s hg19.fa.gz $REF.fa.gz || true
cd -

#---
# Download Gene information (KnownGenes)
#---

echo "Query MySql database (KnownGenes)"

(
echo "use hg19;"
echo "select  kg.name as '#name'"
echo "		, kg.chrom"
echo "		, kg.strand"
echo "		, kg.txStart"
echo "		, kg.txEnd"
echo "		, kg.cdsStart"
echo "		, kg.cdsEnd"
echo "		, kg.exonCount"
echo "		, kg.exonStarts"
echo "		, kg.exonEnds"
echo "		, kg.proteinID"
echo "		, kg.alignID"
echo "	from knownGene kg"
echo ";"
) | mysql --user=genome --host=genome-mysql.cse.ucsc.edu -A hg19 > genes.kg

# Compress file
rm -f genes.kg.gz || true
gzip genes.kg

