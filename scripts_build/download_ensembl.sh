#!/bin/sh -e

source `dirname $0`/config.sh

#mkdir download
cd download

#---
# Download from ENSEMBL
#---

# Download GTF files (annotations)
wget -r -A "*gtf.gz" "ftp://ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/gtf/"

# Download FASTA files (reference genomes)
wget -r -A "*dna.toplevel.fa.gz" "ftp://ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/fasta/"

# Download CDS sequences
wget -r -A "*cdna.all.fa.gz" "ftp://ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/fasta/"

# Download PROTEIN sequences
wget -r -A "*.pep.all.fa.gz" "ftp://ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/fasta/"

# Download regulation tracks
wget -r -A "*AnnotatedFeatures.gff.gz" "ftp://ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/regulation/"
wget -r -A "*MotifFeatures.gff.gz" "ftp://ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/regulation/"

#---
# Create directory structure
#---

# Move all GTF and FASTA downloaded files to this directory
mv `find ftp.ensembl.org -type f -iname "*gtf.gz" -or -iname "*.fa.gz"` .

# Gene annotations files
mkdir -p data/genomes
for gtf in *.gtf.gz
do
	short=`../scripts_build/file2GenomeName.pl $gtf | cut -f 5`
	base=`../scripts_build/file2GenomeName.pl $gtf | cut -f 7`
	echo ANNOTATIONS: $short

	fasta="$base.dna.toplevel.fa.gz"
	cds="$base.cdna.all.fa.gz"
	prot="$base.pep.all.fa.gz"

	mkdir -p data/$short
	cp $gtf data/$short/genes.gtf.gz
	cp $cds data/$short/cds.fa.gz
	cp $prot data/$short/protein.fa.gz
	cp $fasta data/genomes/$short.fa.gz
done
 
# Regulation tracks
mkdir -p data/$GRCH.$ENSEMBL_RELEASE/
cp ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/regulation/homo_sapiens/AnnotatedFeatures.gff.gz data/$GRCH.$ENSEMBL_RELEASE/regulation.gff.gz
cp ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/regulation/homo_sapiens/MotifFeatures.gff.gz data/$GRCH.$ENSEMBL_RELEASE/motif.gff.gz

mkdir -p data/$GRCM.$ENSEMBL_RELEASE/
cp ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/regulation/mus_musculus/AnnotatedFeatures.gff.gz data/$GRCM.$ENSEMBL_RELEASE/regulation.gff.gz
cp ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/regulation/mus_musculus/MotifFeatures.gff.gz data/$GRCM.$ENSEMBL_RELEASE/motif.gff.gz

#---
# Config file entries
#---

(
for fasta in *.gtf.gz
do
	genome=`../scripts_build/file2GenomeName.pl $fasta | cut -f 4`
	short=`../scripts_build/file2GenomeName.pl $fasta | cut -f 5`

	# Individual genome entry
	echo -e "$short.genome : $genome"
	echo -e "$short.reference : ftp://ftp.ensembl.org/pub/release-$ENSEMBL_RELEASE/gtf/"
	echo
done
) | tee ../config/snpEff.ENSEMBL_$ENSEMBL_RELEASE.config

#---
# Move data to 'data' dir
#---

cd -

echo Moving files to data dir
mv download/data/genomes/* data/genomes/
rmdir download/data/genomes
mv download/data/* data/

echo "Done!"

