#!/bin/sh

# VCF="1kg/ALL.wgs.phase1.projectConsensus.snps.sites.vcf"
VCF="1kg/ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites.vcf"
VCF="1kg/1kgchr1.vcf"

REF=GRCh37.66
REF=hg19

EFF=`dirname $VCF`/`basename $VCF .vcf`.eff.vcf

#---
# Get & unzip file
#---

#cd 1kg

#wget "ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20101123/interim_phase1_release/ALL.wgs.phase1.projectConsensus.snps.sites.vcf.gz"
#gunzip ALL.wgs.phase1.projectConsensus.snps.sites.vcf.gz 

#wget ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20110521/ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites.vcf.gz
#$HOME/tools/pigz/pigz -d ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites.vcf.gz

#cd -

#---
# SNP analysis
# Note: The are 41.6 millon variants
#	$ wc -l 1kg/ALL.wgs.phase1.projectConsensus.snps.sites.vcf
#	41599494 1kg/ALL.wgs.phase1.projectConsensus.snps.sites.vcf
# Time:
#	- VCF input and VCF output: 81m30.039s
#	- VCF input and no output : 77m18.157s
#---

time java -Xmx4G -jar snpEff.jar eff \
	-v \
	-stats $VCF.html \
	$REF \
	$VCF \
	> $EFF

#echo "TXT output"
#time ./scripts/snpEffXL.sh \
#	-v \
#	-useLocalTemplate \
#	-stats $VCF.html \
#	$REF \
#	$VCF \
#	> $EFF.txt

#---
#	Multi-threaded version 
#---
#echo "VCF output (multi-thread)"
#time ./scripts/snpEffXL.sh \
#	-v \
#	-t \
#	$REF \
#	$VCF \
#	> $EFF

