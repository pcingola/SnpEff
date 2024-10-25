#!/bin/sh -e

SQLITE=$HOME/tools/sqlite3
SQLITE=sqlite3
OUT_FILE="sift.txt"
OUT_SORT_FILE="sift.sort.txt"
OUT_VCF="sift.vcf"

# Download files
for f in Human_CHR1.sqlite.gz Human_CHR10.sqlite.gz Human_CHR11.sqlite.gz Human_CHR12.sqlite.gz Human_CHR13.sqlite.gz Human_CHR14.sqlite.gz Human_CHR15.sqlite.gz Human_CHR16.sqlite.gz Human_CHR17.sqlite.gz Human_CHR18.sqlite.gz Human_CHR19.sqlite.gz Human_CHR2.sqlite.gz Human_CHR20.sqlite.gz Human_CHR21.sqlite.gz Human_CHR22.sqlite.gz Human_CHR3.sqlite.gz Human_CHR4.sqlite.gz Human_CHR5.sqlite.gz Human_CHR6.sqlite.gz Human_CHR7.sqlite.gz Human_CHR8.sqlite.gz Human_CHR9.sqlite.gz Human_CHRX.sqlite.gz Human_CHRY.sqlite.gz Human_Supp.sqlite.gz Human_enst.sqlite.gz 
do
	url=ftp://ftp.jcvi.org/pub/data/sift/Human_db_37_ensembl_63/$f
	echo Getting file $url 
	#wget $url
done

# Unzip files
for f in Human_CHR*.sqlite.gz
do
	echo Decompressing file $f 
	#gunzip $f
done

# Dumping data
rm -f $OUT_FILE
for f in Human_CHR*.sqlite
do
	echo Dumping Database $f to $OUT_FILE

	TABLES=`$SQLITE $f ".tables" | tr "\n" "\t"`
	for t in $TABLES
	do
		echo "    Dumping Table $t"

		$SQLITE $f "select CHR, COORD1, NT1, NT2, SCORE, MEDIAN, SEQS_REP from $t where SCORE != '' AND NT1 != NT2;" \
			| tr "|" "\t" \
			| sed "s/^chr//" \
			>> $OUT_FILE
	done
done

echo Sorting file
sort -k 1 -n -k 2 -n -o $OUT_SORT_FILE $OUT_FILE

echo Creating VCF 
cat $OUT_SORT_FILE | sift2vcf.pl > $OUT_VCF

