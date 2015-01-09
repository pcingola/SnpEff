#!/bin/sh

#-------------------------------------------------------------------------------
#
# Command lines for SnpEff's manua (examples)
#
#
#                                                                Pablo Cingolani
#-------------------------------------------------------------------------------

genome="GRCh37.75"
genome="testHg3775Chr1"			# Note: Sometimes we can use testHg3775Chr1 instead of GRCh37.75 ('testHg3775Chr1' only loads chr1 so it's faster)

# #---
# # Multiple annotations per variant examples
# #---
# 
# java -Xmx4g -jar snpEff.jar $genome examples/variants_1.vcf > examples/variants_1.ann.vcf
# 
# java -Xmx4g -jar snpEff.jar $genome examples/variants_2.vcf > examples/variants_2.ann.vcf
# 
# #---
# # Cancer examples
# #---
# 
# java -Xmx4g -jar snpEff.jar -v -cancer -cancerSamples examples/samples_cancer_one.txt $genome examples/cancer.vcf > examples/cancer.ann.vcf
# 
# java -Xmx4g -jar snpEff.jar -v -classic -cancer -cancerSamples examples/samples_cancer_one.txt $genome examples/cancer.vcf > examples/cancer.eff.vcf
# 
# java -Xmx4g -jar snpEff.jar -v -cancer $genome examples/cancer_pedigree.vcf > examples/cancer_pedigree.ann.vcf
# 
# #---
# # Encode example
# #---
# 
# # Create a directory for ENCODE files
# mkdir -p db/encode
# 
# # Download ENCODE experimental results (BigBed file)
# cd db/encode
# wget "http://ftp.ebi.ac.uk/pub/databases/ensembl/encode/integration_data_jan2011/byDataType/openchrom/jan2011/fdrPeaks/wgEncodeDukeDnase8988T.fdr01peaks.hg19.bb"
# cd -

# Annotate using ENCODE's data:
java -Xmx4g -jar snpEff.jar -v -interval db/encode/wgEncodeDukeDnase8988T.fdr01peaks.hg19.bb $genome test.vcf > test.ann.vcf

