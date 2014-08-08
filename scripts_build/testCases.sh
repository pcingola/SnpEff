#!/bin/sh

# Build JAR files
# ./scripts_build/make.sh

DIR=$HOME/snpEff

# # Run SnpSift test cases
# cd $HOME/workspace/SnpSift/
# java -Xmx4g \
# 	-cp $DIR/SnpSift.jar \
# 	org.junit.runner.JUnitCore \
# 	ca.mcgill.mcb.pcingola.snpSift.testCases.TestSuiteAll \
# 	2>&1 \
# 	| tee $DIR/testSuiteAll.snpsift.txt

# Run SnpEff test cases
cd $HOME/workspace/SnpEff/
java -Xmx4g \
	-cp $DIR/snpEff.jar \
	org.junit.runner.JUnitCore \
	ca.mcgill.mcb.pcingola.snpEffect.testCases.TestSuiteAll \
	2>&1 \
	| tee $DIR/testSuiteAll.snpeff.txt

