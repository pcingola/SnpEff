#!/usr/bin/env python3

"""
 Convert chromosome IDs in a VCF file from NCBI to human-readable format
 Add "CAF" field to the INFO column

 Mapping file:
     Download the mapping file from:
       wget "https://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.40_GRCh38.p14/GCF_000001405.40_GRCh38.p14_assembly_report.txt"

     Then cut the relevant columns:
       cut -f 1,7 GCF_000001405.40_GRCh38.p14_assembly_report.txt | grep -v "^#" | tee chr2id.txt
"""

import sys


CAF_INFO_HEADER = '##INFO=<ID=CAF,Number=R,Type=String,Description="An ordered list of allele frequencies as reported by 1000Genomes, starting with the reference allele followed by alternate alleles as ordered in the ALT column.">'

def getCaf(info: str) -> str:
    """
    Example: 'FREQ: 1000Genomes:0.006989,0.993|GENOME_DK:0,1|Korea1K:0.8689,0.1311'
              => "0.006989,0.993"
    """
    freq = getFreq(info)
    if not freq:
        return None
    for f in freq.split('|'):
        if f.startswith('1000Genomes:'):
            return f[12:]
    return None
    

def getFreq(info: str) -> str:
    """ Find 'FREQ=' in the INFO column """
    idx = info.find('FREQ=')
    if idx < 0:
        return None
    idx2 = info.find(';', idx)
    if idx2 < 0:
        return info[idx+5:]
    return info[idx+5:idx2]


def load_mapping_file(mapping_file):
    """ Load the mapping file (assuming it's a TSV or space-separated file) """
    chrid2name = {}
    with open(mapping_file) as f:
        for l in f.readlines():
            chr_name, chr_id = l.strip().split("\t")
            chrid2name[chr_id] = chr_name

    return chrid2name


#--------------------------------------------
# Main
#--------------------------------------------

# Read the VCF file name and mapping file name from command line
if len(sys.argv) != 3:
    print("Usage: python convert.py <vcf_file> <mapping_file>")
    sys.exit(1)

vcf_file = sys.argv[1]
chr2id_file = sys.argv[2]

# Load mapping file
chrid2name = load_mapping_file(chr2id_file)

# Read the VCF file and replace chromosome IDs
with open(vcf_file, 'r') as file:
    for line in file:
        if line.startswith("#"):
            # Add header for CAF right before the last header line
            if line.startswith("#CHROM"):
                print(CAF_INFO_HEADER)
            print(line.strip())  # Print header lines as is
        else:
            # Split the line into columns
            columns = line.strip().split('\t')
            if columns[0] in chrid2name:
                columns[0] = chrid2name[columns[0]]  # Replace chromosome ID

            # Add CAF field to the INFO column
            info = columns[7]
            caf = getCaf(info)
            if caf:
                columns[7] = info + ";CAF=" + caf

            # Print the modified line
            print('\t'.join(columns))
