#!/usr/bin/env python

"""Pick one p-value per gene"""

import sys

pbygene = {}
linebygene = {}

# Read each line
for line in sys.stdin:
	line = line.rstrip()
	f = line.split("\t")

	# Is this a results line?
	if len(f) > 10:
		pvalue = float( f[2] )
		gene1 = f[6]
		gene2 = f[9]

		oldp = pbygene.get(gene1, 1.0)
		if pvalue < oldp :
			pbygene[gene1] = pvalue
			linebygene[gene1] = line

		#oldp = pbygene.get(gene2, 1.0)
		#if pvalue < oldp :
		#	pbygene[gene2] = pvalue


for gene in pbygene:
	print "{}\t{}\t{}".format(pbygene[gene], gene, linebygene[gene])