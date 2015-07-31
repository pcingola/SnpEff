#!/bin/sh

mkdir -p db/nextProt/
cd db/nextProt/

for chr in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 21 22 X Y MT
do
	wget "ftp://ftp.nextprot.org/pub/current_release/xml/nextprot_chromosome_$chr.xml.gz"
done

echo "Uncompressing files"
gunzip -v *gz
