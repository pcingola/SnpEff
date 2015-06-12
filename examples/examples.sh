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
genome22="testHg3775Chr22"		# Note: Sometimes we can use testHg3775Chr22 instead of GRCh37.75 ('testHg3775Chr22' only loads chr22 so it's faster)

#---
# Multiple annotations per variant examples
#---

# java -Xmx4g -jar snpEff.jar $genome examples/variants_1.vcf > examples/variants_1.ann.vcf
# 
# java -Xmx4g -jar snpEff.jar $genome examples/variants_2.vcf > examples/variants_2.ann.vcf

#---
# Cancer examples
#---

# java -Xmx4g -jar snpEff.jar -v -cancer -cancerSamples examples/samples_cancer_one.txt $genome examples/cancer.vcf > examples/cancer.ann.vcf
# 
# java -Xmx4g -jar snpEff.jar -v -classic -cancer -cancerSamples examples/samples_cancer_one.txt $genome examples/cancer.vcf > examples/cancer.eff.vcf
# 
# java -Xmx4g -jar snpEff.jar -v -cancer $genome examples/cancer_pedigree.vcf > examples/cancer_pedigree.ann.vcf

#---
# Regulatory variants
#---

# java -Xmx4g -jar snpEff.jar -v -reg HeLa-S3 -reg NHEK $genome examples/test.1KG.vcf > examples/test.1KG.ann_reg.vcf

#---
# Encode example
#---

# # Create a directory for ENCODE files
# mkdir -p db/encode
# 
# # Download ENCODE experimental results (BigBed file)
# cd db/encode
# wget "http://ftp.ebi.ac.uk/pub/databases/ensembl/encode/integration_data_jan2011/byDataType/openchrom/jan2011/fdrPeaks/wgEncodeDukeDnase8988T.fdr01peaks.hg19.bb"
# cd -
#
# # Annotate using ENCODE's data:
# java -Xmx4g -jar snpEff.jar -v -interval db/encode/wgEncodeDukeDnase8988T.fdr01peaks.hg19.bb $genome examples/test.1KG.vcf > examples/test.1KG.ann_encode.vcf

#---
# Annotation example
#---

# java -Xmx4g -jar snpEff.jar -v $genome22 examples/test.chr22.vcf > examples/test.chr22.ann.vcf

#---
# SnpSift Filter examples
#---

#java -jar SnpSift.jar filter "ANN[0].EFFECT = 'missense_variant'" examples/test.chr22.ann.vcf > examples/test.chr22.ann.filter_missense_first.vcf

#java -jar SnpSift.jar filter "ANN[*].EFFECT = 'missense_variant'" examples/test.chr22.ann.vcf > examples/test.chr22.ann.filter_missense_any.vcf

#java -jar SnpSift.jar filter "(ANN[*].EFFECT = 'missense_variant') && (ANN[*].GENE = 'TRMT2A')" examples/test.chr22.ann.vcf > examples/test.chr22.ann.filter_missense_any_TRMT2A.vcf

#java -jar SnpSift.jar filter "( GEN[HG00096].DS > 0.2 ) & ( GEN[HG00097].DS > 0.5 )" examples/1kg.head_chr1.vcf.gz > examples/1kg.head_chr1.filtered.vcf
#gzip examples/1kg.head_chr1.filtered.vcf

#---
# SnpSift extractFields examples
#---

#java -jar SnpSift.jar extractFields -s "," -e "." examples/test.chr22.ann.vcf CHROM POS REF ALT "ANN[*].EFFECT" "ANN[*].HGVS_P" > examples/test.chr22.ann.txt

#java -jar SnpSift.jar extractFields examples/test.chr22.ann.vcf CHROM POS REF ALT "ANN[*].EFFECT" > examples/test.chr22.ann.txt

#cat examples/test.chr22.ann.vcf \
#	| ./scripts/vcfEffOnePerLine.pl \
#	| java -jar SnpSift.jar extractFields - CHROM POS REF ALT "ANN[*].EFFECT" \
#	> examples/test.chr22.ann.one_per_line.txt

# java -jar SnpSift.jar extractFields examples/1kg.head_chr1.vcf.gz CHROM POS REF ALT "GEN[HG00096].DS" "GEN[HG00097].DS" #> examples/1kg.head_chr1.txt
