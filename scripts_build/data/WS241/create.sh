#!/bin/sh -e

zcat ORI/c_elegans.PRJNA13758.WS241.annotations.gff3.gz \
        | grep WormBase \
		| sed "s/Transcript://g" \
		| sed "s/Gene://g" \
        > genes.gff


