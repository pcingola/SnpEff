#!/bin/sh -e

#-------------------------------------------------------------------------------
#
# Download hg38 annotations
#
# Script created by Sarmady, Mahdi
#
#-------------------------------------------------------------------------------

REF=hg38kg

mkdir -p data/$REF || true
cd data/$REF/

#---
# Use same FASTA as hg38
#---

cd $HOME/snpEff/data/genomes/
ln -s hg38.fa.gz $REF.fa.gz || true
cd -

#---
# Download Gene information (KnownGenes)
#---

echo "Query MySql database (KnownGenes)"

(
echo "use hg38;"
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
) | mysql --user=genome --host=genome-mysql.cse.ucsc.edu -A hg38 > genes.kg

# Compress file
rm -f genes.kg.gz || true
gzip genes.kg

