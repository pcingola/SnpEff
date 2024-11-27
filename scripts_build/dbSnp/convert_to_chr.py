#!/usr/bin/env python3

#
# Convert chromosome IDs in a VCF file from NCBI to human-readable format
#
# Mapping file:
#     Download the mapping file from:
#       wget "https://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.40_GRCh38.p14/GCF_000001405.40_GRCh38.p14_assembly_report.txt"
#     
#     Then cut the relevant columns:
#       cut -f 1,7 GCF_000001405.40_GRCh38.p14_assembly_report.txt | grep -v "^#" | tee chr2id.txt


# Load the mapping file (assuming it's a TSV or space-separated file)
mapping_file = "chr2id.txt"
chrid2name = {}
with open(mapping_file) as f:
    for l in f.readlines():
        chr_name, chr_id = l.strip().split("\t")
        chrid2name[chr_id] = chr_name


# Example: Translating IDs in a VCF file
vcf_file = "GCF_000001405.38.vcf"
with open(vcf_file, 'r') as file:
    for line in file:
        if not line.startswith("#"):  # Skip header lines
            columns = line.strip().split('\t')
            if columns[0] in chrid2name:
                columns[0] = chrid2name[columns[0]]  # Replace chromosome ID
            print('\t'.join(columns))  # Print or write to a new file
        else:
            print(line.strip())  # Print header lines as is
