#!/bin/sh

for BUNDLE in NCBI_bacterial ENSEMBL_BFMPP_21 ENSEMBL_BFMPP_22
do
	cat config/snpEff.$BUNDLE.config | grep "\.genome" | cut -f 1 -d : | sed "s/.genome\s*$//" > $BUNDLE.genomes.txt
	./scripts_build/createBundles.pl $BUNDLE.genomes.txt $BUNDLE | tee ./config/snpEff.bundles.$BUNDLE.config
done

./scripts_build/make_config.sh

