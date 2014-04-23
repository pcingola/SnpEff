#!/usr/bin/env python

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

#---
# Command line parsing
#---
if len(sys.argv) < 3:
	print >> sys.stderr, "Usage:{} ids.txt input.txt colNum\n".format( sys.argv[0] )
	sys.exit(1)

idsFileName = sys.argv[1]
inputFileName = sys.argv[2]
colNum = int( sys.argv[3] ) - 1

#---
# Read IDs
#---
with open(idsFileName) as idsFile:
	idSet = set(line.strip() for line in idsFile)
print >> sys.stderr, "Read ", len(idSet) ," IDs from file", idsFileName

#---
# Read input file and filter
#---
print >> sys.stderr, "Reding", inputFileName
with open(inputFileName) as inFile:
	for line in inFile:
		fields = line.split('\t')
		if len(fields) >= colNum and ( fields[colNum] in idSet ): print line.strip()
	
