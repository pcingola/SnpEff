# Features, versions and roadmap
##  1. Roadmap

Major features planned:

* **SnpEff**: Improvements in loss of function analysis
* **SnpSift**: Switch to ANTLR 4.X, handle arbitrary expressions.
				
## 2. Features

Features by version

**Version: 4.3 (2016-09).**

* Improved support for gene fusions
* Annotation of large structural variants


**Version: 4.1 (2015-01).**

* Standard annotation format: 'ANN' INFO field
* A better / more robust HGVS implementation
* Variants are re-aligned to the most 3'UTR (in agreement with HGVS).


**Version: 4.0 (2014-11).**

* Consistent 'help' screen when using command line option `-h`
* Effects sorted canonical transcripts first (for same level of effect / impact)
* Corrected problem on LOF annotations for gene names having spaces.


**Version: 4.0 (2014-07).**

* HGVS notations (now is default)
* Sequence Ontology terms (now by default)
* SnpEff downloads databases automatically
* Automatic third party databases downloads
* Support for new genome versions (such as GRCh38 / hg38)
* NextProt, Loss of function (LOF) and Nonsense mediated decay (MND) annotations by default
* Improved protein coding transcript detection (when building databases)
* Full support for MIXED variants: E.g. Some variants maybe a combination of Insertions, Deletions, SNPs or MNPs.
* Major code refactoring
* SnpSift `annotate`: Improved annotate support.
* SnpSift `dbNsfp`: Several improvements on annotation methods.
* Added support for gVCF files
                

**Version: 3.6 (2013-05-23).**

* Improved support for MIXED variants: E.g. Some variants maybe a combination of Insertions, Deletions, SNPs or MNPs.
* Improved HGVS notation
* SnpSift: `concordance`: Calculate concordance statistics between two VCF files (e.g. a sequencing and a chip-genotyping experiment)
* SnpSift: `vcfCheck` command (check VCF for several "common" problems)
* Moved to Java 7. mostly due to several problems in Java 6 libraries when reading bgzip files.


**Version: 3.5 (2013-03-23).**

* Improvements in cancer sample annotations
* Added `SPLICE_REGION` annotation
* SnpSift `private`: Annotate if a variant is "private" to a family (or cohort)
* SnpSift: `ccs`: Case / control summary statistics (of annotated files).
* SnpSift ` annotate `: Added tabix indexed files support. Automatic detection.


**Version: 3.4 (2013-11-23).**

* Automatic database download ("-download" option)
* Cancer samples: can be defined using a TXT file instead of VCF header.
* Improved GenBank
* Extended configuration options
* Better frame handling for GTF/GFF files
* Improvements in HGVS notation
* Galaxy support: Improvements and bug fixes
* SnpSift: Better support for dbNSFP (v2.1)


**Version: 3.3 (2013-06-12).**

* Over **8,500** genomes supported.
* All ENSEMBL (version 18) : Bacteria, Fungi, Metazoa, Plants and Protist genomes added.
* NextProt annotations added
* Motif annotations support added
* SnpSift: GeneSet annotations
* SnpEff count: Genomic region statistics counting reads, variants, intervals, etc.
                

** Version: 3.2 (2013-14-01).**

* **Cancer variants analysis**
* **GATK** compatible (`-o gatk`)
* HGVS notations support


**Version: 3.1 (2012-11-02).**

* All NCBI bacterial genomes added: Over 2,500 genomes added!
* Loss of function effect and tag added (experimental command line option '-lof')
* Nonsense-mediated decay effect and tag added (experimental command line option '-lof')
* ENSEMBL version 68 genomes added
* SnpEff 'countReads' count number of reads and bases (form a BAM file) on each gene, transcript, exon, intron, etc.
* SnpEff Intron and Intergenic annotations improved.


**Version: 3.0, revision 'f' (2012-08-23).**

* GATK output format compatibility option: '-o gatk'
* Fixed problem when parsing comment after GFF headers.
* Added GENCODE tags for GTF parsing
* Splice site analysis tools
* Analysis of U12 branch sites.
* Minor problems caused by empty VCF headers solved.
* Fixed bug in calculation of degenerate sites.
* Fixed problem in canonical transcripts.
* Plasmodium falciparum hand curated versions (by Daniel Park, Broad): Pf3D7v72 and Pf3D7v90
* Maven project, created by Louis Letourneau.
* Project source code changed to SVN (Louis Letourneau).
* Databases will be 'backwards compatible' from now on.
* New format for VCF files: added CDS length in amino acid (AA_LEN field).
* Canonical transcript filter (command line option "-canon").
* Improved GenBank parsing.
* SnpSift 'dbnsfp': Annotate using dbNSFP (Louis Letourneau).
* SnpSift 'gwasCat': Added GWA catalog annotations.
* SnpSift 'extractFields': extract fields to TXT files (tab separated)
* SnpSift 'sift': Annotate using SIFT database.
* SnpSift 'Annotate' and 'AnnMem': Now support to add all fields in a VCF file for annotations.


**Version: 2.1b (2012-04-26).**

* Revision "2.1c" : Maven project (by Louis Letourneau)
* Revision "2.1c" : Improved Galaxy wrappers (by Peter briggs)
* Revision "2.1b" : Improved RefSeq parsing
* Revision "2.1a" : Multi-thread race condition solved.
* **Note** If you are using **hg19**, it is recommended to download the latest database (due to improved RefSeq parsing in 2.1b).
* Added multi-threaded support (command line option '-t').
* GenBank support for building databases. See details [here](se_build_db.md#option-4-building-a-database-from-genbank-files).
* Config file simplified
* E.Coli database added
* Galaxy download database option added.
* Added all ENSEMBL version 66 genomes
* Database 'download' issue solved. Apparently SourceForge servers were choking on URL that had double slashes, this should not happen. Implemented a workaround.
* SnpSift GWAS catalog: Annotate using [GWAS Catalog](http://www.genome.gov/gwastudies/).
* SnpSift: Added 'varType' to annotate variant type (SNP/MNP/INS/DEL), as well as HOM/HET if possible.
* Faster VCF processing.
				

**Version: 2.0.5 (2011-11-25).**

* Support for RARE amino acids (see details [here](se_inputoutput.md#details-about-rare-amino-acid-effect))
* Database for Soybean (Glycine max) added


**Version: 2.0.5 (2011-11-25).**

* Database download command, e.g. "java -jar snpEff.jar download GRCH37.64"
* Added all ENSEMBL version 65 genomes
* RefSeq annotations support added.
* Rogue transcript filter: By default SnpEff filters out some suspicious transcripts from annotations databases. This should improve false positive rates.
* Amino acid changes in HGVS style (VCF output)
* Optimized parsing for VCF files with large number of samples (genotypes).
* Option to suppress summary calculation ('-noStats'), can speed up processing considerably in cases where VCF files have hundreds or thousands of genotype fields.
* Option '-onlyCoding' is set to 'auto' to reduce number of false positives (see next).
* Option '-onlyCoding' can be assigne a value: If value is 'true', report only 'protein_coding' transcripts as protein coding changes. If 'false', report all transcript as if they were conding. Default: Auto, i.e. if transcripts any marked as 'protein_coding' the set it to 'true', if no transcripts are marked as 'protein_coding' then set it to 'false'.
* Added BED output format. This is usefull to annotate the output of a Chip-Seq experiment (e.g. after performing peak calling with MACS, you want to know where the peaks hit).
* Added BED Annotation output format. This is usefull to get all annotation intervals that intersect a set of variants (or genomic regions).
* **SnpSift filter**:
    
     * Added generic index ('*') for variables, genotypes and effects. E.g.: ( 'GEN\[\*].GT = '1|1' )
     * Added support for 'EFF' and subfields (from SnpEff processed files). E.g.: ( EFF\[\*].EFFECT = 'NON_SYNONYMOUS_CODING' )
    
* **SnpSift intidx**: 
    Designed to extract a small number of intervals from huge VCF files. 
    Added indexing using  memory mapped I/O files for retrieving intervals from huge VCF files. 
    Works really fast! 


**Version: 2.0.3 (2011-10-08)**

* Functional classes added in VCF output (i.e. NONE, SILENT, MISSENSE, NONSENSE)
* Added MODIFIER effect 'impact'.
* Rice genome added.
* Added all ENSEMBL version 64 genomes.
* Several minor issues solved.
* Report usage statistics to server (can be disabled using '-noLog' options).


**Version: 2.0.2 (2011-09-09)**

* VCF output format
* **[GATK](http://www.broadinstitute.org/gsa/wiki/index.php/The_Genome_Analysis_Toolkit)** integration. Now you can use SnpEff from GATK's [VariantAnnotator](http://www.broadinstitute.org/gsa/wiki/index.php/Adding_Genomic_Annotations_Using_SnpEff_and_VariantAnnotator).
* Default input file is STDIN. I.e. inputFile parameter can be ommited now.
* Gene list outputs to a TXT file (tab separated) instead of the summary (HTML) file.
* Command line format changed for various options
* Option '-sort' deprecated.


**Version: 1.9.6 (2011-08-08)**

* Ensembl genomes v63 added.
* **Warning!** Genome names changed to agree with Ensembl naming convention, here are the names:

    Full name              | Short name
    ---------------------- | ------------
    Ailuropoda_melanoleuca |  ailMel1.63
    Anolis_carolinensis    |  AnoCar2.0.63
    Bos_taurus             |  Btau_4.0.63
    Caenorhabditis_elegans |  WS220.63
    Callithrix_jacchus     |  C_jacchus3.2.1.63
    Canis_familiaris       |  BROADD2.63
    Cavia_porcellus        |  cavPor3.63
    Choloepus_hoffmanni     |  choHof1.63
    Ciona_intestinalis      |  JGI2.63
    Ciona_savignyi          |  CSAV2.0.63
    Danio_rerio             |  Zv9.63
    Dasypus_novemcinctus    |  dasNov2.63
    Dipodomys_ordii         |  dipOrd1.63
    Drosophila_melanogaster |  BDGP5.25.63
    Echinops_telfairi       |  TENREC.63
    Equus_caballus          |  EquCab2.63
    Erinaceus_europaeus     |  HEDGEHOG.63
    Felis_catus             |  CAT.63
    Gallus_gallus           |  WASHUC2.63
    Gasterosteus_aculeatus  |  BROADS1.63
    Gorilla_gorilla         |  gorGor3.63
    Homo_sapiens            |  GRCh37.63
    Loxodonta_africana      |  loxAfr3.63
    Macaca_mulatta          |  MMUL_1.63
    Macropus_eugenii        |  Meug_1.0.63
    Meleagris_gallopavo     |  UMD2.63
    Microcebus_murinus      |  micMur1.63
    Monodelphis_domestica   |  BROADO5.63
    Mus_musculus            |  NCBIM37.63
    Myotis_lucifugus        |  Myoluc2.0.63
    Nomascus_leucogenys     |  Nleu1.0.63
    Ochotona_princeps       |  pika.63
    Ornithorhynchus_anatinus |  OANA5.63
    Oryctolagus_cuniculus    |  oryCun2.63
    Oryzias_latipes          |  MEDAKA1.63
    Otolemur_garnettii       |  BUSHBABY1.63
    Pan_troglodytes          |  CHIMP2.1.63
    Pongo_abelii             |  PPYG2.63
    Procavia_capensis        |  proCap1.63
    Pteropus_vampyrus        |  pteVam1.63
    Rattus_norvegicus        |  RGSC3.4.63
    Saccharomyces_cerevisiae |  EF3.63
    Sorex_araneus            |  COMMON_SHREW1.63
    Spermophilus_tridecemlineatus |  SQUIRREL.63
    Sus_scrofa               |  Sscrofa9.63
    Taeniopygia_guttata      |  taeGut3.2.4.63
    Takifugu_rubripes        |  FUGU4.63
    Tarsius_syrichta         |  tarSyr1.63
    Tetraodon_nigroviridis   |  TETRAODON8.63
    Tupaia_belangeri         |  TREESHREW.63
    Tursiops_truncatus       |  turTru1.63
    Vicugna_pacos            |  vicPac1.63
    Xenopus_tropicalis       |  JGI_4.2.63

* Problems with VCF heterozygous: Fixed
* Problems parsing some InDels: Fixed
* Error conditions on deletion at the border between UTR and Exon: Fixed
* Problems reporting some CDS relative positions: Fixed
* Some issues related to distance calculation on Downstream genes on negative strands: Fixed


**Version: 1.9.5 (2011-03-10)**

* Variants per gene table.
* Improvements in summary report.
* Improved GFF3 parsing.
* Several genomes added.


**Version: 1.9 (2011-03-10)**
Features recently added:

* Improved command line
* Genomes added (Arabidopsis) : alyrata107, athaliana130
* Genomes added (all ENSEMBL version 61): 
  
          ailmel1.61, anoCar2.0.61, btau4.0.61, bushBaby1.61, calJac3.2.1.61, canFam2.61, 
          cat1.61, cavPor3.61, ce.WS220.61, chimp2.1.61, choHof1.61, cInt2.61, cSav2.0.61, 
          danRer9.61, dasNov2.61, dipOrd1.61, dm5.25.61, equCab2.61, eriEur1.61, fugu4.61, 
          gacu1.61, ggallus2.61, gorGor3.61, hg37.61, loxAfr3.61, medaka1.61, meug1.0.61, 
          micMur1.61, mm37.61, mmul1.61, monDom5.61, myoLuc1.61, oana5.61, ochPri2.61, 
          oryCun2.61, ppyg2.61, proCap1.61, pteVam1.61, rat3.4.61, sacCer2.61, sorAra1.61, 
          speTri1.61, sScrofa9.61, taeGut3.2.4.61, tarSyr1.61, tenrec1.61, tetraodon8.61, 
          tupBel1.61, turkey.UMD2.61, turTru1.61, vicPac1.61, xtrop4.1.61

* Genomes added (Flybase): dm5.34
* Genomes added (legacy hg18): hg36.54
* Improved summary and statistics
* Supports BED format: if you just need to check where an interval hits (e.g. exon, intron, genes, etc.)
* Added support for GTF 2.2 format
* Improved robustness of GFF3 and GFF2 parsing
* Improved splice site detection: SPLICE_SITE_DONOR and SPLICE_SITE_ACCEPTOR
* Improved support for insertions and deletions: CODON_INSERTION, CODON_CHANGE_PLUS_CODON_INSERTION, CODON_DELETION, CODON_CHANGE_PLUS_CODON_DELETION 
* Improved support for large deletions: EXON_DELETED and UTR_DELETED
* Added suport for INTRON_CONSERVED and INTERGENIC_CONSERVED intervals (available in GTF 2.2 files)
* Added support for ambiguous sequences in exons (e.g. sequences that have "N")
* Database dump support: 

         java -jar snpEff.jar dump genome_version

* CDS testing support: 

         java -jar snpEff.jar cds genome_version cds.fasta


**Older features **

* Show DNA and amino acid sequence before and after change: option "-a, --around", e.g. "-a 5" shows 5 codons around sequence change)
* **WARNING**: Since version 1.7 snpEff assumes one-based coordinates (i.e. option "-1" is the default instead of "-0")
* **WARNING**: Since version 1.7 snpEff does not sort sequence changes. You should use option "-sort" if you want that.
* Genomes added (Pseudomonas): Pseudomonas aeruginosa (paeru.PA01 and paeru.PA14) and Pseudomonas fluorescens (pfluo.SBW25.NC_009444 and pfluo.SBW25.NC_012660)
* Genomes supported (all ENSEMBL version 60): 
        
            ailMel1.60, amel2, anoCar1.0.60, btau4.0.59, btau4.0.60, bushBaby1.60,
            calJac3.2.1.60, canFam2.59, canFam2.60, cat1.60, cavPor3.60, ce6, ce.WS210.60,
            chimp2.1.59, chimp2.1.60, choHof1.60, cInt2.60, cSav2.0.60, danRer6, danRer8.59,
            danRer9.60, dasNov2.60, dipOrd1.60, dm3, dm5.12, dm5.22, dm5.25.59, dm5.25.60,
            dm5.30, dm5.31, equCab2.60, eriEur1.60, fugu4.60, gacu1.60, ggallus2.59, ggallus2.60,
            gorGor3.60, hg37, hg37.59, hg37.60, loxAfr3.60, medaka1.60, meug1.0.60, micMur1.60,
            mm37, mm37.59, mm37.60, mmul1.60, monDom5.60, myoLuc1.60, oana5.60, ochPri2.60,
            oryCun2.60, ppyg2.60, proCap1.60, pteVam1.60, rat3.4.59, rat3.4.60, sacCer2,
            sacCer2.59, sacCer2.60, SIVmac239, sorAra1.60, speTri1.60, sScrofa9.60,
            taeGut3.2.4.60, tarSyr1.60, tenrec1.60, testCase, tetraodon8.60, tupBel1.60,
            turTru1.60, vicPac1.60, xtrop4.1.60

* VCF4 input format is now supported
* Support new genome Apis Mellifera
* Statistics and plots
* Filter intervals (only analyze selected intervals)
* One-based and zero-based positions for input and output (as well arbitrary offsets)
* Support for heterozygous SNPs (e.g. A/W)
* Predicts insertions and deletions (FRAME_SHIFT)
* Supports GFF format when building databases.
* Added: Multiple nucleotide polymorphisms (MNPs)
* New format shows SNP quality and coverage.
* Can filter SNPs, InDels and MNPs based on quality, coverage and zygosity (Hom/Het).
