#!/bin/sh

# #---
# # QQ Plots by model (no PC correction)
# #---
# echo
# echo Allellic
# cat coevolution.genes.MAX.out | cut -f 2 | $HOME/snpEff/scripts/qqplot.pl qq/p_allellic
# cat coevolution.genes.MAX.out | cut -f 2- | sort -g | head
# 
# echo
# echo Dominant
# cat coevolution.genes.MAX.out | cut -f 3 | $HOME/snpEff/scripts/qqplot.pl qq/p_dominant
# cat coevolution.genes.MAX.out | cut -f 3- | sort -g | head
# 
# echo
# echo Recessive
# cat coevolution.genes.MAX.out | cut -f 4 | $HOME/snpEff/scripts/qqplot.pl qq/p_recessive
# cat coevolution.genes.MAX.out | cut -f 4- | sort -g | head
# 
# echo
# echo Trend
# cat coevolution.genes.MAX.out | cut -f 5 | $HOME/snpEff/scripts/qqplot.pl qq/p_trend
# cat coevolution.genes.MAX.out | cut -f 5- | sort -g | head
# 
# #---
# # QQ plot using logistic regression (PC correction) and minumum of non-corrected p-values
# #---
# for file in coevolution.???.logistic_*.txt
# do
# 	# Logistic regression values
# 	title=qq/`basename $file .txt`
# 	echo $title
# 	ls -al $file
# 	wc -l $file
# 
# 	cat $file \
# 		| cut -f 3 \
# 		| grep "^[0-9]" \
# 		| $HOME/snpEff/scripts/qqplot.pl $title
# 
# 
# 	# Fisher exact test values
# 	title=qq/fisher_`basename $file .txt`
# 
# 	cat $file \
# 		| cut -f 4 \
# 		| grep "^[0-9]" \
# 		| $HOME/snpEff/scripts/qqplot.pl $title
# done

#---
# Using one result per gene
#---

for file in coevolution.???.logistic_*.txt
do
 	title=qq/gene_`basename $file .txt`
 	echo $title

	cat $file \
		| ./onePvaluePerGene.py \
		| sort -g \
		| head

	cat $file \
		| ./onePvaluePerGene.py \
		| cut -f 1 \
		| $HOME/snpEff/scripts/qqplot.pl $title

done
