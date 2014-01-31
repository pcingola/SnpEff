#!/bin/sh -e

#---
# Create ref sequence
#---

echo "Creating reference fasta"
rm -vf $HOME/snpEff/data/genomes/canFam3.103.fa

for fa in $HOME/snpEff/data/genomes/canFam3.103/chr*.fa
do
	chr=`basename $fa .fa`
	echo "    Chromosome $chr"

	cat $fa \
		| sed "s/^>gi|[0-9]*|ref|\(.*\)|.*/>\1/" \
		>> $HOME/snpEff/data/genomes/canFam3.103.fa
done

#---
echo "Build GFF file"
#---

zcat ORI/ref_CanFam3.1_top_level.gff3.gz \
	| ./fixGff.pl \
	> ./genes.gff

#---
echo "Build protein file"
#---

cat genes.gff \
	| grep CDS \
	| cut -f 9 \
	| tr ";" "\t" \
	| cut -f 1,2 \
	| grep Parent \
	| uniq \
	| tr "=" "\t" \
	| cut -f 2,4 \
	> cdsId_trId.txt

#---
#---

zcat ORI/protein.fa.gz \
	| sed "s/^>gi|[0-9]*|ref|\(.*\..*\)|.*/>\1/" \
	| ./cdsId_trId.pl \
	> protein.fa

#---
echo "Build SnpEff database"
#---

cd $HOME/snpEff
java -Xmx4G -jar snpEff.jar build -v -gff3 canFam3.103 2>&1 | tee canFam3.103.build
java -Xmx4G -jar snpEff.jar dump canFam3.103 > canFam3.103.dump

