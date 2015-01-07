#!/bin/sh

# Command lines from HTML examples

java -Xmx4g -jar snpEff.jar -v -cancer -cancerSamples examples/samples_cancer_one.txt GRCh37.75 examples/cancer.vcf > examples/cancer.ann.vcf

