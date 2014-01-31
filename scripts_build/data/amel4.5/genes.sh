#!/bin/sh -e

#---
# Download
#---
for chr in LG1 LG2 LG3 LG4 LG5 LG6 LG7 LG8 LG9 LG10 LG11 LG12 LG13 LG14 LG15 LG16 MT
do
	echo "Download chromosome $chr"

	# Download GFF file
	#wget "http://biomirror.aarnet.edu.au/biomirror/ncbigenomes/Apis_mellifera/special_requests/gff/ame_ref_Amel_4.5_chr$chr.gff3.gz"

	# Download FASTA files
	#wget "ftp://ftp.ncbi.nih.gov/genomes/Apis_mellifera/Assembled_chromosomes/seq/ame_ref_Amel_4.5_chr$chr.fa.gz"
done

#---
echo "Create one GFF3 file"
#---
touch genes.gff
rm -vf genes.gff
for chr in LG1 LG2 LG3 LG4 LG5 LG6 LG7 LG8 LG9 LG10 LG11 LG12 LG13 LG14 LG15 LG16 MT
do
	echo "    $chr"
	gunzip -c "ame_ref_Amel_4.5_chr$chr.gff3.gz" >> genes.gff
done

# Some entries do not have ID, remove those.
# Note: There are not so many (only 300 or so)
#	$ cat genes.ori.gff | grep -v ID= | wc -l 
#     322
mv genes.gff genes.ori.gff 
cat genes.ori.gff | grep ID= > genes.gff

#---
echo "Create one FASTA file"
#---
touch amel4.5.fa
rm -vf amel4.5.fa
for chr in LG1 LG2 LG3 LG4 LG5 LG6 LG7 LG8 LG9 LG10 LG11 LG12 LG13 LG14 LG15 LG16 MT
do
	echo "    $chr"
	gunzip -c "ame_ref_Amel_4.5_chr$chr.fa.gz" \
		| sed "s/^>gi|.*|\(NC_.*\)|.*/>\1/" \
		>> amel4.5.fa
done

# Move fasta file
mv amel4.5.fa ../genomes/

