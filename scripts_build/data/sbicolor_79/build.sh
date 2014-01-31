#!/bin/sh

gunzip -c ORI/Sbicolor_79_cds.fa.gz \
	| sed "s/^>S.*\|PACid:/>PAC:/g" \
	> cds.fa

gunzip -c ORI/Sbicolor_79_protein.fa.gz \
	| sed "s/^>S.*\|PACid:/>PAC:/g" \
	> protein.fa
