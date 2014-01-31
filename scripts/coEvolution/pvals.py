#!/usr/bin/env python

import sys

for l in sys.stdin:
	l = l.rstrip()
	f = l.split('\t')
	minp = 1
	for i in range(4) : 
		p = float( f[i] )
		minp = min( minp, p )
	print "{}\t{}".format(minp, l)

