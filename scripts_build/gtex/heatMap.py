#!/usr/bin/env python

"""
  Show Gene/Tissue expressin (heatmap) using GTEx


  Pablo Cingolani 2013
"""

import sys

# Debug mode?
debug = False

# How many NAs are too much (skip genes)
naThreshold = 0.95

#------------------------------------------------------------------------------
# Process normalized GTEx file
#------------------------------------------------------------------------------
def readGtex(gtexFile, ids, labels, genes):
	columnIdx = []
	header = []
	gtexGenes = {}
	for line in open(gtexFile) :
		fields = line.rstrip().split("\t")

		if not columnIdx : 
			header = fields[:]
			# Read header and add all columnIdx numbers that we are looking for
			title = []
			for i in range(len(fields)):
				id = fields[i]
				if id in ids:
					columnIdx.append(i)
					ids[ id ] = 1
					title.append( labels[id] )

			# Sanity check
			ok = True
			
			for id in ids:
				if not ids[id]:
					print >> sys.stderr, "Missing GTEx ID '{}'.".format( id )
					ok = False

			if not ok: sys.exit(1)
			if debug: print >> sys.stderr, "OK, All required IDs found."

			# Show title line. Note: We don't have 'gene' in the title in order to make R use that as a row name 
			print "\t".join(title)

		else :
			geneId, geneName = fields[0], fields[1]

			# Only show if the gene in in our list
			if geneId in genes or geneName in genes:
				# Collect values for requested IDs
				vals = []
				countNa = 0
				for idx in columnIdx :
					vals.append( fields[idx] )
					if fields[idx] == 'NA': countNa += 1

				if countNa < (len(vals) * naThreshold):
					print "{}\t{}".format( geneName, "\t".join(vals) )
				else:
					print >> sys.stderr, "Skipping gene {}, because it has mostly NA values ({} / {}): {}".format( geneName, countNa, len(vals), "\t".join(vals) )

	return gtexGenes

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

#---
# Command line parameters
#---
if len(sys.argv) != 5 :
	print >> sys.stderr, "Usage: " + sys.argv[0] + " gtex_normalized.txt gtexExperimentId_1,gtexExperimentId_2,...,gtexExperimentId_N expLabel_1,expLabel_2,...,expLabel_N geneName_1,geneName_2,...,geneName_M"
	sys.exit(1)

gtexFile = sys.argv[1]
gtexExperimentIds = sys.argv[2]
gtexExperimentLabels = sys.argv[3]
geneNames = sys.argv[4]

# Create a hash of IDs
idList = [ id for id in gtexExperimentIds.split(",") if id ]
labelList = [ label for label in gtexExperimentLabels.split(",") if label ]
ids = dict( (id,0) for id in gtexExperimentIds.split(",") if id )

# Sanity check
if len(labelList) != len(idList):
	print >> sys.stderr, "Error: gtexExperimentId list and expLabel list must be the same size"
	sys.exit(1)

labels = dict( zip(idList, labelList) )
genes = set( gene for gene in geneNames.split(",") if gene )

if debug:
	print >> sys.stderr, "idList    :", idList
	print >> sys.stderr, "labelList :", labelList
	print >> sys.stderr, "labels    :", labels
	print >> sys.stderr, "GTEx IDs:"
	for id in ids:
		print >> sys.stderr, "\t", id

# Read files
gtexGenes = readGtex(gtexFile, ids, labels, genes)

