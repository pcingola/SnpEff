#!/bin/sh

dir="db/darned/"

mkdir -p $dir

# Download
wget -O $dir/hg19.txt "http://beamish.ucc.ie/static/downloads/hg19.txt"

echo "Convert TXT to VCF"
cat $dir/hg19.txt | sort -k1,1 -k2n,2n | ./scripts_build/darnedToVcf.pl > $dir/darned.hg19.vcf
