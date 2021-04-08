#!/bin/bash -eu
set -o pipefail

cd '/Users/kqrw311/snpEff/download/ncbi/GRCh38.p13'

# SYS command. line 0
wget -N --retr-symlinks -O '/Users/kqrw311/snpEff/download/ncbi/GRCh38.p13/GCF_000001405.39_GRCh38.p13_genomic.gtf.gz' 'https://ftp.ncbi.nlm.nih.gov//genomes/all/GCF/000/001/405/GCF_000001405.39_GRCh38.p13/GCF_000001405.39_GRCh38.p13_genomic.gtf.gz' 
# Checksum: b09e0765
