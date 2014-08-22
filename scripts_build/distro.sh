#!/bin/sh

#------------------------------------------------------------------------------
# Create a zip file for distribution
# Note: Only binary data is included (no raw gene info / genomes)
#
#                                      Pablo Cingolani 2010
#------------------------------------------------------------------------------

source `dirname $0`/config.sh

DIR=snpEff_$SNPEFF_VERSION
rm -rvf $DIR snpEff
mkdir $DIR

# Copy core files
cp -rvfL snpEff.config snpEff.jar SnpSift.jar examples galaxy scripts $DIR

# Change name to 'snpEff' (so that config file can be used out of the box)
mv $DIR snpEff

# Create 'core' zip file
ZIP="snpEff_v"$SNPEFF_VERSION"_core.zip"
rm -f $ZIP 2> /dev/null
zip -r $ZIP snpEff

# Create ZIP file for each database
# for d in `ls data/Brachyspira_hyodysenteriae_WA1_uid59291/snpEffectPredictor.bin data/Candidatus_Hamiltonella_defensa_5AT__Acyrthosiphon_pisum__uid59289/snpEffectPredictor.bin data/Erwinia_amylovora_ATCC_49946_uid46943/snpEffectPredictor.bin  data/Gordonia_KTR9_uid174812/snpEffectPredictor.bin  data/Haemophilus_influenzae_R2866_uid161923/snpEffectPredictor.bin  data/Lactobacillus_salivarius_UCC118_uid58233/snpEffectPredictor.bin  data/NC_009089.1/snpEffectPredictor.bin data/NC_014033.1/snpEffectPredictor.bin data/Petromyzon_marinus_7.0.65/snpEffectPredictor.bin  data/Saccharopolyspora_erythraea_NRRL_2338_uid62947/snpEffectPredictor.bin data/Salmonella_enterica_serovar_Typhimurium_LT2_uid57799/snpEffectPredictor.bin data/Shewanella_piezotolerans_WP3_uid58745/snpEffectPredictor.bin data/Staphylococcus_aureus_04_02981_uid161969/snpEffectPredictor.bin data/Staphylococcus_aureus_CP000730_USA300_TCH1516/snpEffectPredictor.bin data/Streptococcus_pyogenes_M1_GAS_uid57845/snpEffectPredictor.bin data/Streptococcus_pyogenes_MGAS10270_uid58571/snpEffectPredictor.bin data/Streptococcus_pyogenes_MGAS10750_uid58575/snpEffectPredictor.bin data/Streptococcus_pyogenes_MGAS2096_uid58573/snpEffectPredictor.bin data/Streptococcus_pyogenes_MGAS5005_uid58337/snpEffectPredictor.bin data/Streptococcus_pyogenes_MGAS6180_uid58335/snpEffectPredictor.bin data/Streptococcus_pyogenes_MGAS9429_uid58569/snpEffectPredictor.bin ` 
for d in `ls data/*/snpEffectPredictor.bin`
do
	DIR=`dirname $d`
	GEN=`basename $DIR`
	
	echo $GEN
	ZIP="snpEff_v"$SNPEFF_VERSION"_"$GEN".zip"
	zip -r $ZIP data/$GEN/*.bin
done

# Look for missing genomes
echo Missing genomes:
java -jar snpEff.jar databases | cut -f 1-3 | tr -s " " | grep -v "OK $" | sort | tee databases.missing.txt 

