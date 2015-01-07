#!/bin/sh -e

wget="wget --wait=1 -r -nc -e robots=off "

mkdir download/ncbi || true
cd download/ncbi

#---
# Download from NCBI (Bacterial genoms)
#---

$wget -A "gbk" "ftp://ftp.ncbi.nih.gov/genomes/Bacteria/"

#---
# Create directory structure
#---

# Move all downloaded file to this directory
mv ftp.ncbi.nih.gov/genomes/Bacteria/* .
rmdir ftp.ncbi.nih.gov/genomes/Bacteria
rmdir ftp.ncbi.nih.gov/genomes
rmdir ftp.ncbi.nih.gov

confFile="../../snpEff.NCBI_bacterial.config"
echo Creating config file $confFile
rm -vf $confFile || true

for dir in `find . -mindepth 1 -maxdepth 1 -type d `
do
	# Config file entries
	gen=`basename $dir`
	echo -e "$gen.genome : $gen\n$gen.reference : http://ftp.ncbi.nih.gov/genomes/Bacteria/\n" | tee -a $confFile

	# Collapse all fine into one 
	cd $dir
	rm -f genes.gbk
	cat *.gbk > genes.gbk
	cd - > /dev/null
done

echo Copying to snpEff/data
cp -rvf `find . -mindepth 1 -maxdepth 1 -type d ` ../../data/
