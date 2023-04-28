#!/usr/bin/env python3

# Create a lis of 'vcf' entries, showing a variant at each possition

import gzip
import sys

# Gene chromosome, possition start, and end
chr, start, end = 'chrY', 2786855, 2787682

# FASTA reference file
reference  = "data_genomes/hg38/chrY.fa.gz"

# Other VCF entries
qual = 30

# Watson-Cricks complement
wc = {'A': 'T'
    , 'C': 'G'
    , 'G': 'C'
    , 'T': 'A'
}

def read_fasta(file_name):
    """
    Read a FASTA file, return a dictionary of sequences
    """
    print(f"Reading FASTA file '{file_name}'", file=sys.stderr)
    sequences = dict()
    sequence, name = None, None
    with gzip.open(file_name, 'rt') as f:
        for l in f:
            l = l.strip()
            if l.startswith('>'):
                # Header line
                if name is not None and sequence is not None:
                    print(f"Adding sequence '{name}'", file=sys.stderr)
                    sequences[name] = sequence
                sequence = ''
                name = l[1:].split(' ')[0] # First name after '>', but before any space character
                print(f"Readng sequence '{name}'", file=sys.stderr)
            else:
                sequence += l
    # Store the last sequence after iterating through the whole file
    if name is not None and sequence is not None:
        print(f"Adding sequence '{name}'", file=sys.stderr)
        sequences[name] = sequence
    return sequences


def deletion(chr_sequence, pos):
    """
    Create an DEL in chromosome 'chr' at possition 'pos' 
    Return REF and ALT fields for a VCF
    """
    ref = chr_sequence[pos:pos+2]
    alt = chr_sequence[pos:pos+1]
    return ref, alt


def insertion(chr_sequence, pos, insert_base = 'T'):
    """
    Create an INS in chromosome 'chr' at possition 'pos' 
    Return REF and ALT fields for a VCF
    """
    ref = chr_sequence[pos:pos+1]
    alt = ref + insert_base
    return ref, alt


def snv(chr_sequence, pos):
    """
    Create an SNV/SNP in chromosome 'chr' at possition 'pos' 
    Return REF and ALT fields for a VCF
    """
    ref = chr_sequence[pos:pos+1]
    return ref, wc[ref]

# Main

mutation_types = [snv, insertion, deletion]
mutation_type = deletion

# Read chromosomes, select 'chr'
seqs = read_fasta(reference)
seq = seqs[chr]

print(f"Creating VCF entries", file=sys.stderr)
for pos in range(start, end + 1):
    # Create a ref/alt pair
    ref, alt = mutation_type(seq, pos)
    print(f"{chr}\t{pos}\t.\t{ref}\t{alt}\t{qual}\tPASS\t.")

print(f"Done", file=sys.stderr)
