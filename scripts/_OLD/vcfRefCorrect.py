#!/usr/bin/env python3

import sys


#-------------------------------------------------------------------------------
# Read fasta file
#-------------------------------------------------------------------------------

def readFasta( fasta ):
	print(f"Reading FASTA file {fasta}", file=sys.stderr)
	lineNum = 1
	chrname = ''
	chrs = dict()
	seq = []	# Read sequence as a list of strigs

	for line in open(fasta):
		if line.startswith(">"):
			# Add previous sequence, if any
			if chrname != '':	chrs[chrname] = ''.join(seq).upper()
			chrname = line[1:].strip()
			if chrname.startswith('chr'): chrname = chrname[3:]
			print(f"Chromosome '{chrname}'", file=sys.stderr)
			chrs[chrname] = ""
			seq = []
		else:
			seq.append( line.rstrip() )

		# Show something
		if lineNum % 10000 == 0:
			sys.stderr.write('.')
		lineNum += 1

	print("", file=sys.stderr)
	if chrname != '':   chrs[chrname] = ''.join(seq).upper()
	return chrs

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Read fasta file
fasta = sys.argv[1]
chrs = readFasta(fasta)

# Read VCF file
for line in sys.stdin:
	line = line.rstrip()

	if line.startswith('#'):
		# Show header
		print(line)
	else:
		# Extract REF field
		fields = line.split('\t')
		(chrom, pos, ref) = ( fields[0], fields[1], fields[3] )

		if chrom in chrs:
			chrSeq = chrs[chrom]

			# Get coordinates
			posStart = int(pos) - 1
			posEnd = posStart + len(ref)

			# Correct 'REF' sequence
			if posEnd < len(chrSeq):
				refOri = ref
				ref = chrSeq[posStart:posEnd]
				fields[3] = ref
			else:
				print(f"Position {pos} not found in chromosome '{chrom}' (chromosome length {len(chrSeq)})", file=sys.stderr)
		else:
			print(f"Chromosome '{chrom}' not found", file=sys.stderr)

		# Show corrected line
		print('\t'.join(fields))
