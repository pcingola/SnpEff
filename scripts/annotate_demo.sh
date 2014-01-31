#!/bin/sh -e

#-------------------------------------------------------------------------------
#
# Example on how to annotate human variants using SnpSift
# Note: It is assumed that the organism is "human"
#
#															Pablo Cingolani 2012
#-------------------------------------------------------------------------------

#---
# Parameters
#--- 
INPUT_VCF="test.vcf"                        # Variants file to annotate
REFERENCE="GRCh37.66"                       # Reference genome

MEM="4G"                                    # Amount of memory to use (make sure there is enough physical memory)
SNPSIFT="java -Xmx$MEM -jar SnpSift.jar"	# SnpSift command
SNPEFF="java -Xmx$MEM -jar snpEff.jar"	    # SnpEff command

VCF_BASE=`basename $INPUT_VCF .vcf`         # Name to use for intermediate files

#---
# Annotate ID fields using dbSnp
#
# Note: We annotate using dbSnp before using SnpEff in order to 
#       have 'known' and 'unknown' statistics in SnpEff's summary page.
#       Those stats are based on the presence of an ID field. If the ID 
#       is non-empty, then it is assumed to be a 'known variant'.
#---

# Download and uncompress dbSnp database.
# Note: Uncomment the following line if you need to download it.
#
#wget -O dbSnp.vcf.gz ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/VCF/v4.0/00-All.vcf.gz
#gunzip dbSnp.vcf.gz

$SNPSIFT annotate -v $DB_SNP $VCF > $VCF_BASE.dbSnp.vcf

#---
# Annotate variants using SnpEff
#---
$SNPEFF eff -v  $REFERENCE  $VCF.dbSnp.vcf  > $VCF_BASE.eff.vcf

# Here is an example using regulatory annotations.
# Note: You should customize which annotations you use depending on what makes sense for your project.

# $SNPEFF eff -v \
# 	-reg H1ESC \
# 	-reg HUVEC \
# 	-reg HepG2 \
# 	-reg IMR90 \
# 	-reg NHEK \
# 	-reg Adult_Liver \
# 	-reg Muscle_Satellite_Cultured_Cells \
# 	-reg Pancreatic_Islets \
# 	-reg Skeletal_Muscle \
# 	$REFERENCE \
# 	$VCF.dbSnp.vcf \
# 	> $VCF_BASE.eff.vcf

# At this point, you should have an HTML summary file.
echo "Take a look at the summary. Open snpEff_summary.html in your browser."

#---
# Annotate using GWAS catalog
#---

# Download GWAS catalog 
# Note: Uncomment the following lines if you need to download the database.
#
#wget http://www.genome.gov/admin/gwascatalog.txt

# Annotate
$SNPSIFT gwasCat -v gwascatalog.txt $VCF_BASE.eff.vcf > $VCF_BASE.gwas.vcf

#---
# Annotate using SIFT (Sorts Intolerant From Tolerant) score.
# Note: SIFT and SnpSift are two unrelated projects, but you can use SnpSift to annotate using SIFT databases (yes, I know it's confusing...)
#---

# Download database 
# Note: Uncomment the following lines if you need to download the database.
#
#wget http://sourceforge.net/projects/snpeff/files/databases/sift.vcf.gz
#gunzip sift.vcf.gz

# Annotate
$SNPSIFT sift $VCF_BASE.gwas.vcf > $VCF_BASE.sift.vcf

#---
# Other (optional) annotations
#---

# Hardy Weinberg 
$SNPSIFT hw $VCF_BASE.sift.vcf > $VCF_BASE.hw.vcf

# Variant types
$SNPSIFT varType $VCF_BASE.hw.vcf > $VCF_BASE.annotated.vcf

