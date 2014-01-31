#!/bin/sh -e

# mkdir ORI
cd ORI

#---
echo "Download data"
#---

# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/GFF/ref_CanFam3.1_top_level.gff3.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/protein/protein.fa.gz

# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr1.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr10.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr11.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr12.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr13.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr14.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr15.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr16.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr17.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr18.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr19.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr2.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr20.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr21.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr22.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr23.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr24.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr25.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr26.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr27.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr28.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr29.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr3.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr30.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr31.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr32.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr33.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr34.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr35.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr36.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr37.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr38.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr4.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr5.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr6.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr7.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr8.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chr9.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chrMT.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_chrX.fa.gz	
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Canis_lupus_familiaris/Assembled_chromosomes/seq/cfa_ref_CanFam3.1_unplaced.fa.gz


#---
echo "Build reference FASTA file"
#---

# Create 'chr.sh' script to change chromosome names
echo "#!/bin/sh" > chr.sh
echo >> chr.sh
echo "cat \\" >> chr.sh

for fa in cfa_ref_CanFam3.1_chr*.fa.gz
do
	chr=`echo $fa | cut -f 2 -d . | cut -f 2 -d _`
	chrName=`zcat $fa | head -n 1 | sed "s/^>gi|[0-9]*|ref|\(.*\..*\)|.*/\1/"`
	echo "    | sed \"s/$chrName/$chr/g\" \\" >> chr.sh
done
chmod a+x chr.sh

zcat  \
		cfa_ref_CanFam3.1_chr1.fa.gz \
		cfa_ref_CanFam3.1_chr2.fa.gz \
		cfa_ref_CanFam3.1_chr3.fa.gz \
		cfa_ref_CanFam3.1_chr4.fa.gz \
		cfa_ref_CanFam3.1_chr5.fa.gz \
		cfa_ref_CanFam3.1_chr6.fa.gz \
		cfa_ref_CanFam3.1_chr7.fa.gz \
		cfa_ref_CanFam3.1_chr8.fa.gz \
		cfa_ref_CanFam3.1_chr9.fa.gz \
		cfa_ref_CanFam3.1_chr10.fa.gz \
		cfa_ref_CanFam3.1_chr11.fa.gz \
		cfa_ref_CanFam3.1_chr12.fa.gz \
		cfa_ref_CanFam3.1_chr13.fa.gz \
		cfa_ref_CanFam3.1_chr14.fa.gz \
		cfa_ref_CanFam3.1_chr15.fa.gz \
		cfa_ref_CanFam3.1_chr16.fa.gz \
		cfa_ref_CanFam3.1_chr17.fa.gz \
		cfa_ref_CanFam3.1_chr18.fa.gz \
		cfa_ref_CanFam3.1_chr19.fa.gz \
		cfa_ref_CanFam3.1_chr20.fa.gz \
		cfa_ref_CanFam3.1_chr21.fa.gz \
		cfa_ref_CanFam3.1_chr22.fa.gz \
		cfa_ref_CanFam3.1_chr23.fa.gz \
		cfa_ref_CanFam3.1_chr24.fa.gz \
		cfa_ref_CanFam3.1_chr25.fa.gz \
		cfa_ref_CanFam3.1_chr26.fa.gz \
		cfa_ref_CanFam3.1_chr27.fa.gz \
		cfa_ref_CanFam3.1_chr28.fa.gz \
		cfa_ref_CanFam3.1_chr29.fa.gz \
		cfa_ref_CanFam3.1_chr30.fa.gz \
		cfa_ref_CanFam3.1_chr31.fa.gz \
		cfa_ref_CanFam3.1_chr32.fa.gz \
		cfa_ref_CanFam3.1_chr33.fa.gz \
		cfa_ref_CanFam3.1_chr34.fa.gz \
		cfa_ref_CanFam3.1_chr35.fa.gz \
		cfa_ref_CanFam3.1_chr36.fa.gz \
		cfa_ref_CanFam3.1_chr37.fa.gz \
		cfa_ref_CanFam3.1_chr38.fa.gz \
		cfa_ref_CanFam3.1_chrMT.fa.gz \
		cfa_ref_CanFam3.1_chrX.fa.gz \
		cfa_ref_CanFam3.1_unplaced.fa.gz \
	| sed "s/^>gi|[0-9]*|ref|\(.*\..*\)|.*/>\1/" \
	| ./chr.sh \
	> ../../genomes/canFam3.1.fa

#---
echo "Build GFF file"
#---

zcat ref_CanFam3.1_top_level.gff3.gz \
	| ../fixGff.pl \
	| ./chr.sh \
	> ../genes.gff

cat ../genes.gff \
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
echo "Build protein file"
#---

zcat protein.fa.gz \
	| sed "s/^>gi|[0-9]*|ref|\(.*\..*\)|.*/>\1/" \
	| ../cdsId_trId.pl \
	> ../protein.fa

#---
echo "Build SnpEff database"
#---

cd ../../..
java -Xmx10G -jar snpEff.jar build -v -gff3 canFam3.1 2>&1 | tee canFam3.1.build
java -Xmx10G -jar snpEff.jar dump canFam3.1 > canFam3.1.dump

