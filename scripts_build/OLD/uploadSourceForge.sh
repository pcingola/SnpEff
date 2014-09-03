#!/bin/sh

#-------------------------------------------------------------------------------
#
# Upload files to SourceForge web 
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

# Include variables
source `dirname $0`/config.sh

#---
# Upload to ZIP files and databases
#---
# Core program
echo
echo "Upload snpEff_latest_core.zip and snpEff_v${SNPEFF_VERSION}_core.zip"
cp snpEff_v${SNPEFF_VERSION}_core.zip snpEff_latest_core.zip
scp snpEff_v${SNPEFF_VERSION}_core.zip snpEff_latest_core.zip pcingola,snpeff@frs.sourceforge.net:/home/frs/project/s/sn/snpeff/

# Individual databases
echo
echo "Upload databases"
scp snpEff_v${SNPEFF_VERSION}_*.zip pcingola,snpeff@frs.sourceforge.net:/home/frs/project/s/sn/snpeff/databases/v${SNPEFF_VERSION}/

#---
# Update SnpEff web pages
#---

# Create versions file (html/versions.txt)
./scripts_build/versions.sh

# Upload HTML pages
cd $HOME/workspace/SnpEff/html/

# Copy html and txt files 
echo
echo "Upload web pages"
scp style.css *.html *.txt pcingola,snpeff@frs.sourceforge.net:htdocs/
		
# Copy images
echo
echo "Upload web pages images"
scp -r  images/ pcingola,snpeff@frs.sourceforge.net:htdocs/images/

