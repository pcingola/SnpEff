#!/bin/sh

mkdir ORI

# Download genome
wget http://agambiae.vectorbase.org/downloads/public_data/organism_data/agambiae/Genome/agambiae.CHROMOSOMES-PEST.AgamP3.fa.gz
wget http://agambiae.vectorbase.org/downloads/public_data/organism_data/agambiae/Geneset/agambiae.PEPTIDES-AgamP3.7.fa.gz
wget http://agambiae.vectorbase.org/downloads/public_data/organism_data/agambiae/Geneset/agambiae.BASEFEATURES_PEST-AgamP3.7.gtf.gz 
wget http://agambiae.vectorbase.org/downloads/public_data/organism_data/agambiae/Geneset/agambiae.TRANSCRIPTS-AgamP3.7.fa.gz

# Create genome file
mv agambiae.CHROMOSOMES-PEST.AgamP3.fa.gz ../genomes/agam3.7.fa.gz
gunzip ../genomes/agam3.7.fa.gz
echo "Edit and change chromosome names in FASTA file"
gzip ../genomes/agam3.7.fa

# Genes
mv agambiae.BASEFEATURES_PEST-AgamP3.7.gtf.gz ORI
cp ORI/agambiae.BASEFEATURES_PEST-AgamP3.7.gtf.gz genes.gtf.gz 

# Proteins
zcat ORI/agambiae.PEPTIDES-AgamP3.7.fa.gz | sed "s/^>\([^ ]*\)\(.*\)/>\1/" | sed "s/-P/-R/" > protein.fa 
gzip protein.fa 

# CDSs
mv agambiae.TRANSCRIPTS-AgamP3.7.fa.gz ORI
zcat ORI/agambiae.TRANSCRIPTS-AgamP3.7.fa.gz | sed "s/^>\([^ ]*\)\(.*\)/>\1/" > cds.fa
gzip cds.fa 

