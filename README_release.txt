

									Release process
									---------------

0) Install required libraries

	wget http://www.antlr.org/download/antlr-4.4-complete.jar
	mvn install:install-file -Dfile=antlr-4.4-complete.jar -DgroupId=org.antlr -DartifactId=antlr -Dversion=4.4 -Dpackaging=jar

1) Run JUnit tests (TestSuiteUnity) and make sure all of them pass

2) Run integration tests (TestSuiteIntegration)

3) Change version numbers:

	- Update SnpEff pom.xml 
	- Update SnpSift pom.xml
	- Update scripts_build/config.sh 
	- Update scripts_build/make.bds

4) Build JAR files, download databases, build databases, etc.
	
	a) Build JAR files
		./scripts_build/make.sh 
	
	b) Nextprot:
	
		 - Check nextProt database: Any new release? 
				ftp://ftp.nextprot.org/pub/current_release/xml/
				
		 - Build nextProt database (WARNING: It takes a lot of memory!): 
		 
		 	java -Xmx50G -jar snpEff.jar buildNextProt -v GRCh37.71 db/nextProt 2>&1 | tee buildNextProt.out
		 	
	c) Motif (Jasper)
	
		- Download latest Jaspar database
		
			./scripts_build/download_Pwms_Jaspar.sh
				
		- Copy to latest GRCh and GRCm (or any that has a mofit file "ls data/*/motif*)
		
			cp db/jaspar/pwms.bin data/GRCh37.75/
			cp db/jaspar/pwms.bin data/GRCm38.78/
		
	d) Check if there is a newer dbNSFP
	
		https://sites.google.com/site/jpopgen/dbNSFP

	e) Download ENSEMBL genomes

		./scripts_build/download_ensembl.sh
		./scripts_build/download_ensembl_bfmpp.sh

	f) Download NCBI genomes

		./scripts_build/download_ncbi.sh
		
	g) Download NCBI Human genomes

		./scripts_build/download_hg19.sh
		./scripts_build/download_hg19kg.sh

		./scripts_build/download_hg38.sh
		./scripts_build/download_hg38kg.sh

			
5) Upload files to sourceForge

	./scripts_build/uploadSourceForge.sh

6) Update Galaxy's snpEff.xml
		# Create galaxy genomes list 
		scripts_build/galaxy.sh		 

7) Upload to Galaxy ToolShed: http://toolshed.g2.bx.psu.edu/
		Reference: http://wiki.g2.bx.psu.edu/Tool%20Shed

