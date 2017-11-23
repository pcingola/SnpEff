#!/bin/sh

EXT="ori3.vcf"

#GENOME="hg19"
GENOME="testHg19Chr7"
# pos: 117174417

rm -vf splice*ann*.vcf

for vcf in splice*.$EXT
do
	base=`basename $vcf .$EXT`
	vcfBare="$base.bare.vcf"
	ann="$base.bare.ann.vcf"
	annsort="$base.bare.ann.sort.vcf"
	echo "vcf     :$vcf"
	echo "vcfBare :$vcfBare"
	echo "annsort :$annsort"

	cat $vcf | ./scripts/vcfBareBones.pl > $vcfBare
	echo "Number of lines in $vcfBare:" `wc -l $vcfBare`
	java -Xmx6G -jar snpEff.jar $GENOME $vcf > $ann
	cat $ann | grep -v "^#" | sort > $annsort

	echo
	echo
done

diff="splice.ann.sort."`date +"%Y%m%d-%H%M%S"`".diff"
diff splice*.ann.sort.vcf > $diff
grep 117174417 $diff

