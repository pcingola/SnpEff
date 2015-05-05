#!/usr/bin/env python

import sys

debug = True

# Find an 'ANN' or 'EFF' field
def vcfReplaceFirstAnn(line):
	f = line.split('\t')
	infos = f[7].split(';')
	
	infosChanged = False
	
	# For every INFO field
	for i in range(0, len(infos)):
		info = infos[i]
		if info.startswith('ANN=') or info.startswith('EFF='):
			# Parse 'ANN' field
			name = info[0:4]
			anns = info[4:]
			firstAnn = anns.split(',')[0]

			# Replace field by forst annotation only
			info = name + firstAnn
			infos[i] = info
			infosChanged = True

	if infosChanged:
		# Show new fields
		f[7] = ';'.join(infos)
		print '\t'.join(f)
	else : 
		# No change, just show original line
		print line

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

# Read VCF form STDIN
for l in sys.stdin:
	l = l.rstrip()

	if l.startswith('#') : 
		# Show header
		print l
	else :
		vcfReplaceFirstAnn(l)

