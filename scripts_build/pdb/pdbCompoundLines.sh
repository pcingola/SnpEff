#!/bin/sh -e

dir=$1
scripts=`dirname $0`

for pdb in `find $dir -iname "*.ent.gz"`
do
	pdbId=`basename $pdb .ent.gz`
	gunzip -c $pdb | $scripts/pdbCompoundLines.py $pdbId
done
