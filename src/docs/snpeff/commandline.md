# Commands &amp; command line options

SnpEff has several 'commands' that can be used for different annotations.
The default command is `'eff'` used to annotate variants.

**Help:** In order to see all available commands, you can run SnpEff without any arguments:

```
# This will show a 'help' message
java -jar snpEff.jar
```

## Commands

Here is a list of what each command does:

Command       |  Meaning
------------- | ------------------
`eff | ann`   | This is the default command. It is used for annotating variant filed (e.g. VCF files).
`build`         | Build a SnpEff database from reference genome files (FASTA, GTF, etc.).
`buildNextProt` | Build NextProt database using XML files
`cds`           | Compare CDS sequences calculated form a SnpEff database to the one in a FASTA file. Used for checking databases correctness (invoked automatically when building a database).
`closest`       | Annotate the closest genomic region.
`count`         | Count how many intervals (from a BAM, BED or VCF file) overlap with each genomic interval.
`databases`     | Show currently available databases (from local config file).
`download`      | Download a SnpEff database.
`dump`          | Dump to STDOUT a SnpEff database (mostly used for debugging).
`genes2bed`     | Create a bed file from a genes list.
`len`           | Calculate total genomic length for each marker type.
`protein`       | Compare protein sequences calculated form a SnpEff database to the one in a FASTA file. Used for checking databases correctness. (invoked automatically when building a database).
spliceAnalysis``| Perform an analysis of splice sites. Experimental feature.

### Common options: All commands

The general help shows some options that are available to all commands. For instance, at the time of writing, the common options are under "Generic options" and "Database options" are these:
```
$ java -jar snpEff.jar
SnpEff version SnpEff 4.1 (build 2015-01-07), by Pablo Cingolani
Usage: snpEff [command] [options] [files]

Run 'java -jar snpEff.jar command' for help on each specific command

Available commands: 
	[eff|ann]                    : Annotate variants / calculate effects (you can use either 'ann' or 'eff', they mean the same). Default: ann (no command or 'ann').
	build                        : Build a SnpEff database.
	buildNextProt                : Build a SnpEff for NextProt (using NextProt's XML files).
	cds                          : Compare CDS sequences calculated form a SnpEff database to the one in a FASTA file. Used for checking databases correctness.
	closest                      : Annotate the closest genomic region.
	count                        : Count how many intervals (from a BAM, BED or VCF file) overlap with each genomic interval.
	databases                    : Show currently available databases (from local config file).
	download                     : Download a SnpEff database.
	dump                         : Dump to STDOUT a SnpEff database (mostly used for debugging).
	genes2bed                    : Create a bed file from a genes list.
	len                          : Calculate total genomic length for each marker type.
	protein                      : Compare protein sequences calculated form a SnpEff database to the one in a FASTA file. Used for checking databases correctness.
	spliceAnalysis               : Perform an analysis of splice sites. Experimental feature.

Generic options:
	-c , -config                 : Specify config file
	-d , -debug                  : Debug mode (very verbose).
	-dataDir <path>              : Override data_dir parameter from config file.
	-download                    : Download a SnpEff database, if not available locally. Default: true
	-nodownload                  : Do not download a SnpEff database, if not available locally.
	-noShiftHgvs                 : Do not shift variants towards most 3-prime position (as required by HGVS).
	-h , -help                   : Show this help and exit
	-noLog                       : Do not report usage statistics to server
	-t                           : Use multiple threads (implies '-noStats'). Default 'off'
	-q ,  -quiet                 : Quiet mode (do not show any messages or errors)
	-v , -verbose                : Verbose mode

Database options:
	-canon                       : Only use canonical transcripts.
	-interval                    : Use a custom intervals in TXT/BED/BigBed/VCF/GFF file (you may use this option many times)
	-motif                       : Annotate using motifs (requires Motif database).
	-nextProt                    : Annotate using NextProt (requires NextProt database).
	-noGenome                    : Do not load any genomic database (e.g. annotate using custom files).
	-noMotif                     : Disable motif annotations.
	-noNextProt                  : Disable NextProt annotations.
	-onlyReg                     : Only use regulation tracks.
	-onlyProtein                 : Only use protein coding transcripts. Default: false
	-onlyTr <file.txt>           : Only use the transcripts in this file. Format: One transcript ID per line.
	-reg <name>                  : Regulation track to use (this option can be used add several times).
	-ss , -spliceSiteSize <int>  : Set size for splice sites (donor and acceptor) in bases. Default: 2
	-strict                      : Only use 'validated' transcripts (i.e. sequence has been checked). Default: false
```

## `ANN` command: Variant annotations

In order to see a help message for a particular command, you can run the command without any arguments or use `-help` command line option:

```
# This will show a 'help' message for the 'ann' (aka 'eff') command
$ java -jar snpEff.jar ann

snpEff version SnpEff 4.1 (build 2015-01-07), by Pablo Cingolani
Usage: snpEff [eff] [options] genome_version [input_file]


	variants_file                   : Default is STDIN


Options:
	-chr <string>                   : Prepend 'string' to chromosome name (e.g. 'chr1' instead of '1'). Only on TXT output.
	-classic                        : Use old style annotations instead of Sequence Ontology and Hgvs.
	-download                       : Download reference genome if not available. Default: true
	-i <format>                     : Input format [ vcf, bed ]. Default: VCF.
	-fileList                       : Input actually contains a list of files to process.
	-o <format>                     : Output format [ vcf, gatk, bed, bedAnn ]. Default: VCF.
	-s , -stats                     : Name of stats file (summary). Default is 'snpEff_summary.html'
	-noStats                        : Do not create stats (summary) file
	-csvStats                       : Create CSV summary file instead of HTML

Results filter options:
	-fi , -filterInterval  <file>   : Only analyze changes that intersect with the intervals specified in this file (you may use this option many times)
	-no-downstream                  : Do not show DOWNSTREAM changes
	-no-intergenic                  : Do not show INTERGENIC changes
	-no-intron                      : Do not show INTRON changes
	-no-upstream                    : Do not show UPSTREAM changes
	-no-utr                         : Do not show 5_PRIME_UTR or 3_PRIME_UTR changes
	-no EffectType                  : Do not show 'EffectType'. This option can be used several times.

Annotations options:
	-cancer                         : Perform 'cancer' comparisons (Somatic vs Germline). Default: false
	-cancerSamples <file>           : Two column TXT file defining 'original \t derived' samples.
	-formatEff                      : Use 'EFF' field compatible with older versions (instead of 'ANN').
	-geneId                         : Use gene ID instead of gene name (VCF output). Default: false
	-hgvs                           : Use HGVS annotations for amino acid sub-field. Default: true
	-lof                            : Add loss of function (LOF) and Nonsense mediated decay (NMD) tags.
	-noHgvs                         : Do not add HGVS annotations.
	-noLof                          : Do not add LOF and NMD annotations.
	-noShiftHgvs                    : Do not shift variants according to HGVS notation (most 3prime end).
	-oicr                           : Add OICR tag in VCF file. Default: false
	-sequenceOntology               : Use Sequence Ontology terms. Default: true

Generic options:
	-c , -config                 : Specify config file
	-d , -debug                  : Debug mode (very verbose).
	-dataDir <path>              : Override data_dir parameter from config file.
	-download                    : Download a SnpEff database, if not available locally. Default: true
	-nodownload                  : Do not download a SnpEff database, if not available locally.
	-noShiftHgvs                 : Do not shift variants towards most 3-prime position (as required by HGVS).
	-h , -help                   : Show this help and exit
	-noLog                       : Do not report usage statistics to server
	-t                           : Use multiple threads (implies '-noStats'). Default 'off'
	-q ,  -quiet                 : Quiet mode (do not show any messages or errors)
	-v , -verbose                : Verbose mode

Database options:
	-canon                       : Only use canonical transcripts.
	-interval                    : Use a custom intervals in TXT/BED/BigBed/VCF/GFF file (you may use this option many times)
	-motif                       : Annotate using motifs (requires Motif database).
	-nextProt                    : Annotate using NextProt (requires NextProt database).
	-noGenome                    : Do not load any genomic database (e.g. annotate using custom files).
	-noMotif                     : Disable motif annotations.
	-noNextProt                  : Disable NextProt annotations.
	-onlyReg                     : Only use regulation tracks.
	-onlyProtein                 : Only use protein coding transcripts. Default: false
	-onlyTr <file.txt>           : Only use the transcripts in this file. Format: One transcript ID per line.
	-reg <name>                  : Regulation track to use (this option can be used add several times).
	-ss , -spliceSiteSize <int>  : Set size for splice sites (donor and acceptor) in bases. Default: 2
	-strict                      : Only use 'validated' transcripts (i.e. sequence has been checked). Default: false
	-ud , -upDownStreamLen <int> : Set upstream downstream interval length (in bases)

```

### Annotation Filters

SnpEff supports filter of output results by using combinations of the following command line options:

!!! warning
    Output filters can be implemented using `SnpSift filter`, which allows to create more flexible and complex filters.

Command line option | Meaning
------------------- | -----------
`-no-downstream`      | Do not show DOWNSTREAM changes
`-no-intergenic`      | Do not show INTERGENIC changes
`-no-intron`          | Do not show INTRON changes
`-no-upstream`        | Do not show UPSTREAM changes
`-no-utr`             | Do not show 5_PRIME_UTR or 3_PRIME_UTR changes
`-no <effect_type>`      | Do not show `effect_type` (it can be used several times), e.g: `-no INTERGENIC -no SPLICE_SITE_REGION`

####  Disabling Upstream and Downstream annotations

You can change the default upstream and downstream interval size (default is 5K) using the `-ud size_in_bases` option.
This also allows to eliminate any upstream and downstream effect by using "-ud 0".

Example: Make upstream and downstream size zero (i.e. do not report any upstream or downstream effect).

```
java -Xmx8g -jar snpEff.jar -ud 0 GRCh37.75 test.chr22.vcf > test.chr22.ann.vcf
```

#### Splice site size

You can change the default splice site size (default is 2 bases) using the `-spliceSiteSize size_in_bases` option.

Example: Make splice sites four bases long

```
java -Xmx8g -jar snpEff.jar -spliceSiteSize 4 GRCh37.75 test.chr22.vcf > test.chr22.ann.vcf
```

#### Adding your own annotations

SnpEff allows user defined intervals to be annotated.
This is achieved using the `-interval file.bed` command line option, which can be used multiple times in the same command line
(it accepts files in TXT, BED, BigBed, VCF, GFF formats).
Any variant that intersects an interval defined in those files, will be annotated using the "name" field (fourth column) in the input bed file.

Example: We create our own annotations in `my_annotations.bed`
```
$ cat my_annotations.bed
1	10000	20000	MY_ANNOTATION

$ cat test.vcf
1	10469	.	C	G	365.78	PASS	AC=30;AF=0.0732

Annotate (output edited for readability)

$ java -Xmx8g -jar snpEff.jar -interval my_annotations.bed GRCh37.66 test.vcf
1    10469    .    C    G    365.78    PASS    AC=30;AF=0.0732;
                                               ANN=G|upstream_gene_variant|MODIFIER|DDX11L1|ENSG00000223972|transcript|ENST00000456328|processed_transcript||n.-1C>G|||||1400|
                                               ...
                                               G|custom|MODIFIER|||CUSTOM&my_annotations|MY_ANNOTATION|||||||||

```

Notice that the variant was annotated using "MY_ANNOTATION" in the `ANN` field.

### Selecting transcripts

#### Canonical transcripts

SnpEff allows to annotate using canonical transcripts by using `-canon` command line option.

!!! warning
    Canonical transcripts are defined as the longest CDS of amongst the protein coding transcripts in a gene.
    If none of the transcripts in a gene is protein coding, then it is the longest cDNA.

!!! warning
    Although this seems to be the standard definitions of "canonical transcript", there is no warranties that what SnpEff considers a canonical transcript
    will match exactly what UCSC or ENSEMBL consider a canonical transcript.

Example on how to use canonical transcripts annotations:

```
java -Xmx8g -jar snpEff.jar -v -canon GRCh37.75 examples/test.chr22.vcf > file.ann.canon.vcf
```

In order to get a list of canonical transcripts, you can use the `-d` (debug) command line option. E.g.:
```
$ java -Xmx8g -jar snpEff.jar -d -v -canon GRCh37.75 test.vcf
00:00:00.000    Reading configuration file 'snpEff.config'
00:00:00.173    done
00:00:00.173    Reading database for genome version 'GRCh37.66'
00:00:02.834    done
00:00:02.845    Filtering out non-canonical transcripts.
00:00:03.219    Canonical transcripts:
                geneName        geneId          transcriptId    cdsLength
                GGPS1           ENSG00000152904 ENST00000488594 903
                RP11-628K18.1.1 ENSG00000235112 ENST00000430808 296
                MIPEPP2         ENSG00000224783 ENST00000422560 1819
                FEN1P1          ENSG00000215873 ENST00000401028 1145
                AL591704.7.1    ENSG00000224784 ENST00000421658 202
                CAPNS1P1        ENSG00000215874 ENST00000401029 634
                ST13P20         ENSG00000215875 ENST00000447996 1061
                NCDN            ENSG00000020129 ENST00000373243 2190
                RP11-99H8.1.1   ENSG00000226208 ENST00000423187 432
                AL391001.1      ENSG00000242652 ENST00000489859 289
...
```

#### Selected list of transcripts

SnpEff allows you to provide a list of transcripts to use for annotations by using the `-onlyTr file.txt` and providing a file with one transcript ID per line.
Any other transcript will be ignored.

```
java -Xmx8g -jar snpEff.jar -onlyTr my_transcripts.txt GRCh37.75 test.chr22.vcf > test.chr22.ann.vcf
```

#### Finltering by transcript tags

In some cases the genome files contain tags, for example, here are from GTF lines from ENSEMBL's GRCh38 (MANE release 1.0):

Note, GTF lines edited for readability:
```
chr1  .  transcript  1471765  1497848  .  +  .  transcript_id "ENST00000673477.1";  gene_name "ATAD3B"; tag "MANE_Select";
chr1  .  transcript  3069203  3438621  .  +  .  transcript_id "ENST00000270722.10"; gene_name "PRDM16"; tag "MANE_Select";
chr1  .  transcript  2476289  2505532  .  +  .  transcript_id "ENST00000378486.8";  gene_name "PLCH2";  tag "MANE_Select";
chr1  .  transcript  9292894  9369532  .  +  .  transcript_id "ENST00000328089.11"; gene_name "SPSB1";  tag "MANE_Select";
chr1  .  transcript  9035106  9069635  .  -  .  transcript_id "ENST00000377424.9";  gene_name "SLC2A5"; tag "MANE_Select";
chr1  .  transcript  8861000  8878686  .  -  .  transcript_id "ENST00000234590.10"; gene_name "ENO1";   tag "MANE_Select"; tag "CAGE_supported_TSS";
```

Command line arguments for tag selection:

- `-tag <tag_name>`: Only use transcripts that match `<tag_name>`
- `-tagNo <tag_name>`: Filter out transcripts that match `<tag_name>`


!!! info
	Both `-tag` and `-tagNo` options can be specified multiple times

### Other options

#### Logging

SnpEff will try to log usage statistics to our "log server".
This is useful for us to understand user's needs and have some statistics on what users are doing with the program (e.g. decide whether a command or option is useful or not).
Logging can be deactivated by using the `-noLog` command line option.

#### Annotating selected intervals

You can use the `-fi intervals.bed` command line option (filterInterval). For instance, let's assume you have an interval file 'intervals.bed':
```
2L	10000	10999
2L	12000	12999
2L	14000	14999
2L	16000	16999
2L	18000	18999 
```
In order to get only variants matching your intervals, you can use the command:

    $ java -Xmx8g -jar snpEff.jar -fi intervals.bed GRCh38.76 test.chr22.vcf

#### Gene ID instead of gene names

You can obtain gene IDs instead of gene names by using the command line option `-geneId`.
Note: This is only for the old 'EFF' field ('ANN' field always shows both gene name and gene ID).

Example:

    $ java -Xmx8g -jar snpEff.jar -geneId GRCh37.66 test.vcf 
    1  902128  3617  C  T  .  PASS  AC=80;EFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|gCt/gTt|A43V|576|ENSG00000187583|protein_coding|CODING|ENST00000379407|2|1),...

Note: The gene 'PLEKHN1' was annotated as 'ENSG00000187583'.

#### Speed up options: No statistics

In order to speed up the annotation process, you can de-activate the statistics.
Calculating statistics can take a significant amount of time, particularly if there are hundreds or thousands of samples in the (multi-sample) VCF file.
The command line option `-noStats` disables the statistics and may result in a significant speedup.

#### HGVS / Classic notation

SnpEff uses [HGVS notation](http://www.hgvs.org/), which is somewhat popular amongst clinicians.

You can switch to the old (deprecated) annotaions format, using the command line option `-classic`.

### Compressed files

SnpEff will automatically open gzip compresssed files, even if you don't specify the '.gz' extension.
Example

```
# Create compressed version of the examples files
cp examples/test.chr22.vcf my.vcf

# Compress it
gzip my.vcf 

# Annotate the comressed file
java -Xmx8g -jar snpEff.jar GRCh37.75 my.vcf.gz > my.ann.vcf
```

### Streaming files

!!! info
    You can use "`-`" as input file name to specify `STDIN`. As of version 4.0 onwards `STDIN` is the default, so using no file name at all, also means `STDIN`.
	
For example, you can easily stream files like this:
```
# These three commands are the same

# Using STDIN (pipe), implicit (no input file name)
cat test.chr22.vcf | java -Xmx8g -jar snpEff.jar hg19 > test.chr22.ann.vcf

# Using STDIN (pipe), exlicit '-' input file name
cat test.chr22.vcf | java -Xmx8g -jar snpEff.jar hg19 - > test.chr22.ann.vcf

# Using explicit file name
java -Xmx8g -jar snpEff.jar hg19 test.chr22.vcf > test.chr22.ann.vcf
```
