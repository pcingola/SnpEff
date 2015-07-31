#!/bin/sh -e

# WARNING: This has to be updated each time we download
FLYBASE_RELEASE="FB2015_01"
GENOMES="dana_r1.04 dere_r1.04 dgri_r1.3 dmel_r6.04 dmoj_r1.3 dper_r1.3 dpse_r3.03 dsec_r1.3 dsim_r2.01 dvir_r1.2 dwil_r1.3 dyak_r1.04"

WGET="wget -N"

CONFIG=`pwd`"/snpEff.$FLYBASE_RELEASE.config"
echo Creating config file $CONFIG

mkdir -p download || true
cd download

#---
# Download from FlyBase
#---

# Download GTF files (annotations)
rm -vf $config
for g in $GENOMES
do
	ORGANISM=`echo $g | cut -f 1 -d "_"`
	VERSION=`echo $g | cut -f 2 -d "_"`
	URL="ftp://ftp.flybase.net/releases/$FLYBASE_RELEASE/$g/gff/$ORGANISM-all-$VERSION.gff.gz"
	echo Organism: $ORGANISM  Version: $VERSION  URL: $URL

	mkdir -p $FLYBASE_RELEASE/$g || true
	$WGET -O $FLYBASE_RELEASE/$g/genes.gff.gz "$URL"

	# Update 'config' file
	echo "$g.genome : $g" >> $CONFIG
	echo "$g.reference : $URL" >> $CONFIG
	echo "" >> $CONFIG
done

# Copy to 'data' dir
cp -rvf $FLYBASE_RELEASE/* ../data/

echo "Done!"

