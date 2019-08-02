#!/usr/bin/env python3

import sys

for line in sys.stdin:
	line = line.rstrip()
	fields = line.split('\t')
	print(f"{len(fields)}\t{line}")
