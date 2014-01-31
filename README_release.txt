

									Release process
									---------------

1) Run JUnit tests (TestSuiteAll) and make sure all of them pass

2) Run integration tests

3) Change version numbers:

	- Update SnpEff pom.xml 
	- Update SnpSift pom.xml
	- Update scripts_build/config.sh 

4) Build JAR files, download databases, build databases, etc.
	
	./scripts_build/build.sh 
	
	4.1) Nextprot:
	
		 - Check nextProt database: Any new release? 
				ftp://ftp.nextprot.org/pub/current_release/xml/
				
		 - Build nextProt database (WARNING: It takes a lot of memory!): 
		 
		 	java -Xmx50G -jar snpEff.jar buildNextProt -v GRCh37.71 db/nextProt 2>&1 | tee buildNextProt.out
		 	
	4.2) Motif (Jasper)
	
		- Download latest Jaspar database
		
				./scripts_build/download_Pwms_Jaspar.sh
				
		- Copy to latest GRCh and GRCm (or any that has a mofit file "ls data/*/motif*)
		
				cp db/jaspar/pwms.bin data/GRCh37.71/
				cp db/jaspar/pwms.bin data/GRCm38.71/
		
	4.3) Check if there is a newer dbNSFP
	
			https://sites.google.com/site/jpopgen/dbNSFP
			
5) Upload files to sourceForge

	./scripts_build/uploadSourceForge.sh

6) Update Galaxy's snpEff.xml
		# Create galaxy genomes list 
		scripts_build/galaxy.sh		 

7) Upload to Galaxy ToolShed: http://toolshed.g2.bx.psu.edu/
		Reference: http://wiki.g2.bx.psu.edu/Tool%20Shed

-------------------------------------------------------------------------------

Maven: Manually install JAR files

    mvn install:install-file -Dfile=sam-1.94.jar -DgroupId=net.sf.samtools -DartifactId=Sam -Dversion=1.94 -Dpackaging=jar
   
