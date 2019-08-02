#!/usr/bin/env python3

#-------------------------------------------------------------------------------
#
# Convert simple TXT files into VCF-like files
#
# Notes:
#	i) TXT file is supposed to have a title line (first line is column names)
#	ii) TXT is tab separated
#
# Usage:
#	cat file.txt | ./txt2vcf.py chrom pos ref alt info_1 ... info_N
#
#	where :
#		'chrom', 'pos', etc. are the names of the columns for CHROM, POS, etc.
#
#		'info_1' ... 'info_N' will be added to the INFO fields using
#		the same names as field name
#
#
# 															Pablo Cingolani
#-------------------------------------------------------------------------------

import sys

# Parse command line argument
if len(sys.argv) < 5:
	print(f'Usage: {sys.argv[0]} chrom pos ref alt info_1 .... info_N', file=sys.stderr)
	exit(1)

chromName = sys.argv[1]
posName = sys.argv[2]
refName = sys.argv[3]
altName = sys.argv[4]
infoNames = set( sys.argv[5:] )

# Fields
chromCol = -1
posCol = -1
refCol = -1
altCol = -1
infoCol = {}
header = []

# Read STDIN
for line in sys.stdin:
	f = line.rstrip().split('\t')

	if not header:
		# Parse first line (header)
		header = f
		for i in range(len(f)):
			if f[i] == chromName:	chromCol = i
			if f[i] == posName:		posCol = i
			if f[i] == refName:		refCol = i
			if f[i] == altName:		altCol = i
			if f[i] in infoNames:
				infoCol[f[i]] = i
				print(f"Adding column name {f[i]}, column number {i}", file=sys.stderr)
				infoNames.remove(f[i])

		# Sanity checks
		if infoNames:
			print(f"Columns not found: {infoNames}", file=sys.stderr)
			exit(1)

		if chromCol < 0:
			print(f"Column not found: {chromName}", file=sys.stderr)
			exit(1)

		if posCol < 0:
			print(f"Column not found: {posCol}", file=sys.stderr)
			exit(1)

		if refCol < 0:
			print(f"Column not found: {refCol}", file=sys.stderr)
			exit(1)

		if altCol < 0:
			print(f"Column not found: {altCol}", file=sys.stderr)
			exit(1)

		# Show VCF header
		print("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO")

	else:
		# Add all info fields
		infos = ""
		for name in infoCol:
			val = f[infoCol[name]]
			if not val: val = '.'
			if infos: infos += ';'
			infos += "{}={}".format(name, val)

		# Output in pseudo-VCF format
		print(f"{f[chromCol]}\t{f[posCol]}\t{f[refCol]}\t{f[altCol]}\t{infos}")
