################################################################################
#Getting SNPEff to work, build the ptr database
#
#@author:charles.hefer@gmail.com
###############################################################################
all: database

#Potri.019G130700.recode.vcf:
	#Copied from the GeneVCF directory, not required for support

#snpEff.config:
	#the original, but added new database lines : ptrichocarpa210.genome: Populus_trichocarpa

###############################################################################
#Create the required directories
###############################################################################
.data/ptrichocarpa210:
	mkdir -p data/ptrichocarpa210

.data/genomes:
	mkdir -p data/genomes
	
###############################################################################
#Step1
#Download the datasets to the current directory
###############################################################################
Ptrichocarpa_210_gene.gff3:
	wget ftp://ftp.jgi-psf.org/pub/compgen/phytozome/v9.0/Ptrichocarpa/annotation/Ptrichocarpa_210_gene.gff3.gz
	gunzip Ptrichocarpa_210_gene.gff3.gz

Ptrichocarpa_210.fa:
	wget ftp://ftp.jgi-psf.org/pub/compgen/phytozome/v9.0/Ptrichocarpa/assembly/Ptrichocarpa_210.fa.gz
	gunzip Ptrichocarpa_210.fa.gz

Ptrichocarpa_210_protein_primaryTranscriptOnly.fa: 
	wget ftp://ftp.jgi-psf.org/pub/compgen/phytozome/v9.0/Ptrichocarpa/annotation/Ptrichocarpa_210_protein_primaryTranscriptOnly.fa.gz
	gunzip Ptrichocarpa_210_protein_primaryTranscriptOnly.fa.gz

Ptrichocarpa_210_protein.fa:
	wget ftp://ftp.jgi-psf.org/pub/compgen/phytozome/v9.0/Ptrichocarpa/annotation/Ptrichocarpa_210_protein.fa.gz
	gunzip Ptrichocarpa_210_protein.fa.gz

Ptrichocarpa_210_cds.fa:
	wget ftp://ftp.jgi-psf.org/pub/compgen/phytozome/v9.0/Ptrichocarpa/annotation/Ptrichocarpa_210_cds.fa.gz
	gunzip Ptrichocarpa_210_cds.fa.gz


###############################################################################
#Make the required directory structure, perform mods, build database
###############################################################################
data/ptrichocarpa210/genes.gff: Ptrichocarpa_210_gene.gff3 | .data/ptrichocarpa210
	cat Ptrichocarpa_210_gene.gff3 \
		| sed 's/Name=/biotype=protein_coding;Name=/' \
		| sed 's/longest/constitutive/' \
		> Ptrichocarpa_210_gene.gff3.mod
	mv Ptrichocarpa_210_gene.gff3.mod data/ptrichocarpa210/genes.gff
	
data/genomes/ptrichocarpa210.fa: Ptrichocarpa_210.fa | .data/genomes
	cp Ptrichocarpa_210.fa data/genomes/ptrichocarpa210.fa
	
data/ptrichocarpa210/protein.fa: Ptrichocarpa_210_protein.fa | .data/ptrichocarpa210
	less Ptrichocarpa_210_protein.fa \
		> Ptrichocarpa_210_protein.fa.mod
	cat Ptrichocarpa_210_protein.fa.mod | sed "s/>Potri.*|PACid:/>PAC:/" > data/ptrichocarpa210/protein.fa
	
data/ptrichocarpa210/cds.fa: Ptrichocarpa_210_cds.fa | .data/ptrichocarpa210
	cat Ptrichocarpa_210_cds.fa | sed "s/>Potri.*|PACid:/>PAC:/" > data/ptrichocarpa210/cds.fa

###############################################################################
#Build the database
###############################################################################
database: data/ptrichocarpa210/genes.gff data/genomes/ptrichocarpa210.fa data/ptrichocarpa210/protein.fa data/ptrichocarpa210/cds.fa
	java -jar snpEff.jar build -gff3 ptrichocarpa210 
