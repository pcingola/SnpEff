#!/bin/sh

REF=GRCh37.66

IN_VCF=$HOME/snpEff/1kg/test.Broad_NS_SYN.vcf.gz
OUT_VCF=all.vcf

# Run SnpEff
./scripts/snpEffXL.sh eff -v -noStats -o vcf $REF $IN_VCF > $OUT_VCF

# Calculate number of lines
SILENT=`cat $OUT_VCF | grep "SILENT" | wc -l`
MISSENSE=`cat $OUT_VCF | grep "MISSENSE" | wc -l`
NONSENSE=`cat $OUT_VCF | grep "NONSENSE" | wc -l`
SILENT_AND_MISSENSE=`cat $OUT_VCF | grep "SILENT" | grep "MISSENSE" | wc -l`

PSILENT=`echo "100 * $SILENT/($SILENT + $MISSENSE + $NONSENSE ) " | bc -l`
PMISSENSE=`echo "100 * $MISSENSE/($SILENT + $MISSENSE + $NONSENSE ) " | bc -l`
PNONSENSE=`echo "100 * $NONSENSE/($SILENT + $MISSENSE + $NONSENSE ) " | bc -l`

echo -e "Silent              :\t$SILENT ($PSILENT %)" 
echo -e "Missense            :\t$MISSENSE ($PMISSENSE %)"
echo -e "Nonsense            :\t$NONSENSE ($PNONSENSE %)"
echo -e "Silent and missense :\t$SILENT_AND_MISSENSE"

