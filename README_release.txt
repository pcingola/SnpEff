

									Release process
									---------------

0) Install required libraries

	# ANTRL
	curl -O http://www.antlr.org/download/antlr-4.5.1-complete.jar
	mvn install:install-file -Dfile=antlr-4.5.1-complete.jar -DgroupId=org.antlr -DartifactId=antlr -Dversion=4.5.1 -Dpackaging=jar

	# BioJava
	mvn install:install-file -Dfile=biojava3-core-3.0.7.jar      -DgroupId=org.biojava -DartifactId=biojava3-core      -Dversion=3.0.7 -Dpackaging=jar
	mvn install:install-file -Dfile=biojava3-structure-3.0.7.jar -DgroupId=org.biojava -DartifactId=biojava3-structure -Dversion=3.0.7 -Dpackaging=jar

1) Change version numbers:
	- Update SnpEff pom.xml 
	- Update SnpSift pom.xml

2) Build JAR files, download databases, build databases, etc.
		./make.bds 

3) Run JUnit tests and integration tests
	./make.bds -test

4) Download databases: ENSEMBL, NCBI, dbSnp, ClinVar, dbNSFP, PDB, Jaspar, etc.
		./make.bds -download
		 		
5) Build databases
		./make.bds -db
			
6) Upload files to sourceForge

	./make.bds -uploadCore		# Upload core files
	./make.bds -uploadDbs		# Upload databases files
	./make.bds -uploadHtml		# Upload web pages and manual	
