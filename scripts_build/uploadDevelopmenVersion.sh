#!/bin/sh -e

#------------------------------------------------------------------------------
# Create a zip file for distribution
# Note: Only binary data is included (no raw gene info / genomes)
#
#                                      Pablo Cingolani 2010
#------------------------------------------------------------------------------

source `dirname $0`/config.sh

# Make JAR files
`dirname $0`/make.sh

# Create tmp dir
DIR=snpEff_$SNPEFF_VERSION
rm -rvf $DIR snpEff
mkdir $DIR

# Copy core files
cp -vf snpEff.config snpEff.jar SnpSift.jar snpeff $DIR
cp -rvf galaxy scripts $DIR

cd $DIR
rm -rvf `find . -name "CVS" -type d`
cd -

# Change name to 'snpEff' (so that config file can be used out of the box)
mv $DIR snpEff

# Create 'core' zip file
ZIP="snpEff_development.zip"
rm -f $ZIP 2> /dev/null
zip -r $ZIP snpEff

#---
# Upload to ZIP file
#---
# Core program
echo
echo "Upload $ZIP"
scp $ZIP pcingola,snpeff@frs.sourceforge.net:/home/frs/project/s/sn/snpeff/

