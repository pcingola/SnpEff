#!/usr/bin/env python

#------------------------------------------------------------------------------
#
# Overlap between gene sets
#
#------------------------------------------------------------------------------

import sys

# Debug mode?
debug = False

#------------------------------------------------------------------------------
# Load MSigDb file
#------------------------------------------------------------------------------
def loadMsigDb(msigFile):
	geneSet = {}
	for line in open(msigFile) :
		fields = line.rstrip().split("\t")
		geneSetName = fields[0]
		geneSet[ geneSetName ] = set( fields[2:] )
		if debug : print geneSetName, " => ", geneSet[ geneSetName ]
	return geneSet

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

#---
# Command line parameters
#---
if len(sys.argv) != 3 :
	print >> sys.stderr, "Usage: " + sys.argv[0] + " msigDb.gmt set.gmt"
	sys.exit(1)

msigFile = sys.argv[1]
setFile = sys.argv[2]

geneSets = loadMsigDb(msigFile)
testSets = loadMsigDb(setFile)

print "{}%\t{}\t{}\t{}\t{}".format("overlap%", "overlap", "size_1", "size_2", "Gene Set 1", "Gene Set 2")
for gsetName1 in testSets:
	size1 = len(testSets[gsetName1])
	if size1 > 5:
		for gsetName2 in geneSets:
			size2 = len(geneSets[gsetName2])
			count = len(testSets[gsetName1] & geneSets[gsetName2])
			if count > 0:
				overlap = (100.0 * count) / size1
				print "{}%\t{}\t{}\t{}\t{}\t{}".format(overlap, count, size1, size2, gsetName1, gsetName2)




