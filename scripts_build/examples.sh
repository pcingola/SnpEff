#!/bin/sh

# Command lines from HTML examples
#
# Note: Sometimes we use testHg3775Chr1 instead of GRCh37.75 for speed ('testHg3775Chr1' only loads chr1 so it's faster)
#

# Multiple annotations per variant
java -Xmx4g -jar snpEff.jar testHg3775Chr1 examples/variants_1.vcf > examples/variants_1.ann.vcf

java -Xmx4g -jar snpEff.jar testHg3775Chr1 examples/variants_2.vcf > examples/variants_2.ann.vcf

# Cancer
java -Xmx4g -jar snpEff.jar -v -cancer -cancerSamples examples/samples_cancer_one.txt testHg3775Chr1 examples/cancer.vcf > examples/cancer.ann.vcf

java -Xmx4g -jar snpEff.jar -v -classic -cancer -cancerSamples examples/samples_cancer_one.txt testHg3775Chr1 examples/cancer.vcf > examples/cancer.eff.vcf

java -Xmx4g -jar snpEff.jar -v -cancer testHg3775Chr1 examples/cancer_pedigree.vcf > examples/cancer_pedigree.ann.vcf

