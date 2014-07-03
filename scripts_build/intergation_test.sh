#!/bin/bash

curl -v -L http://sourceforge.net/projects/snpeff/files/protocols.zip > protocols.zip

echo "Annotating with dbSNP"
java -Xmx4g -jar SnpSift.jar annotate -dbsnp integration_1.vcf > integration_1.dnsnp.vcf

echo "Annotating with ClinVar"
java -Xmx4g -jar SnpSift.jar annotate -clinvar integration_1.dnsnp.vcf > integration_1.clinvar.vcf

echo "Annotating with dnNSFP"
java -Xmx4g -jar SnpSift.jar dbNSFP integration_1.clinvar.vcf > integration_1.dbnsfp.vcf

echo "Annotating genomic effects"
java -Xmx4g -jar snpEff.jar -v -lof -motif -nextProt GRCh37.75 integration_1.dbnsfp.vcf > integration_1.eff.vcf

