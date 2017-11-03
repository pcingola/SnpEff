#!/bin/sh

rm -vf splice*ann*.vcf

for vcf in splice*.ori.vcf
do
	base=`basename $vcf .ori.vcf`
	vcfBare="$base.vcf"
	ann="$base.ann.vcf"
	annsort="$base.ann.sort.vcf"
	echo $vcf $annsort

	cat $vcf | ./scripts/vcfBareBones.pl > $vcfBare
	java -Xmx6G -jar snpEff.jar hg19 $vcf > $ann
	cat $ann | grep -v "^#" | sort > $annsort
done

diff="splice.ann.sort.diff."`date +"%Y%m%d-%H%M%S"`
diff splice*.ann.sort.vcf | tee $diff

