#!/usr/bin/env python

import sys

for line in sys.stdin:
	line = line.rstrip()
	print "{}\t{}".format( len( line.split('\t') ), line)
