#!/usr/bin/env python

#------------------------------------------------------------------------------
#
# Create a phenmotypes + covariates file that we can easily read in R
#
#														Pablo Cingolani 2013
#------------------------------------------------------------------------------

import sys

debug = False

#------------------------------------------------------------------------------
# Print a header line
#------------------------------------------------------------------------------
def printHeaderLine(title, values, samples):

	# First columns is 'title'
	out = title

	# Add values on the following columns
	for id in samples:
		out += "\t{}".format( values.get(id, "-1") )

	print out

#------------------------------------------------------------------------------
# Find header in VCF line
#------------------------------------------------------------------------------
def readVcfSampleNames(vcfFile):
	print >> sys.stderr, "Reading VCF file: " + vcfFile

	if vcfFile == "-":
		f = sys.stdin
	else:
		f = open(vcfFile)

	for line in f:
		# Header line?
		if line.startswith('#'):
			if line.startswith('#CHROM'):
				return line.rstrip().split('\t')[9:]
		else: return []

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

#---
# Parse command line arguments
#---
if len(sys.argv) != 4 :
	print "Usage: {} pcaFile.txt pheno.age.tfam file.vcf".format(sys.argv[0])
	sys.exit(1)

pcaFile = sys.argv[1]
tfamFile = sys.argv[2]
vcfFile = sys.argv[3]

#---
# Read PCA file
#---
print >> sys.stderr, "Reading PCA file: " + pcaFile
pcas = {}
maxPc = 0
with open(pcaFile) as f:
	for line in f:
		line = line.rstrip()
		id = line.split('\t')[0]
		pc = line.split('\t')[1:]
		maxPc = max(maxPc, len(pc))
		if debug : print "Line\tID: {}\tPCAs: {}".format(id, pc)
		if id : pcas[id] = pc

#---
# Read TFAM
#---
print >> sys.stderr, "Reading TFAM (with age) file: " + tfamFile
sex = {}
pheno = {}
age = {}
with open(tfamFile) as f:
	for line in f:
		f = line.rstrip().split('\t')
		id, lsex, lpheno, lage = f[1], f[4], f[5], f[6]
		sex[ id ] = lsex
		pheno[ id ] = lpheno
		age[ id ] = lage
		if debug : print "Line\tID: {}\tSex: {}\tPheno: {}".format(id, sex, pheno)

#---
# Read VCF file
#---
samples = readVcfSampleNames(vcfFile)

#---
# Output phenotypes + covariates
#---
print "sample\t" + "\t".join(samples)
printHeaderLine("phenotype", pheno, samples)

# Show PC information
for pcnum in range(maxPc):
	# Show 'PC' label
	outLine = "PC{}".format(pcnum+1)

	# Show PC values
	for id in samples:
		pc = 0
		pcs = pcas.get(id,[])
		if pcs : pc = pcs[pcnum]
		outLine += "\t{}".format(pc)

	print outLine

# Show sex information
printHeaderLine("sex", sex, samples)
printHeaderLine("age", age, samples)

