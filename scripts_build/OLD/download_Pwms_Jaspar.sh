#!/bin/sh

mkdir -p db/jaspar/
cd db/jaspar/

wget "http://jaspar.binf.ku.dk/html/DOWNLOAD/JASPAR_CORE/pfm/redundant/pfm_all.txt"
gzip pfm_all.txt
mv pfm_all.txt.gz pwms.bin

echo "File pwms.bin created"
