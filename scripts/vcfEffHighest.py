#!/usr/bin/env python

import sys

debug = True

# Parse fields from a VCF line
def vcfParse(line):
	# Parse fields
	f = line.rstrip().split('\t')
	chr, pos, id, ref, alt, qual, filter, infos = f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7]
	return chr, pos, id, ref, alt, qual, filter, infos

# Find an 'EFF' field
def vcfFindEff(infos):
	for info in infos.split(';'):
		if info.startswith('EFF='):
			effs = info[4:]
			return effs.split(',')
	return ''

# Find an 'EFF' field
def vcfParseEff(effs):
	f = eff.split('|')
	effectImpact, funcClass, codon, aa, aalen, geneName, trBioType, geneCoding, trId, exon, genotypeNum = f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], f[8], f[9], f[10]
	effect, impact = effectImpact.split('(')
	return effect, impact, funcClass, codon, aa, aalen, geneName, trBioType, geneCoding, trId, exon, genotypeNum


#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

# Read VCF form STDIN
for l in sys.stdin:
	# Skip header
	if l.startswith('#') : continue

	# Parse fields
	chr, pos, id, ref, alt, qual, filter, infos = vcfParse(l)

	# Find 'EFF'
	imp = {}

	# Split each 'EFF' sub-field
	for eff in vcfFindEff(infos):
		effect, impact, funcClass, codon, aa, aalen, geneName, trBioType, geneCoding, trId, exon, genotypeNum = vcfParseEff(eff)
		if impact == 'HIGH' or impact == 'MODERATE':
			if not impact in imp: imp[impact] = {}
			imp[impact][geneName] = eff

	# Show results
	out = ""
	if 'HIGH' in imp:
		out += '\tHIGH\t' + ','.join( sorted(imp['HIGH'].keys()) )
		out += '\t' + ','.join( sorted(imp['HIGH'].values()) )
	elif 'MODERATE' in imp:
		out += '\tMODERATE\t' + ','.join( sorted(imp['MODERATE'].keys()) )
		out += '\t' + ','.join( sorted(imp['MODERATE'].values()) )
	
	if out: print chr + '\t' + pos + '\t' + out
	else:	print chr + '\t' + pos + '\t.\t.\t.'




