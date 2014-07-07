#!/usr/bin/env python

import sys
import os

for line in sys.stdin:
	line = line.rstrip(os.linesep)
	print( '\t'.join( sorted( line.split('\t') ) ) )
