#!/bin/sh

SNPEFF="java -Xmx2G -jar snpEff.jar"

#---
# Build "special" cases
#---
# Test cases hg37
$SNPEFF build -noLog -txt testCase

# Test cases 
$SNPEFF build -noLog -gff3 testLukas

# Test cases hg37.70
$SNPEFF build -noLog -gtf22 testHg3770Chr22
cp db/jaspar/pwms.bin data/testHg3770Chr22/

# Test case testHg19Chr1
snpeff build -noLog -refSeq testHg19Chr1

#---
# Buils all GTF 2.2
#---
for gen in testHg3761Chr15 testHg3761Chr16 testHg3763Chr1 testHg3763Chr20 testHg3763ChrY testHg3765Chr22 testHg3766Chr1 testHg3767Chr21Mt testHg3769Chr12 testHg3771Chr1 testHg3775Chr1 testENST00000268124
do
	echo
	echo
	echo Genome: $gen
	$SNPEFF build -noLog -gtf22 testHg3761Chr15
done


