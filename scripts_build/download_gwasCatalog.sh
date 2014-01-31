#!/bin/sh -e

# Update GwasCatalog
cd db/gwasCatalog
wget http://www.genome.gov/admin/gwascatalog.txt
cd -

# Create VCF file
cat db/gwasCatalog/gwascatalog.txt | ./scripts_build/scripts_build/gwascatalog2vcf.pl > db/gwasCatalog/gwascatalog.vcf

# Annotate VCF file
java -Xmx4g -jar snpEff.jar eff -v GRCh37.71 db/gwasCatalog/gwascatalog.vcf > db/gwasCatalog/gwascatalog.eff.vcf
