#!/bin/sh

# Build JAR files
# ./scripts_build/make.sh

# Run SnpSift test cases
cd $HOME/workspace/SnpSift/
java -Xmx4g -cp $HOME/snpEff/SnpSift.jar org.junit.runner.JUnitCore ca.mcgill.mcb.pcingola.snpSift.testCases.TestSuiteAll 2>&1 | tee testSuiteAll.snpsift.txt

# Run SnpEff test cases
cd $HOME/workspace/SnpEff/
java -Xmx4g -cp $HOME/snpEff/snpEff.jar ca.mcgill.mcb.pcingola.snpEffect.testCases.TestSuiteAll 2>&1 | tee testSuiteAll.snpeff.txt

