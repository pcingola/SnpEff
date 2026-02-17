# Integration: GATK

SnpEff is integrated with other tools commonly used in sequencing data analysis pipelines.
Most notably Broad Institute's Genome Analysis Toolkit ([GATK](http://www.broadinstitute.org/gatk/)) supports SnpEff.
By using standards, such as VCF, SnpEff makes it easy to integrate with other programs.

### GATK compatibility mode

In order to make sure SnpEff and GATK understand each other, you can activate GATK compatibility in SnpEff by using the `-o gatk` command line option.

!!! warning
    The `-o gatk` option is a **legacy compatibility mode** designed for older versions of GATK (v2.x/v3.x). It forces SnpEff to use the old `EFF` annotation format (instead of the current `ANN` format) and disables several features:

    - Sequence Ontology terms (uses old-style effect names instead)
    - HGVS notation
    - NextProt annotations
    - Motif annotations
    - Splice region detection (splice region sizes set to zero)

    Additionally, GATK only picks the single highest-impact effect from the annotation, discarding the rest.
    You can get the full set of effects by running SnpEff independently, instead of using it within the GATK framework.

!!! info
    Modern GATK (v4.x) has a different architecture and no longer uses the `VariantAnnotator -A SnpEff` workflow described below. For GATK4 pipelines, it is generally recommended to run SnpEff independently (without `-o gatk`) and use the standard `ANN` format, which GATK4's `Funcotator` and other tools can handle.

Script example: In this example we combine SnpEff and GATK's VariantAnnotator (legacy GATK v2.x/v3.x workflow):
```
#!/bin/sh

#-------------------------------------------------------------------------------
# Files
#-------------------------------------------------------------------------------

in=$1                                                   # Input VCF file
eff=`dirname $in`/`basename $in .vcf`.ann.vcf           # SnpEff annotated VCF file
out=`dirname $in`/`basename $in .vcf`.gatk.vcf          # Output VCF file (annotated by GATK)

ref=$HOME/snpEff/data/genomes/hg19.fa                   # Reference genome file
dict=`dirname $ref`/`basename $ref .fa`.dict            # Reference genome: Dictionary file

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
samtools faidx $ref

# Create dictionary
java -jar $picard/CreateSequenceDictionary.jar R= $ref O= $dict

# Annotate using SnpEff (note the '-o gatk' flag for GATK compatibility)
java -Xmx8g -jar $snpeff -c $HOME/snpEff/snpEff.config -v -o gatk hg19 $in > $eff

# Use GATK's VariantAnnotator to transfer SnpEff annotations
java -Xmx8g -jar $gatk \
    -T VariantAnnotator \
    -R $ref \
    -A SnpEff \
    --variant $in \
    --snpEffFile $eff \
    -L $in \
    -o $out
```

!!! warning
    **Important:** In order for this to work, GATK requires that the Genome Reference file should have the chromosomes in karyotyping order
    (largest to smallest chromosomes, followed by the X, Y, and MT). Your VCF file should also respect that order.
