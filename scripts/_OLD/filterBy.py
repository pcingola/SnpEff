#!/usr/bin/env python3

#-------------------------------------------------------------------------------
#
# Filter a TXT file
#
# Make sure column number 'colNum' from 'input.txt' matches
# one entry from 'ids.txt'
#
#
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

import sys

# Command line parsing
if len(sys.argv) < 3:
	print(f"Usage:{sys.argv[0]} ids.txt input.txt colNum\n", file=sys.stderr)
	sys.exit(1)

idsFileName = sys.argv[1]
inputFileName = sys.argv[2]
colNum = int( sys.argv[3] ) - 1

# Read IDs
with open(idsFileName) as idsFile:
	idSet = set(line.strip() for line in idsFile)
print(f"Read {len(idSet)} IDs from file '{idsFileName}'", file=sys.stderr)

# Read input file and filter
print(f"Reding '{inputFileName}'", file=sys.stderr)
with open(inputFileName) as inFile:
	for line in inFile:
		fields = line.split('\t')
		if len(fields) > colNum and ( fields[colNum] in idSet ):
			print(line.strip())
