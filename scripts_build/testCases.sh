#!/bin/sh

cd $HOME/workspace/SnpSift/
java -Xmx4g -cp $HOME/snpEff/SnpSift.jar ca.mcgill.mcb.pcingola.snpSift.testCases.TestSuiteAll 2>&1 | tee testSuiteAll.snpsift.txt

cd $HOME/workspace/SnpEff/
java -Xmx4g -cp $HOME/snpEff/snpEff.jar ca.mcgill.mcb.pcingola.snpEffect.testCases.TestSuiteAll 2>&1 | tee testSuiteAll.snpeff.txt

