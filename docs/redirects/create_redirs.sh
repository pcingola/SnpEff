#!/bin/bash

for html in  \
		index_frame.html \
		SnpEff.html \
		download_donate.html \
		supportNewGenome_content.html \
		1kg.html \
		index.html \
		SnpSift.html \
		snpEff_summary.html \
		SnpEff_manual.html \
		about.html \
		protocol.html \
		SnpSift.version_4_0.html \
		supportRegulation_content.html \
		supportNewGenome.html \
		download.20171031.html \
		moreBs.html \
		SnpSift.version_4_1.html \
		manual_content.html \
		xiangyi_lu_donate.html \
		protocol/index.html \
		index_content.html \
		download_content.html \
		supportRegulation.html \
		SnpEff_faq.html \
		SnpSift_content.html \
		SnpEff_manual.version_4_0.html \
		SnpEff_manual.4_1.html \
		features_content.html \
		examples.html \
		download.html \
		moreBs_content.html \
		faq_content.html \
		features.html \
		manual.html \
		about_content.html \
		faq.html \
		examples_content.html
do
	echo '<meta http-equiv="refresh" content="0; URL=https://pcingola.github.io/SnpEff/" />' > "$html"
done
