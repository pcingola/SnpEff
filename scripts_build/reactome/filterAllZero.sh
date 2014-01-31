#!/bin/sh -e

#file=zzz.1660.txt.gz
file=$1

cat $file | ./filterAllZero.pl > circuits.non_zero.raw.txt
cut -f 1,2 circuits.non_zero.raw.txt | tr -d "-" | tr -s "_" > ids.txt
cut -f 3- circuits.non_zero.raw.txt > nums.txt

wc -l nums.txt ids.txt circuits.non_zero.raw.txt

# Create file
cat $file | head -n 1 |  tr -d "-" | tr -s "_" > circuits.non_zero.txt
paste ids.txt nums.txt >> circuits.non_zero.txt

head -n 1 circuits.non_zero.txt | tr "\t" "\n" > expNames.long.txt
head -n 1 circuits.non_zero.txt | tr "\t" "\n" | cut -f 1 -d "_" > expNames.short.txt
( echo -e "nameLong\tnameShort" ; paste expNames.long.txt expNames.short.txt ) > expNames.txt

rm -vf circuits.non_zero.raw.txt expNames.long.txt expNames.short.txt  ids.txt nums.txt

