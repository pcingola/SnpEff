# Integration: GATK and Galaxy

SnpEff is integrated with other tools commonly used in sequencing data analysis pipelines.
Most notably [Galaxy](http://galaxyproject.org/) and Broad Institute's Genome Analysis Toolkit ([GATK](http://www.broadinstitute.org/gatk/)) projects support SnpEff.
By using standards, such as VCF, SnpEff makes it easy to integrate with other programs.

### Integration: GATK

In order to make sure SnpEff and GATK understand each other, you must activate GATK compatibility in SnpEff by using the `-o gatk` command line option.
The reason for using '-o gatk' is that, even though both GATK and SnpEff use VCF format, SnpEff has recently updated the `EFF` sub-field format and this might cause some trouble (since GATK still uses the original version).

!!! warning
    GATK only picks one effect.
    Indeed, the GATK team decided to only report the effect having the highest impact.
    This was done intentionally for the sake of brevity, in a 'less is more' spirit.
    You can get the full effect by using snpEff independently, instead of using it within GATK framework.

Script example: In this example we combine SnpEff and GATK's VariantAnnotator (you can find this script in `snpEff/scripts/` directory of the distribution)
```
#!/bin/sh

#-------------------------------------------------------------------------------
# Files
#-------------------------------------------------------------------------------

in=$1                                                   # Input VCF file
eff=`dirname $in`/`basename $in .vcf`.ann.vcf        # SnpEff annotated VCF file
out=`dirname $in`/`basename $in .vcf`.gatk.vcf          # Output VCF file (annotated by GATK)

ref=$HOME/snpEff/data/genomes/hg19.fa                   # Reference genome file
dict=`dirname $ref`/`basename $ref .fa`.dict            # Reference genome: Dictionary file

#-------------------------------------------------------------------------------
# Path to programs and libraries
#-------------------------------------------------------------------------------

gatk=$HOME/tools/gatk/GenomeAnalysisTK.jar
picard=$HOME/tools/picard/
snpeff=$HOME/snpEff/snpEff.jar

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Create genome index file
echo
echo "Indexing Genome reference FASTA file: $ref"
samtools faidx $ref

# Create dictionary
echo
echo "Creating Genome reference dictionary file: $dict"
java -jar $picard/CreateSequenceDictionary.jar R= $ref O= $dict

# Annotate
echo
echo "Annotate using SnpEff"
echo "    Input file  : $in"
echo "    Output file : $eff"
java -Xmx8g -jar $snpeff -c $HOME/snpEff/snpEff.config -v -o gatk hg19 $in > $eff

# Use GATK
echo
echo "Annotating using GATK's VariantAnnotator:"
echo "    Input file  : $in"
echo "    Output file : $out"
java -Xmx8g -jar $gatk \
    -T VariantAnnotator \
    -R $ref \
    -A SnpEff \
    --variant $in \
    --snpEffFile $eff \
    -L $in \
    -o $out
```

!!! warning
    **Important:** In order for this to work, GATK requires that the Genome Reference file should have the chromosomes in karotyping order
    (largest to smallest chromosomes, followed by the X, Y, and MT). Your VCF file should also respect that order.

Now we can use the script:
```
$ ~/snpEff/scripts/gatk.sh zzz.vcf

Indexing Genome reference FASTA file: /home/pcingola/snpEff/data/genomes/hg19.fa

Creating Genome reference dictionary file: /home/pcingola/snpEff/data/genomes/hg19.dict
[Fri Apr 12 11:23:12 EDT 2013] net.sf.picard.sam.CreateSequenceDictionary REFERENCE=/home/pcingola/snpEff/data/genomes/hg19.fa OUTPUT=/home/pcingola/snpEff/data/genomes/hg19.dict    TRUNCATE_NAMES_AT_WHITESPACE=true NUM_SEQUENCES=2147483647 VERBOSITY=INFO QUIET=false VALIDATION_STRINGENCY=STRICT COMPRESSION_LEVEL=5 MAX_RECORDS_IN_RAM=500000 CREATE_INDEX=false CREATE_MD5_FILE=false
[Fri Apr 12 11:23:12 EDT 2013] Executing as pcingola@localhost.localdomain on Linux 3.6.11-4.fc16.x86_64 amd64; OpenJDK 64-Bit Server VM 1.6.0_24-b24; Picard version: 1.89(1408)
[Fri Apr 12 11:23:12 EDT 2013] net.sf.picard.sam.CreateSequenceDictionary done. Elapsed time: 0.00 minutes.
Runtime.totalMemory()=141164544
To get help, see http://picard.sourceforge.net/index.shtml#GettingHelp
Exception in thread "main" net.sf.picard.PicardException: /home/pcingola/snpEff/data/genomes/hg19.dict already exists.  Delete this file and try again, or specify a different output file.
        at net.sf.picard.sam.CreateSequenceDictionary.doWork(CreateSequenceDictionary.java:114)
        at net.sf.picard.cmdline.CommandLineProgram.instanceMain(CommandLineProgram.java:177)
        at net.sf.picard.sam.CreateSequenceDictionary.main(CreateSequenceDictionary.java:93)

Annotate using SnpEff
    Input file  : zzz.vcf
    Output file : ./zzz.ann.vcf
00:00:00.000	Reading configuration file '/home/pcingola/snpEff/snpEff.config'
00:00:00.173	done
00:00:00.173	Reading database for genome version 'hg19' from file '/home/pcingola//snpEff/data/hg19/snpEffectPredictor.bin' (this might take a while)
00:00:11.860	done
00:00:11.885	Building interval forest
00:00:17.755	done.
00:00:18.391	Genome stats :
# Genome name                : 'Homo_sapiens (USCS)'
# Genome version             : 'hg19'
# Has protein coding info    : true
# Genes                      : 25933
# Protein coding genes       : 20652
# Transcripts                : 44253
# Avg. transcripts per gene  : 1.71
# Protein coding transcripts : 36332
# Cds                        : 365442
# Exons                      : 429543
# Exons with sequence        : 409789
# Exons without sequence     : 19754
# Avg. exons per transcript  : 9.71
# Number of chromosomes      : 50
# Chromosomes names [sizes]  : '1' [249250621]	'2' [243199373]	'3' [198022430]	'4' [191154276]	'5' [180915260]	'6' [171115067]	'7' [159138663]	'X' [155270560]	'8' [146364022]	'9' [141213431]	'10' [135534747]	'11' [135006516]	'12' [133851895]	'13' [115169878]	'14' [107349540]	'15' [102531392]	'16' [90354753]	'17' [81195210]	'18' [78077248]	'20' [63025520]	'Y' [59373566]	'19' [59128983]	'22' [51304566]	'21' [48129895]	'6_ssto_hap7' [4905564]	'6_mcf_hap5' [4764535]	'6_cox_hap2' [4734611]	'6_mann_hap4' [4679971]	'6_qbl_hap6' [4609904]	'6_dbb_hap3' [4572120]	'6_apd_hap1' [4383650]	'17_ctg5_hap1' [1574839]	'4_ctg9_hap1' [582546]	'Un_gl000220' [156152]	'19_gl000209_random' [145745]	'Un_gl000213' [139339]	'17_gl000205_random' [119732]	'Un_gl000223' [119730]	'4_gl000194_random' [115071]	'Un_gl000228' [114676]	'Un_gl000219' [99642]	'Un_gl000218' [97454]	'Un_gl000211' [93165]	'Un_gl000222' [89310]	'4_gl000193_random' [88375]	'7_gl000195_random' [86719]	'1_gl000192_random' [79327]	'Un_gl000212' [60768]	'1_gl000191_random' [50281]	'M' [16571]	
00:00:18.391	Predicting variants
00:00:20.267	Creating summary file: snpEff_summary.html
00:00:20.847	Creating genes file: snpEff_genes.txt
00:00:25.026	done.
00:00:25.036	Logging
00:00:26.037	Checking for updates...

Annotating using GATK's VariantAnnotator:
    Input file  : zzz.vcf
    Output file : ./zzz.gatk.vcf
INFO  11:23:41,316 ArgumentTypeDescriptor - Dynamically determined type of zzz.vcf to be VCF 
INFO  11:23:41,343 HelpFormatter - -------------------------------------------------------------------------------- 
INFO  11:23:41,344 HelpFormatter - The Genome Analysis Toolkit (GATK) v2.4-9-g532efad, Compiled 2013/03/19 07:35:36 
INFO  11:23:41,344 HelpFormatter - Copyright (c) 2010 The Broad Institute 
INFO  11:23:41,344 HelpFormatter - For support and documentation go to http://www.broadinstitute.org/gatk 
INFO  11:23:41,347 HelpFormatter - Program Args: -T VariantAnnotator -R /home/pcingola/snpEff/data/genomes/hg19.fa -A SnpEff --variant zzz.vcf --snpEffFile ./zzz.ann.vcf -L zzz.vcf -o ./zzz.gatk.vcf 
INFO  11:23:41,347 HelpFormatter - Date/Time: 2013/04/12 11:23:41 
INFO  11:23:41,348 HelpFormatter - -------------------------------------------------------------------------------- 
INFO  11:23:41,348 HelpFormatter - -------------------------------------------------------------------------------- 
INFO  11:23:41,353 ArgumentTypeDescriptor - Dynamically determined type of zzz.vcf to be VCF 
INFO  11:23:41,356 ArgumentTypeDescriptor - Dynamically determined type of ./zzz.ann.vcf to be VCF 
INFO  11:23:41,399 GenomeAnalysisEngine - Strictness is SILENT 
INFO  11:23:41,466 GenomeAnalysisEngine - Downsampling Settings: Method: BY_SAMPLE, Target Coverage: 1000 
INFO  11:23:41,480 RMDTrackBuilder - Loading Tribble index from disk for file zzz.vcf 
INFO  11:23:41,503 RMDTrackBuilder - Loading Tribble index from disk for file ./zzz.ann.vcf 
WARN  11:23:41,505 RMDTrackBuilder - Index file /data/pcingola/Documents/projects/snpEff/gatk_test/./zzz.ann.vcf.idx is out of date (index older than input file), deleting and updating the index file 
INFO  11:23:41,506 RMDTrackBuilder - Creating Tribble index in memory for file ./zzz.ann.vcf 
INFO  11:23:41,914 RMDTrackBuilder - Writing Tribble index to disk for file /data/pcingola/Documents/projects/snpEff/gatk_test/./zzz.ann.vcf.idx 
INFO  11:23:42,076 IntervalUtils - Processing 33411 bp from intervals 
INFO  11:23:42,125 GenomeAnalysisEngine - Creating shard strategy for 0 BAM files 
INFO  11:23:42,134 GenomeAnalysisEngine - Done creating shard strategy 
INFO  11:23:42,134 ProgressMeter - [INITIALIZATION COMPLETE; STARTING PROCESSING] 
INFO  11:23:42,135 ProgressMeter -        Location processed.sites  runtime per.1M.sites completed total.runtime remaining 
INFO  11:23:49,268 VariantAnnotator - Processed 9966 loci.

INFO  11:23:49,280 ProgressMeter -            done        3.34e+04    7.0 s        3.6 m    100.0%         7.0 s     0.0 s 
INFO  11:23:49,280 ProgressMeter - Total runtime 7.15 secs, 0.12 min, 0.00 hours 
INFO  11:23:49,953 GATKRunReport - Uploaded run statistics report to AWS S3 
```

### Integration: Galaxy

In order to install SnpEff in your own Galaxy server, you can use the `galaxy/*.xml` files provided in the main distribution.

This is a screen capture from a Galaxy server (click to enlarge):

[![](../images/snpEff_galaxy_small.png)](../images/snpEff_galaxy.png){: .center}

Installing SnpEff in a Galaxy server:
```
# Set variable to snpEff install dir (we only use it for this install script)
export snpEffDir="$HOME/snpEff"

# Go to your galaxy 'tools' dir
cd galaxy-dist/tools

# Create a directory and copy the XML config files from SnpEff's distribution
mkdir snpEff
cd snpEff/
cp $snpEffDir/galaxy/* .

# Create links to JAR files
ln -s $snpEffDir/snpEff.jar
ln -s $snpEffDir/SnpSift.jar

# Link to config file
ln -s $snpEffDir/snpEff.config

# Allow scripts execution
chmod a+x *.{pl,sh}

# Copy genomes information
cd ../..
cp $snpEffDir/galaxy/tool-data/snpEff_genomes.loc tool-data/

# Edit Galaxy's tool_conf.xml and add all the tools
vi tool_conf.xml

-------------------- Begin: Edit tool_conf.xml --------------------
<!-- 
    Add this section to tool_conf.xml file in your galaxy distribution

    Note: The following lines should be added at the end of the 
          file, right before "</toolbox>" line
-->
<section name="snpEff tools" id="snpEff_tools">
    <tool file="snpEff/snpEff.xml" />
    <tool file="snpEff/snpEff_download.xml" />
    <tool file="snpEff/snpSift_annotate.xml" />
    <tool file="snpEff/snpSift_caseControl.xml" />
    <tool file="snpEff/snpSift_filter.xml" />
    <tool file="snpEff/snpSift_int.xml" />
</section>
-------------------- End: Edit tool_conf.xml --------------------

# Run galaxy and check that the new menus appear
./run.sh
```
