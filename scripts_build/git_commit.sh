#!/bin/sh -e

rm -vf snpeff_*.html snpEff_genes.txt snpEff_summary.csv testSuiteAll.snpeff.txt

git add -A .
git commit -a -m "Project updated"
git push origin

