#!/bin/sh

. `dirname $0`/config.sh

#---
# Build SnpEff
#---

cd $HOME/workspace/SnpEff/

mvn clean compile assembly:assembly

cp target/snpEff-$VERSION_SNPEFF-jar-with-dependencies.jar $HOME/snpEff/snpEff.jar

# Install JAR file in local Maven repo
mvn install:install-file \
	-Dfile=target/snpEff-$VERSION_SNPEFF.jar \
	-DgroupId=ca.mcgill.mcb.pcingola \
	-DartifactId=snpEff \
	-Dversion=$VERSION_SNPEFF \
	-Dpackaging=jar \
	-DgeneratePom=true \
	--quiet

cd - 

#---
# Build SnpSift
#---
cd $HOME/workspace/SnpSift/

mvn clean compile assembly:assembly

cp target/snpSift-$VERSION_SNPSIFT-jar-with-dependencies.jar $HOME/snpEff/SnpSift.jar

# Install JAR file in local Maven repo
mvn install:install-file \
	-Dfile=target/snpSift-$VERSION_SNPSIFT.jar \
	-DgroupId=ca.mcgill.mcb.pcingola \
	-DartifactId=snpSift \
	-Dversion=$VERSION_SNPSIFT \
	-Dpackaging=jar \
	-DgeneratePom=true \
	--quiet

cd - 

#---
# Update galaxy databases
#---
./scripts_build/galaxy.sh
