#!/usr/bin/env python

import sys

for line in sys.stdin:
	line = line.strip()

	# Show comment lines
	if line.startswith('#'):
		print line
		continue

	# Split and parse GTF fields
	fields = line.split('\t')

	# Parse attributes
	attrs = fields[8].split(';')
	attrdict = dict()
	attrlist = []
	for attr in attrs:
		# Add if non-empty
		attr = attr.strip()
		if attr:
			(key, value) = attr.split(' ', 1)
			value = value[1:-1]	# Remove starting and trailing quotes
			attrdict[key] = value
			attrlist.append(key)

	changed = False

	# Append version to gene ID, if available
	if ('gene_id' in attrdict) and ('gene_version' in attrdict):
		attrdict['gene_id'] = attrdict['gene_id'] + '.' + attrdict['gene_version']
		changed = True

	# Append version to transcript ID, if available
	if ('transcript_id' in attrdict) and ('transcript_version' in attrdict):
		attrdict['transcript_id'] = attrdict['transcript_id'] + '.' + attrdict['transcript_version']
		changed = True
		
	if changed:
		# If changed, create fields from attributed in the same order as the original line
		attrs = ''
		for attr in attrlist:
			attrs += attr + ' "' + attrdict[attr] + '"; '
		fields[8] = attrs
		print '\t'.join(fields).strip()
	else:
		# No change? Just show the original line
		print line

	
