#!/bin/sh -e

export VERSION=5.4

# Separate version-subversion by '_'
# E.g.: '4_1' (instead of '4.1')
export VERSION_UND=`echo $VERSION | tr '.' '_'`

#---
# Build SnpEff
#---

cd $HOME/workspace/SnpEff/

mvn clean compile assembly:single jar:jar

cp target/SnpEff-$VERSION-jar-with-dependencies.jar $HOME/snpEff/snpEff.jar

# Install JAR file in local Maven repo
mvn install:install-file \
	-Dfile=target/SnpEff-$VERSION.jar \
	-DgroupId=org.snpeff \
	-DartifactId=SnpEff \
	-Dversion=$VERSION \
	-Dpackaging=jar \
	-DgeneratePom=true \
	--quiet

cd - 

#---
# Build SnpSift
#---
cd $HOME/workspace/SnpSift/

mvn clean compile assembly:single jar:jar

cp target/SnpSift-$VERSION-jar-with-dependencies.jar $HOME/snpEff/SnpSift.jar

# Install JAR file in local Maven repo
mvn install:install-file \
	-Dfile=target/SnpSift-$VERSION.jar \
	-DgroupId=org.snpsift \
	-DartifactId=SnpSift \
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
