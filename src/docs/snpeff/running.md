# Running SnpEff

We show some basic examples how to use SnpEff.

### Basic example: Installing SnpEff

Obviously the first step to use the program is to install it (for details, take a look at the [download page](../download.md).
You have to download the core program and then uncompress the ZIP file.
In Windows systems, you can just double click and copy the contents of the ZIP file to wherever you want the program installed.
If you have a Unix or a Mac system, the command line would be:

``` sh
# Download using wget
wget https://snpeff.blob.core.windows.net/versions/snpEff_latest_core.zip

# If you prefer to use 'curl' instead of 'wget', you can type:
#     curl -L https://snpeff.blob.core.windows.net/versions/snpEff_latest_core.zip > snpEff_latest_core.zip

# Install
unzip snpEff_latest_core.zip
```

### Basic example: Annotate using SnpEff

Let's assume you have a VCF file and you want to annotate the variants in that file.
An example file is provided in `examples/test.chr22.vcf` (this data is from the 1000 Genomes project, so the reference genome is the human genome GRCh37).

You can annotate the file by running the following command (as an input, we use a Variant Call Format (VCF) file available in SnpEff's `examples` directory).

```
java -Xmx8g -jar snpEff.jar GRCh37.75 examples/test.chr22.vcf > test.chr22.ann.vcf

# Here is how the output looks like
$ head examples/test.chr22.ann.vcf
##SnpEffVersion="4.1 (build 2015-01-07), by Pablo Cingolani"
##SnpEffCmd="SnpEff  GRCh37.75 examples/test.chr22.vcf "
##INFO=<ID=ANN,Number=.,Type=String,Description="Functional annotations: 'Allele | Annotation | Annotation_Impact | Gene_Name | Gene_ID | Feature_Type | Feature_ID | Transcript_BioType | Rank | HGVS.c | HGVS.p | cDNA.pos / cDNA.length | CDS.pos / CDS.length | AA.pos / AA.length | Distance | ERRORS / WARNINGS / INFO' ">
##INFO=<ID=LOF,Number=.,Type=String,Description="Predicted loss of function effects for this variant. Format: 'Gene_Name | Gene_ID | Number_of_transcripts_in_gene | Percent_of_transcripts_affected' ">
##INFO=<ID=NMD,Number=.,Type=String,Description="Predicted nonsense mediated decay effects for this variant. Format: 'Gene_Name | Gene_ID | Number_of_transcripts_in_gene | Percent_of_transcripts_affected' ">
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO
22	17071756	.	T	C	.	.	ANN=C|3_prime_UTR_variant|MODIFIER|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.*11A>G|||||11|,C|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397A>G|||||4223|
22	17072035	.	C	T	.	.   ANN=T|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.1406G>A|p.Gly469Glu|1666/2034|1406/1674|469/557||,T|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397G>A|||||3944|
22	17072258	.	C	A	.	.	ANN=A|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.1183G>T|p.Gly395Cys|1443/2034|1183/1674|395/557||,A|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397G>T|||||3721|
22	17072674	.	G	A	.	.	ANN=A|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.767C>T|p.Pro256Leu|1027/2034|767/1674|256/557||,A|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397C>T|||||3305|
```

As you can see, SnpEff added functional annotations in the `ANN` info field (eigth column in the VCF output file).

Details about the 'ANN' field format can be found in the [ANN Field](inputoutput.md#ann-field-vcf-output-files) section and in [VCF annotation about standard 'ANN' field](../adds/VCFannotationformat_v1.0.pdf).
Note: Older SnpEff version used 'EFF' field (details about the 'EFF' field format can be found in the [EFF Field](inputoutput.md#eff-field-vcf-output-files) section).

You can also annotate using the "verbose" mode (command line option `-v`), this makes SnpEff to show a lot of information which can be useful for debugging.

Here output is edited for brevity:

```
$ java -Xmx8g -jar snpEff.jar -v GRCh37.75 examples/test.chr22.vcf > test.chr22.ann.vcf

00:00:00.000	Reading configuration file 'snpEff.config'. Genome: 'GRCh37.75'
00:00:00.434	done
00:00:00.434	Reading database for genome version 'GRCh37.75' from file '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/snpEffectPredictor.bin' (this might take a while)
00:00:00.434	Database not installed
    Attempting to download and install database 'GRCh37.75'
00:00:00.435	Reading configuration file 'snpEff.config'. Genome: 'GRCh37.75'
00:00:00.653	done
00:00:00.654	Downloading database for 'GRCh37.75'
00:00:00.655	Connecting to http://downloads.sourceforge.net/project/snpeff/databases/v4_0/snpEff_v4_0_GRCh37.75.zip
00:00:01.721	Local file name: 'snpEff_v4_0_GRCh37.75.zip'
.............................................
00:01:31.595	Download finished. Total 177705174 bytes.
00:01:31.597	Extracting file 'data/GRCh37.75/motif.bin' to '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/motif.bin'
00:01:31.597	Creating local directory: '/home/pcingola/snpEff_v4_0/./data/GRCh37.75'
00:01:31.652	Extracting file 'data/GRCh37.75/nextProt.bin'
00:01:31.707	Extracting file 'data/GRCh37.75/pwms.bin'
00:01:31.707	Extracting file 'data/GRCh37.75/regulation_CD4.bin'
...
00:01:32.038	Extracting file 'data/GRCh37.75/snpEffectPredictor.bin'
00:01:32.881	Unzip: OK
00:01:32.881	Done
00:01:32.881	Database installed.
00:01:58.779	done
00:01:58.813	Reading NextProt database from file '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/nextProt.bin'
00:02:01.448	NextProt database: 523361 markers loaded.
00:02:01.448	Adding transcript info to NextProt markers.
00:02:02.180	NextProt database: 706289 markers added.
00:02:02.181	Loading Motifs and PWMs
00:02:02.181		Loading PWMs from : /home/pcingola/snpEff_v4_0/./data/GRCh37.75/pwms.bin
00:02:02.203		Loading Motifs from file '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/motif.bin'
00:02:02.973		Motif database: 284122 markers loaded.
00:02:02.973	Building interval forest
00:02:41.857	done.
00:02:41.858	Genome stats :
#-----------------------------------------------
# Genome name                : 'Homo_sapiens'
# Genome version             : 'GRCh37.75'
# Has protein coding info    : true
# Genes                      : 63677
# Protein coding genes       : 23172
#-----------------------------------------------
# Transcripts                : 215170
# Avg. transcripts per gene  : 3.38
#-----------------------------------------------
# Checked transcripts        :
#               AA sequences : 104254 ( 114.79% )
#              DNA sequences : 179360 ( 83.36% )
#-----------------------------------------------
# Protein coding transcripts : 90818
#              Length errors :  14349 ( 15.80% )
#  STOP codons in CDS errors :     39 ( 0.04% )
#         START codon errors :   8721 ( 9.60% )
#        STOP codon warnings :  21788 ( 23.99% )
#              UTR sequences :  87724 ( 40.77% )
#               Total Errors :  21336 ( 23.49% )
#-----------------------------------------------
# Cds                        : 792087
# Exons                      : 1306656
# Exons with sequence        : 1306656
# Exons without sequence     : 0
# Avg. exons per transcript  : 6.07
# WARNING!                   : Mitochondrion chromosome 'MT' does not have a mitochondrion codon table (codon table = 'Standard'). You should update the config file.
#-----------------------------------------------
# Number of chromosomes      : 297
# Chromosomes names [sizes]  :
#		'HG1292_PATCH' [250051446]
#		'HG1287_PATCH' [249964560]
#		'HG1473_PATCH' [249272860]
#		'HG1471_PATCH' [249269426]
#		'HSCHR1_1_CTG31' [249267852]
#		'HSCHR1_2_CTG31' [249266025]
#		'HSCHR1_3_CTG31' [249262108]
#		'HG999_2_PATCH' [249259300]
#		'HG989_PATCH' [249257867]
#		'HG999_1_PATCH' [249257505]
#		'HG1472_PATCH' [249251918]
#		'1' [249250621]
#		'HG1293_PATCH' [249140837]
#		'HG686_PATCH' [243297375]
#		'HSCHR2_1_CTG12' [243216362]
#		'HSCHR2_2_CTG12' [243205453]
#		'HSCHR2_1_CTG1' [243205406]
#		'HG953_PATCH' [243199374]
#		'2' [243199373]
.....
.....
#-----------------------------------------------

00:02:59.416	Predicting variants

WARNINGS: Some warning were detected
Warning type	Number of warnings
WARNING_TRANSCRIPT_INCOMPLETE	8215
WARNING_TRANSCRIPT_NO_START_CODON	3483


00:03:04.327	Creating summary file: snpEff_summary.html
00:03:04.891	Creating genes file: snpEff_genes.txt
00:03:17.334	done.
00:03:17.336	Logging
00:03:18.337	Checking for updates...
```

Notice how SnpEff automatically downloads and installs the database.
Next time SnpEff will use the local version, so the installation step is only done once.

The annotated variants will be in the new file "test.chr22.ann.vcf".

!!! warning
    SnpEff creates a file called "snpEff_summary.html" showing basic statistics about the analyzed variants.
    Take a quick look at it.

!!! info
    We used the java parameter -Xmx8g to increase the memory available to the Java Virtual Machine to 4G.
    SnpEff's human genome database is large and it has to be loaded into memory.
    If your computer doesn't have at least 4G of memory, you probably won't be able to run this example.

!!! info
    If you are running SnpEff from a directory different than the one it was installed, you will have to specify where the config file is.
    This is done using the '-c' command line option:

```
java -Xmx8g -jar snpEff.jar -c path/to/snpEff/snpEff.config -v GRCh37.75 test.chr22.vcf > test.chr22.ann.vcf
```

### Detailed examples

Take a look at several detailed examples in our [examples page](../examples.md).

### Specify a configuration file

Sometimes you need to specify the path to the config file.
For instance, when you run SnpEff from a different directory than your install directory, you have to
specify where the config file is located using the '-c' command line option.

```
java -Xmx8g path/to/snpEff/snpEff.jar -c path/to/snpEff/snpEff.config GRCh37.75 path/to/snps.vcf
```

!!! info
    Since version 4.1B, you can use the `-configOption` command line option to override any value in the config file

### Java memory options

By default the amount of memory set by a java process is set too low.
If you don't assign more memory to the process, you will most likely have an "OutOfMemory" error.

You should set the amount of memory in your java virtual machine to, at least, 2 Gb.
This can be easily done using the Java command line option `-Xmx`.
E.g. In this example I use 4Gb:

```
# Run using 4 Gb of memory
java -Xmx8g snpEff.jar hg19 path/to/your/files/snps.vcf
```

Note: There is no space between `-Xmx` and `4G`.

### Running SnpEff in the Cloud

You can run SnpEff in a "the Cloud" exactly the same way as running it on your local computer.
You should not have any problems at all.

Here is an example of installing it and running it on an Amazon EC2 instance (virtual machine):

```
$ ssh -i ./aws_amazon/my_secret_key.pem ec2-user@ec2-54-234-14-244.compute-1.amazonaws.com


       __|  __|_  )
       _|  (     /   Amazon Linux AMI
      ___|\___|___|


[ec2-user@ip-10-2-202-163 ~]$ wget https://snpeff.blob.core.windows.net/versions/snpEff_latest_core.zip
[ec2-user@ip-10-2-202-163 ~]$ unzip snpEff_latest_core.zip
[ec2-user@ip-10-2-202-163 ~]$ cd snpEff/
[ec2-user@ip-10-2-202-163 snpEff]$ java -jar snpEff.jar download -v hg19
00:00:00.000    Downloading database for 'hg19'
...
00:00:36.340    Done
[ec2-user@ip-10-2-202-163 snpEff]$ java -Xmx8g -jar snpEff.jar dump -v hg19 > /dev/null
00:00:00.000    Reading database for genome 'hg19' (this might take a while)
00:00:20.688    done
00:00:20.688    Building interval forest
00:00:33.110    Done.
```
As you can see, it's very simple.

### Loading the database

One of the first things SnpEff has to do is to load the database.
Usually it takes from a few seconds to a couple of minutes, depending on database size.
Complex databases, like human, require more time to load.
After the database is loaded, SnpEff can analyze thousands of variants per second.

### Command line vs Web interface

In order to run SnpEff you need to be comfortable running command from a command line terminal.
If you are not, then it is probably a good idea to ask you systems administrator to install a [Galaxy](http://usegalaxy.org) server and use the web interface.
You can also use the open Galaxy server, but functionality may be limited and SnpEff versions may not be updated frequently.
