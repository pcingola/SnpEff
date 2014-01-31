#!/bin/sh -e

wget="wget --wait=1 -r -nc -e robots=off "

mkdir download/ncbi || true
cd download/ncbi

#---
# Download from NCBI (Bacterial genoms)
#---

$wget -A "gbk" "http://ftp.ncbi.nih.gov/genomes/Bacteria/"

#---
# Create directory structure
#---

# Move all downloaded file to this directory
mv ftp.ncbi.nih.gov/genomes/Bacteria/* .
rmdir ftp.ncbi.nih.gov/genomes/Bacteria
rmdir ftp.ncbi.nih.gov/genomes
rmdir ftp.ncbi.nih.gov

for dir in `find . -mindepth 1 -maxdepth 1 -type d `
do
	# Config file entries
	gen=`basename $dir`
	echo -e "$gen.genome : $gen\n" | tee -a ncbi_append.snpEff.config

	# Collapse all fine into one 
	cd $dir
	cat *.gbk > genes.gb
	cd - > /dev/null
done

