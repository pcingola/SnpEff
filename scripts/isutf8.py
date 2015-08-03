#!/usr/bin/env python

import sys
import os.path

for file in sys.argv:
	if os.path.isfile(file):
		#print "File:", file
		content = open(file, 'rb').read()

		ok = True
		try:
			unicode(content, 'utf-8')
		except UnicodeDecodeError:
			print "File '", file, "' is not UTF-8"
			ok = False

		if not ok:
			lineNum = 1
			for line in content.split('\n'):
				try:
					unicode(line, 'utf-8')
				except UnicodeDecodeError:
					print "\tNon UTF-8 line ", lineNum,":\t", line
					ok = False

				lineNum = lineNum + 1

			

