#!/bin/sh

#-------------------------------------------------------------------------------
# Files
#-------------------------------------------------------------------------------

in=$1													# Input VCF file
eff=`dirname $in`/`basename $in .vcf`.snpeff.vcf		# SnpEff annotated VCF file
out=`dirname $in`/`basename $in .vcf`.gatk.vcf			# Output VCF file (annotated by GATK)

ref=$HOME/snpEff/data/genomes/hg19.fa					# Reference genome file
dict=`dirname $ref`/`basename $ref .fa`.dict			# Reference genome: Dictionary file

#-------------------------------------------------------------------------------
# Path to programs and libraries
#-------------------------------------------------------------------------------

gatk=$HOME/tools/gatk/GenomeAnalysisTK.jar
picard=$HOME/tools/picard/
snpeff=$HOME/snpEff/snpEff.jar

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Create genome index file
echo
echo "Indexing Genome reference FASTA file: $ref"
samtools faidx $ref
 
# Create dictionary
echo
echo "Creating Genome reference dictionary file: $dict"
java -jar $picard/CreateSequenceDictionary.jar R= $ref O= $dict
 
# Annotate
echo
echo "Annotate using SnpEff"
echo "    Input file  : $in"
echo "    Output file : $eff"
java -Xmx4G -jar $snpeff -c $HOME/snpEff/snpEff.config -v -o gatk hg19 $in > $eff

# Use GATK
echo
echo "Annotating using GATK's VariantAnnotator:"
echo "    Input file  : $in"
echo "    Output file : $out"
java -Xmx4g -jar $gatk \
	-T VariantAnnotator \
	-R $ref \
	-A SnpEff \
	--variant $in \
	--snpEffFile $eff \
	-L $in \
	-o $out

