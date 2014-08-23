#!/bin/sh

#------------------------------------------------------------------------------
# Create a zip file for distribution
# Note: Only binary data is included (no raw gene info / genomes)
#
#                                      Pablo Cingolani 2010
#------------------------------------------------------------------------------

source `dirname $0`/config.sh

DIR=snpEff_$SNPEFF_VERSION
rm -rvf $DIR snpEff
mkdir $DIR

# Copy core files
cp -rvfL snpEff.config snpEff.jar SnpSift.jar examples galaxy scripts $DIR

# Change name to 'snpEff' (so that config file can be used out of the box)
mv $DIR snpEff

# Create 'core' zip file
ZIP="snpEff_v"$SNPEFF_VERSION"_core.zip"
rm -f $ZIP 2> /dev/null
zip -r $ZIP snpEff

# Create ZIP file for each database
for d in `ls data/*/snpEffectPredictor.bin | grep -v GCA_`
do
	DIR=`dirname $d`
	GEN=`basename $DIR`
	
	echo $GEN
	ZIP="snpEff_v"$SNPEFF_VERSION"_"$GEN".zip"
	zip -r $ZIP data/$GEN/*.bin
done

# Create bundles
cd data
for ver in 21 22
do
	../scripts_build/createBundles.pl $ver | tee ../config/snpEff.bundles_$ver.config 
done
mv *.zip ../
cd ..

# Look for missing genomes
echo Missing genomes:
java -jar snpEff.jar databases | cut -f 1-3 | tr -s " " | grep -v "OK $" | sort | tee databases.missing.txt 

