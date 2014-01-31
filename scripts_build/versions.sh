#!/bin/sh

#-------------------------------------------------------------------------------
# Create versions file
#
#-------------------------------------------------------------------------------

VER="$HOME/workspace/SnpEff/html/versions.txt"

# SnpEff version number
SNPEFF=`java -jar snpEff.jar 2>&1 | grep "snpEff version" | cut -f 3,4,6 -d " " | cut -f 1 -d ")" | tr "[a-z]" "[A-Z]" `

# SnpSift version number
SNPSIFT=`java -jar SnpSift.jar 2>&1 | grep "SnpSift version" | cut -f 3,4,6 -d " " | cut -f 1 -d ")" | tr "[a-z]" "[A-Z]"`

# Create file
(
echo $SNPEFF "http://sourceforge.net/projects/snpeff/files/snpEff_latest_core.zip"
echo $SNPSIFT "http://sourceforge.net/projects/snpeff/files/snpEff_latest_core.zip"
) | tr " " "\t"  > $VER

# Show file
cat $VER
