#!/usr/bin/env python3

import sys
import os

for line in sys.stdin:
	line = line.rstrip(os.linesep)
	fields = line.split('\t')
	fields_sorted = '\t'.join(sorted(fields))
	print(fields_sorted)
