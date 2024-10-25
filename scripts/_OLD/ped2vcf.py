#!/usr/bin/env python3

import sys
import re


def alleles(gt1, gt2):
	"""Get reference and alternative alleles"""

	# Count genotypes 1 and 2
	count = {}
	for g in gt1 :
		count[g] = count.get(g, 0) + 1

	for g in gt2 :
		count[g] = count.get(g, 0) + 1

	# Find mayor allele (we call it 'ref')
	maxCount = 0
	ref = ''
	for g in count:
		if count[g] > maxCount:
			maxCount  = count[g]
			ref = g

	# Find minor allele (we call these one 'alt')
	alt = ''
	for g in count:
		if g != ref and g != '0': alt = g

	# Create a genotype string (VCF style)
	gtstr = ""
	for i in range(len(gt1)):
		gtstr += "\t" + gtVcf(ref, alt, gt1[i]) + "/" + gtVcf(ref, alt, gt2[i])

	return ref, alt, count[alt], gtstr


def gtVcf(ref, alt, gt):
	""" Get genotype in VCF style string"""
	if gt == ref: return "0"
	if gt == alt: return "1"
	return "."

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

# Parse comman line arguments
if len(sys.argv) != 3 :
	print(f"Usage: {sys.argv[0]} file.ped file.map", file=sys.stderr)
	sys.exit(1)

pedFile = sys.argv[1]
mapFile = sys.argv[2]

# Prepare to read data
reSplit = re.compile("\\s+")

snps = []
ids = []
gt = []

# Read MAP file
for line in open(mapFile):
	f = reSplit.split(line.rstrip())
	chr, id, cm, pos = f[0], f[1], f[2], f[3]
	snps.append( (id, chr, pos) )

# Create genotype lists
geno1 = [ [] for s in snps]
geno2 = [ [] for s in snps]

# Read PED file
for line in open(pedFile):
	f = reSplit.split(line.rstrip())
	famId, id, moId, faId, sex, pheno = f[0], f[1], f[2], f[3], f[4], f[5]
	ids.append( id )

	# Genotypes
	gt1 = f[6::2]
	gt2 = f[7::2]
	for i in range(len(snps)):
		geno1[i].append( gt1[i] )
		geno2[i].append( gt2[i] )

# Write VCF file
ids_str = '\t'.join(ids)
print(f"#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tGT\t{ids_str}")
for i in range(len(snps)) :
	id, chr, pos = snps[i]
	ref, alt, count, gtStr = alleles( geno1[i], geno2[i] )
	printf(f"{chr}\t{pos}\t{id}\t{ref}\t{alt}\t{count}\t{gtStr}")
