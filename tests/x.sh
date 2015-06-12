#!/bin/sh

cat hgvs_md.chr17.vcf \
	| grep -e "^#" -e HGVS \
	| grep -v -F "NM_006907.2" \
	| grep -v -F "NM_022167.2" \
	| grep -v -F "NM_000160.3" \
	| grep -v -F "NM_000515.3" \
	| grep -v -F "NM_000835.3" \
	| grep -v -F "NM_001082575.1" \
	| grep -v -F "NM_001185077.1" \
	| grep -v -F "NM_001256071.1" \
	| grep -v -F "NM_003161.2" \
	| grep -v -F "NM_003955.3" \
	| grep -v -F "NM_007168.2" \
	| grep -v -F "NM_022167.2" \
	| grep -v -F "NM_024419.3" \
	| grep -v -F "NM_030779.2" \
	| grep -v -F "NM_058216.1" \
	| grep -v -F "NM_173477.2" \
	| grep -v -F "NM_173627.3" \
	| grep -v -F "NM_004169.3" \
	| grep -v -F "NM_004448.2" \
	| grep -v -F "NM_001146312.1" \
	| grep -v -F "NM_000304.2" \
	| grep -v -F "NM_020795.2" \
	| grep -v -F "NM_005568.3" \
	| grep -v -F "NM_198839.1" \
	| grep -v -F "NM_024819.4" \
	| grep -v -F "NM_004589.2" \
	| grep -v -F "NM_014336.3" \
	| grep -v -F "NM_001045.4" \
	| grep -v -F "NM_002686.3" \
	| grep -v -F "NM_030753.3" \
	| grep -v -F "NM_001004334.2" \
	| grep -v -F "NM_001040.3" \
	| grep -v -F "NM_022059.2" \
	| grep -v -F "NM_001123066.3" \
	| grep -v -F "NM_001114091.1" \
	| grep -v -F "NM_002558.2" \
	> zzz.vcf

wc -l hgvs_md.chr17.vcf zzz.vcf

