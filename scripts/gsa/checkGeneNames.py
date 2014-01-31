#!/usr/bin/env python

import sys

# Debug mode?
debug = False

#------------------------------------------------------------------------------
# Read genes file
#------------------------------------------------------------------------------
def readGenes(genesFile):
	print >> sys.stderr, "Reading file " + genesFile
	genes2new = {} 
	genes2old = {} 
	id2nameNew = {} 
	id2nameOld = {} 
	for line in open(genesFile) :
		fields = line.rstrip().split("\t")

		if debug: print fields

		geneId, nameOld = fields[0], fields[1]
		nameNew = ''
		if len(fields) > 2: nameNew = fields[2]

		if nameNew:
			genes2new[nameOld] = nameNew
			id2nameNew[id] = nameNew

		if nameOld:
			genes2old[nameNew] = nameOld
			id2nameOld[id] = nameOld

	return genes2new, genes2old, id2nameNew, id2nameOld

#------------------------------------------------------------------------------
# Read HGNC file: gene names, previous names and synonyms.
#------------------------------------------------------------------------------
def readHgcn(hgncFile):
	print >> sys.stderr, "Reading file " + hgncFile
	genesHgcn = {}
	for line in open(hgncFile) :
		fields = line.rstrip().split("\t")

		if len(fields) < 8: continue

		geneName, prevName, synonyms = fields[1], fields[6], fields[8]
		if debug: print "{}\t|{}|\t|{}|".format(geneName, prevName, synonyms)
		
		# Add all 'previous names'
		for g in prevName.split(",") :
			alias = g.strip()
			if alias:
				if alias in genesHgcn: 
					print >> sys.stderr, "Error: Alias '{}' already exists ( {} vs {} )!".format( alias, genesHgcn[alias], geneName )
				else :
					genesHgcn[alias] = geneName
					if debug: print "\tPrev: |{}|".format( alias )

		# Add all 'synonyms'
		for g in synonyms.split(",") :
			alias = g.strip()
			if alias:
				if alias in genesHgcn: 
					print >> sys.stderr, "Error: Alias '{}' already exists ( {} vs {} )!".format( alias, genesHgcn[alias], geneName )
				else :
					genesHgcn[alias] = geneName
					if debug: print "\tSyn: |{}|".format( alias )

	return genesHgcn

#------------------------------------------------------------------------------
# Find gene
#------------------------------------------------------------------------------
#def findGeneName(g, genes2new, genes2old, genesHgcn):
def findGeneName(g):
	# Gene name found, no need to find a new name
	if isValid(g, genes2new): return g

	# Try translating the name using 'genes2old' dictionary
	geneOld = genes2old.get(g, "")
	if isValid(geneOld, genes2new): return geneOld

	# Try an alias
	geneHgcn = genesHgcn.get(g, "")
	if isValid(geneHgcn, genes2new): return geneHgcn

	# We have an alias, but it was not valid.
	if geneHgcn:
		# Try to find an 'old' name for the alias
		geneNew = genes2old.get(geneHgcn, "")
		if isValid(geneNew, genes2new): return geneNew

	# Desperate attempt: Find a gene that matches
	for gn in genes2new:
		if gn.startswith(g): return gn

	for gn in genes2old:
		if gn.startswith(g): return genes2old[gn]

	return ""

# Valid gene name (not empty and is in 'genes' dictionary)
def isValid(gname, genes):
	if gname and (gname in genes): return True
	return False

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

#---
# Parse command line 
#---
if len(sys.argv) != 3:
	print >> sys.stderr, "Usage: " + sys.argv[0] + " hgnc_complete_set.txt genes.list"
	sys.exit(1)

hgncFile = sys.argv[1]		# This argument is a Hugo File. Note: You can download the latest version from ftp://ftp.ebi.ac.uk/pub/databases/genenames/hgnc_complete_set.txt.gz
genesFile = sys.argv[2]		# This is a "geneId \t geneName" list created from a GTF file

# Read files
genes2new, genes2old, id2nameNew, id2nameOld = readGenes(genesFile)
genesHgcn = readHgcn(hgncFile)

#---
# Read all lines from STDIN
# Note: This is counter intuitive because we are trying to 
#		replace 'new' names with 'old' names (and not the 
#		other way arround which is what you'd expect)
#---
for line in sys.stdin:
	f = line.rstrip().split('\t')
	geneSet = f[0]
	genesNames = f[2:]

	# Check that each gene has a valid geneID
	missing = ""
	missingCount = 0
	foundAlias = 0
	out = "{}\t{}".format(geneSet, f[1]);
	for g in genesNames :
		geneOld = findGeneName(g)

		if not geneOld:
			# No valid replacement found
			missing += "\t\t'{}'\n".format(g)
			missingCount += 1
		elif g != geneOld:
			# Replacement found
			missingCount += 1
			foundAlias += 1
			missing += "\t\t'{}'\t->\t'{}'\n".format(g, geneOld)
			
		# Add only if there is a gene name (skip if no replacement has been found)
		if geneOld : out += "\t" + geneOld

	# Show line (names have been replaced)
	print out

	if missingCount > 0 :
		total = (len(f) - 2)
		missingPerc = 100.0 * missingCount / total
		print >> sys.stderr, "{}\n\tMissing : {} ( {:.1f}% )\n\tTotal   : {}\n\tReplaced: {}\n\tGenes ( -> Replacement ) :\n{}".format(geneSet, missingCount, missingPerc, total, foundAlias, missing)
