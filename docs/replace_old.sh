touch 1kg.html
touch SnpEff.html
touch SnpEff_manual.html
touch SnpSift.html
touch SnpSift_content.html
touch about.html
touch about_content.html
touch download.html
touch download_content.html
touch examples.html
touch examples_content.html
touch faq.html
touch faq_content.html
touch features.html
touch features_content.html
touch index.html
touch index_content.html
touch index_frame.html
touch manual.html
touch manual_content.html
touch moreBs.html
touch moreBs_content.html
touch morebs.html
touch morebs_content.html
touch snpEff_summary.html
touch snpeff_citations.html
touch supportNewGenome.html
touch supportNewGenome_content.html
touch supportRegulation.html
touch supportRegulation_content.html


for h in SnpSift_content.html about_content.html download_content.html examples.html examples_content.html faq.html faq_content.html features_content.html index_content.html index_frame.html manual.html manual_content.html moreBs.html moreBs_content.html snpEff_summary.html supportNewGenome.html supportNewGenome_content.html supportRegulation.html supportRegulation_content.html 
do
	echo "<meta http-equiv=\"refresh\" content=\"0; url=https://pcingola.github.io/SnpEff\">" > $h
done

scp SnpSift_content.html about_content.html download_content.html examples.html examples_content.html faq.html faq_content.html features_content.html index_content.html index_frame.html manual.html manual_content.html moreBs.html moreBs_content.html snpEff_summary.html supportNewGenome.html supportNewGenome_content.html supportRegulation.html supportRegulation_content.html pcingola,snpeff@frs.sourceforge.net:htdocs/
