#!/bin/sh -e

# Separate version-subversion by '_'
# E.g.: '4_1' (instead of '4.1')
export VERSION=4.1
export VERSION_UND=`echo $VERSION | tr '.' '_'`

#---
# Build SnpEff
#---

cd $HOME/workspace/SnpEff/

mvn clean compile assembly:assembly

cp target/snpEff-$VERSION-jar-with-dependencies.jar $HOME/snpEff/snpEff.jar

# Install JAR file in local Maven repo
mvn install:install-file \
	-Dfile=target/snpEff-$VERSION.jar \
	-DgroupId=ca.mcgill.mcb.pcingola \
	-DartifactId=snpEff \
	-Dversion=$VERSION \
	-Dpackaging=jar \
	-DgeneratePom=true \
	--quiet

cd - 

#---
# Build SnpSift
#---
cd $HOME/workspace/SnpSift/

mvn clean compile assembly:assembly

cp target/snpSift-$VERSION-jar-with-dependencies.jar $HOME/snpEff/SnpSift.jar

# Install JAR file in local Maven repo
mvn install:install-file \
	-Dfile=target/snpSift-$VERSION.jar \
	-DgroupId=ca.mcgill.mcb.pcingola \
	-DartifactId=snpSift \
	-Dversion=$VERSION \
	-Dpackaging=jar \
	-DgeneratePom=true \
	--quiet

cd - 

#---
# Update galaxy databases
#---
./scripts_build/galaxy.sh

echo "Build done!"
