#!/bin/sh

echo
echo "WARNING: This database contains too many errors (88% of proteins do not match!)"
echo

cat ORI/AphidBase_OGS2.1b_withCDS.gff3 > genes.gff3
echo "##FASTA" >> genes.gff3
cat ORI/assembly2_scaffolds.fasta | sed "s/^>gi|[0-9]*|gb|\(.*\)\..*|.*/>\1/" | grep -v "^$" >> genes.gff3

cp ORI/aphidbase_2.1b_mRNA.fasta cds.fa
cat ORI/aphidbase_2.1b_pep.fasta | sed "s/-PA$/-RA/" > protein.fa
