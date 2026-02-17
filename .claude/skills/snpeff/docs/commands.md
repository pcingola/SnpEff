# Commands and utilities

SnpEff provides several commands and utilities for genomic data analysis.

The main commands `ann/eff` (variant annotation) and `build` (database building) are described in the [Commands & command line options](commandline.md) and [Building databases](build_db.md) pages respectively.
Here we describe all the other commands and some scripts provided, that are useful for genomic data analysis.

### SnpEff buildNextProt

Build a NextProt database from NextProt XML files. NextProt provides proteomic annotations for human proteins that can be used to annotate variants with functional information (e.g. active sites, binding sites, post-translational modifications).

```
Usage: snpEff buildNextProt genome_version nextProt_XML_dir
```

The `nextProt_XML_dir` argument should point to a directory containing NextProt XML files. The resulting database is stored as `nextProt.bin` in the genome's data directory and is automatically loaded when annotating variants with the `-nextProt` flag.

### SnpEff closest

Annotates using the closest genomic region (e.g. exon, transcript ID, gene name) and distance in bases.

```
Usage: snpEff closest [options] genome_version file.vcf

Options:
    -bed     : Input format is BED. Default: VCF
    -tss     : Measure distance from TSS (transcription start site)
```

Example:
```
$ java -Xmx8g -jar snpEff.jar closest GRCh37.66 test.vcf
##INFO=<ID=CLOSEST,Number=4,Type=String,Description="Closest exon: Distance (bases), exons Id, transcript Id, gene name">
1       12078   .       G       A       25.69   PASS    AC=2;AF=0.048;CLOSEST=0,exon_1_11869_12227,ENST00000456328,DDX11L1
1       16097   .       T       G       42.42   PASS    AC=9;AF=0.0113;CLOSEST=150,exon_1_15796_15947,ENST00000423562,WASH7P
1       40261   .       C       A       366.26  PASS    AC=30;AF=0.484;CLOSEST=4180,exon_1_35721_36081,ENST00000417324,FAM138A
1       63880   .       C       T       82.13   PASS    AC=10;AF=0.0400;CLOSEST=0,exon_1_62948_63887,ENST00000492842,OR4G11P
```

For instance, in the third line (1:16097 T G), it added the tag `CLOSEST=150,exon_1_15796_15947,ENST00000423562,WASH7P`
 which means that the variant is 150 bases away from exon "exon_1_15796_15947".
The exon belongs to transcript "ENST00000423562" of gene "WASH7P".

!!! info
    If multiple markers are available (at the same distance) the one belonging to the longest mRNA transcript is shown.

The input can also be a BED file, the output file has the same information as CLOSEST info field, added to the fourth column of the output BED file:
```
$ snpeff closest -bed GRCh37.66 test.bed
1	12077	12078	line_1;0,exon_1_11869_12227,ENST00000456328,DDX11L1
1	16096	16097	line_2;150,exon_1_15796_15947,ENST00000423562,WASH7P
1	40260	40261	line_3;4180,exon_1_35721_36081,ENST00000417324,FAM138A
1	63879	63880	line_4;0,exon_1_62948_63887,ENST00000492842,OR4G11P
```

### SnpEff count

As the name suggests, `snpEff count` command counts how many reads and bases from a BAM file hit a gene, transcript, exon, intron, etc.
Input files can be in BAM, SAM, VCF, BED or BigBed formats.

A summary HTML file with charts is generated. Here are some examples:

![snpeff_count_01](../images/snpeff_count_01.png){: .smallerimg .center}

![snpeff_count_02](../images/snpeff_count_02.png){: .smallerimg .center}

If you need to count how many reads (and bases) from a BAM file hit each genomic region, you can use 'count' utility.

The command line is quite simple. E.g. in order to count how many reads (from N BAM files) hit regions of the human genome, you simply run:

    java -Xmx8g -jar snpEff.jar count GRCh37.68 readsFile_1.bam readsFile_2.bam ...  readsFile_N.bam > countReads.txt

```
Options:
    -n <name>    : Output file base name.
    -p           : Calculate probability model (binomial).
    -i <file>    : Add intervals from a BED file. Can be used multiple times.
```

The output is a TXT (tab-separated) file, that looks like this:
```
chr  start  end       type                IDs                         Reads:readsFile_1.bam  Bases:readsFile_1.bam  Reads:readsFile_2.bam  Bases:readsFile_2.bam ...
1    1      11873     Intergenic          DDX11L1                     130                    6631                   50                     2544
1    1      249250621 Chromosome          1                           2527754                251120400              2969569                328173439
1    6874   11873     Upstream            NR_046018;DDX11L1           130                    6631                   50                     2544
1    9362   14361     Downstream          NR_024540;WASH7P            243                    13702                  182                    9279
1    11874  12227     Exon                exon_1;NR_046018;DDX11L1    4                      116                    2                      102
1    11874  14408     Gene                DDX11L1                     114                    7121                   135                    6792
1    11874  14408     Transcript          NR_046018;DDX11L1           114                    7121                   135                    6792
1    12228  12229     SpliceSiteDonor     exon_1;NR_046018;DDX11L1    3                      6                      0                      0
1    12228  12612     Intron              intron_1;NR_046018;DDX11L1  13                     649                    0                      0
1    12611  12612     SpliceSiteAcceptor  exon_2;NR_046018;DDX11L1    0                      0                      0                      0
1    12613  12721     Exon                exon_2;NR_046018;DDX11L1    3                      24                     1                      51
1    12722  12723     SpliceSiteDonor     exon_2;NR_046018;DDX11L1    3                      6                      0                      0
1    12722  13220     Intron              intron_2;NR_046018;DDX11L1  22                     2110                   20                     987
1    13219  13220     SpliceSiteAcceptor  exon_3;NR_046018;DDX11L1    5                      10                     1                      2
1    13221  14408     Exon                exon_3;NR_046018;DDX11L1    82                     4222                   113                    5652
1    14362  14829     Exon                exon_11;NR_024540;WASH7P    37                     1830                   7                      357
1    14362  29370     Transcript          NR_024540;WASH7P            704                    37262                  524                    34377
1    14362  29370     Gene                WASH7P                      704                    37262                  524                    34377
1    14409  19408     Downstream          NR_046018;DDX11L1           122                    7633                   39                     4254
```
The columns are:

* Column 1: Chromosome name
* Column 2: Genomic region start
* Column 3: Genomic region end
* Column 4: Genomic region type (e.g. Exon, Gene, SpliceSiteDonor, etc.)
* Column 5: ID (e.g. exon ID ; transcript ID; gene ID)
* Column 6: Count of reads (in file readsFile_1.bam) intersecting genomic region.
* Column 7: Count of bases (in file readsFile_1.bam) intersecting genomic region, i.e. each read is intersected and the resulting number of bases added.
* Column ...:  (repeat count reads and bases for each BAM file provided)

**Totals and Binomial model**

Using command line option `-p`, you can calculate p-values based on a Binomial model.
For example (output edited for the sake of brevity):
```
$ java -Xmx8g -jar snpEff.jar count -v BDGP5.69 fly.bam > countReads.txt
00:00:00.000	Reading configuration file 'snpEff.config'
...
00:00:12.148	Calculating probability model for read length 50
...
type               p.binomial             reads.fly  expected.fly  pvalue.fly
Chromosome         1.0                    205215     205215        1.0
Downstream         0.29531659795589793    59082      60603         1.0
Exon               0.2030262729897713     53461      41664         0.0
Gene               0.49282883664487515    110475     101136        0.0
Intergenic         0.33995644860241336    54081      69764         0.9999999963234701
Intron             0.3431415343615103     72308      70418         9.06236369003514E-19
RareAminoAcid      9.245222303207472E-7   3          0             9.879186871519377E-4
SpliceSiteAcceptor 0.014623209601955131   3142       3001          0.005099810118785825
SpliceSiteDonor    0.015279075154423956   2998       3135          0.9937690786738507
Transcript         0.49282883664487515    110475     101136        0.0
Upstream           0.31499087549896493    64181      64641         0.9856950416729887
Utr3prime          0.03495370828296416    8850       7173          1.1734134297889064E-84
Utr5prime          0.02765432673262785    8146       5675          7.908406840800345E-215
```

The columns in for this table are (in the previous example the input file was 'fly.bam' so fileName is 'fly'):

* type : Type of interval
* p.binomial : Probability that a random read hits this 'type' of interval (in binomial model)
* reads.fileName : Total number of reads in 'fileName' (user provided BAM/SAM file)
* expected.fileName : Expected number of reads hitting this 'type' of interval (for user provided BAM/SAM file)
* pvalue.fileName : p-value that 'reads.fileName' reads or more hit this 'type' of interval (for user provided BAM/SAM file)
* Column ...:  (repeat last three column for each BAM/SAM file provided by the user)

**User defined intervals**

You can add user defined intervals using `-i file.bed` command line option.
The option can be used multiple times, thus allowing multiple BED files to be added.

Example : You want to know how many reads intersect each peak from a peak detection algorithm:

    java -Xmx8g -jar snpEff.jar count -i peaks.bed GRCh37.68 reads.bam

### SnpEff databases

This command provides a list of configured databases, i.e. available in `snpEff.config` file.

```
Usage: snpEff databases [galaxy|html]
```

The output format can be selected: plain text (default), `galaxy` (Galaxy menu format), or `html` (HTML table format).

Example:
```
$ java -jar snpEff.jar databases
Genome                                                      Organism                                                    Status    Bundle                        Database download link
------                                                      --------                                                    ------    ------                        ----------------------
129S1_SvImJ_v1.99                                           Mus_musculus_129s1svimj                                                                             https://snpeff-public.s3.amazonaws.com/databases/v5_0/snpEff_v5_0_129S1_SvImJ_v1.99.zip
AIIM_Pcri_1.0.99                                            Pavo_cristatus                                                                                      https://snpeff-public.s3.amazonaws.com/databases/v5_0/snpEff_v5_0_AIIM_Pcri_1.0.99.zip
AKR_J_v1.99                                                 Mus_musculus_akrj                                                                                   https://snpeff-public.s3.amazonaws.com/databases/v5_0/snpEff_v5_0_AKR_J_v1.99.zip
AP006557.1                                                  SARS coronavirus TWH genomic RNA, complete genome.                                                                  https://snpeff-public.s3.amazonaws.com/databases/v5_0/snpEff_v5_0_AP006557.1.zip
AP006558.1                                                  SARS coronavirus TWJ genomic RNA, complete genome.                                                                  https://snpeff-public.s3.amazonaws.com/databases/v5_0/snpEff_v5_0_AP006558.1.zip
AP006559.1                                                  SARS coronavirus TWK genomic RNA, complete genome.                                                                  https://snpeff-public.s3.amazonaws.com/databases/v5_0/snpEff_v5_0_AP006559.1.zip
AP006560.1                                                  SARS coronavirus TWS genomic RNA, complete genome.                                                                  https://snpeff-public.s3.amazonaws.com/databases/v5_0/snpEff_v5_0_AP006560.1.zip
AP006561.1                                                  SARS coronavirus TWY genomic RNA, complete genome.                                                                  https://snpeff-public.s3.amazonaws.com/databases/v5_0/snpEff_v5_0_AP006561.1.zip
...
```

### SnpEff download

This command downloads and installs a database.

```
Usage: snpEff download [options] {snpeff | genome_version}
```

You can use `snpEff download snpeff` to download/update SnpEff itself, or specify a genome version to download a pre-built database.

!!! warning
    Note that the database must be configured in `snpEff.config` and available at the download site.

Example: Download and install C.Elegans genome:
```
$ java -jar snpEff.jar download -v WBcel215.69
```

### SnpEff dump

Dump the contents of a database to a text file, a BED file or a tab separated TXT file (that can be loaded into R).

```
Usage: snpEff dump [options] genome_version

Options:
    -bed             : Dump in BED format
    -chr <string>    : Prepend 'string' to chromosome name
    -txt             : Dump as a TXT table
    -0               : Output zero-based coordinates
    -1               : Output one-based coordinates
```

**BED file example**:
```
$ java -jar snpEff.jar download -v GRCh37.70
$ java -Xmx8g -jar snpEff.jar dump -v -bed GRCh37.70 > GRCh37.70.bed
00:00:00.000	Reading database for genome 'GRCh37.70' (this might take a while)
00:00:32.476	done
00:00:32.477	Building interval forest
00:00:45.928	Done.
```

The output file looks like a typical BED file (chr \t start \t end \t name).

!!! warning
    Keep in mind that BED file coordinates are zero based, semi-open intervals.
    So a 2 base interval at (one-based) positions 100 and 101 is expressed as a BED interval `[99 - 101]`.

```
$ head GRCh37.70.bed
1	0	249250621	Chromosome_1
1	111833483	111863188	Gene_ENSG00000134216
1	111853089	111863002	Transcript_ENST00000489524
1	111861741	111861861	Cds_CDS_1_111861742_111861861
1	111861948	111862090	Cds_CDS_1_111861949_111862090
1	111860607	111860731	Cds_CDS_1_111860608_111860731
1	111861114	111861300	Cds_CDS_1_111861115_111861300
1	111860305	111860427	Cds_CDS_1_111860306_111860427
1	111862834	111863002	Cds_CDS_1_111862835_111863002
1	111853089	111853114	Utr5prime_exon_1_111853090_111853114
```

**TXT file example**:
```
$ java -Xmx8g -jar snpEff.jar dump -v -txt GRCh37.70 > GRCh37.70.txt
00:00:00.000	Reading database for genome 'GRCh37.70' (this might take a while)
00:00:31.961	done
00:00:31.962	Building interval forest
00:00:45.467	Done.
```
The output file is a tab-separated table with gene, transcript, and exon information:
```
$ head GRCh37.70.txt
chr start       end        strand  type         id                          geneName  geneId            numberOfTranscripts  canonicalTranscriptLength  transcriptId     cdsLength  numerOfExons  exonRank  exonSpliceType
1   1           249250622  +1      Chromosome   1                                                                                                       
1   111833484   111863189  +1      Gene         ENSG00000134216             CHIA      ENSG00000134216   10                   1431                                                                  
1   111853090   111863003  +1      Transcript   ENST00000489524             CHIA      ENSG00000134216   10                   1431                       ENST00000489524  862        9                     
1   111861742   111861862  +1      Cds          CDS_1_111861742_111861861   CHIA      ENSG00000134216   10                   1431                       ENST00000489524  862        9                     
1   111861949   111862091  +1      Cds          CDS_1_111861949_111862090   CHIA      ENSG00000134216   10                   1431                       ENST00000489524  862        9                     
1   111853090   111853115  +1      Utr5prime    exon_1_111853090_111853114  CHIA      ENSG00000134216   10                   1431                       ENST00000489524  862        9             1         ALTTENATIVE_3SS
1   111854311   111854341  +1      Utr5prime    exon_1_111854311_111854340  CHIA      ENSG00000134216   10                   1431                       ENST00000489524  862        9             2         SKIPPED
1   111860608   111860732  +1      Exon         exon_1_111860608_111860731  CHIA      ENSG00000134216   10                   1431                       ENST00000489524  862        9             5         RETAINED
1   111853090   111853115  +1      Exon         exon_1_111853090_111853114  CHIA      ENSG00000134216   10                   1431                       ENST00000489524  862        9             1         ALTTENATIVE_3SS
1   111861742   111861862  +1      Exon         exon_1_111861742_111861861  CHIA      ENSG00000134216   10                   1431                       ENST00000489524  862        9             7         RETAINED
```

The format is:

Column                    | Meaning
------------------------- | -----------
chr                       | Chromosome name
start                     | Marker start (one-based coordinate)
end                       | Marker end (one-based coordinate)
strand                    | Strand (positive or negative)
type                      | Type of marker (e.g. exon, transcript, etc.)
id                        | ID. E.g. if it's a Gene, then it may be ENSEMBL's gene ID
geneName                  | Gene name, if marker is within a gene (exon, transcript, UTR, etc.), empty otherwise (e.g. intergenic)
geneId                    | Gene ID, if marker is within a gene
numberOfTranscripts       | Number of transcripts in the gene
canonicalTranscriptLength | CDS length of canonical transcript
transcriptId              | Transcript ID, if marker is within a transcript
cdsLength                 | CDS length of the transcript
numerOfExons              | Number of exons in this transcript
exonRank                  | Exon rank, if marker is an exon
exonSpliceType            | Exon splice type, if marker is an exon


### SnpEff genes2bed

Dumps a selected set of genes (or all genes) as BED intervals. By default it outputs gene-level coordinates, but can also output exons, CDS regions, introns, or transcripts.

```
Usage: snpEff genes2bed genomeVer [-f genes.txt | geneList]

Options:
    -cds           : Show coding exons (no UTRs).
    -e             : Show exons for every transcript.
    -f <file.txt>  : A TXT file having one gene ID (or name) per line.
    -i             : Show introns for every transcript.
    -pc            : Use only protein coding genes.
    -tr            : Show transcript coordinates.
    -ud <num>      : Expand gene interval upstream and downstream by 'num' bases.
    geneList       : A list of gene IDs or names as command line arguments.
```

!!! info
    Options `-cds`, `-e`, and `-tr` are mutually exclusive. If no gene list is provided (neither via `-f` nor as arguments), all genes in the genome are used.

Example:
```
$ java -Xmx8g -jar snpEff.jar genes2bed GRCh37.66 DDX11L1 WASH7P
#chr	start	end	geneName;geneId;strand
1	11868	14411	DDX11L1;ENSG00000223972;+
1	14362	29805	WASH7P;ENSG00000227232;-
```

Example showing exons:
```
$ java -Xmx8g -jar snpEff.jar genes2bed -e GRCh37.66 DDX11L1
#chr	start	end	geneName;geneId;transcriptId;exonRank;strand
```

### SnpEff cds

Performs a database sanity check by calculating CDS sequences from a SnpEff database and comparing them to a FASTA file containing the "correct" sequences.

```
Usage: snpEff cds [options] genome_version cds_file
```

This command is invoked automatically when building databases (`snpEff build`), so there is usually no need to invoke it manually.

### SnpEff protein

Performs a database sanity check by calculating protein sequences from a SnpEff database and comparing them to a FASTA file containing the "correct" sequences.

```
Usage: snpEff protein [options] genome_version protein_file

Options:
    -codonTables   : Try all codon tables on each chromosome and calculate error rates.
```

This command is invoked automatically when building databases (`snpEff build`), so there is usually no need to invoke it manually.
The `-codonTables` option is useful for debugging genomes that may use non-standard codon tables.

### SnpEff len

Calculates the genomic length of every type of marker (Gene, Exon, Utr, etc.).
Length is calculated by overlapping all markers and counting the number of bases (e.g. a base is counted as 'Exon' if any exon falls onto that base).
This command also reports the probability of a Binomial model.

```
Usage: snpEff len [options] genome_version

Options:
    -r <num>       : Assume a read size of 'num' bases.
    -iter <num>    : Perform 'num' iterations of random sampling.
    -reads <num>   : Each random sampling iteration has 'num' reads.
```

!!! info
    Parameter `-r num` adjusts the model for a read length of 'num' bases. That is, if two markers of the same type are closer than 'num' bases, it joins them by including the bases separating them.

E.g.:
```
$ java -Xmx1g -jar snpEff.jar len -r 100 BDGP5.69
marker                   size    count     raw_size raw_count    binomial_p
Cds                  22767006    56955     45406378    122117    0.13492635563570918
Chromosome          168736537       15    168736537        15    1.0
Downstream           49570138     5373    254095562     50830    0.29377240330587084
Exon                 31275946    61419     63230008    138474    0.18535372691689175
Gene                 82599213    11659     87017182     15222    0.4895158717166277
Intergenic           56792611    11637     56792611     11650    0.3365756581812509
Intron               55813748    42701    168836797    113059    0.33077452573297744
SpliceSiteAcceptor      97977    48983       226118    113059    5.806507691929223E-4
SpliceSiteDonor        101996    50981       226118    113059    6.044689657225808E-4
Transcript           82599213    11659    232066805     25415    0.4895158717166277
Upstream             52874082     5658    254044876     50830    0.3133528928592389
Utr3prime             5264120    13087     10828991     24324    0.031197274126824114
Utr5prime             3729197    19324      6368070     33755    0.02210070839607192
```
Column meaning:

* marker : Type of marker interval
* size : Size of all intervals in the genome, after overlap and join.
* count : Number of intervals in the genome, after overlap and join.
* raw_size : Size of all intervals in the genome. Note that this could be larger than the genome.
* raw_count : Number of intervals in the genome.

### SnpEff pdb

Build interaction database based on PDB (Protein Data Bank) or AlphaFold structure data.
This command analyzes protein structures to identify amino acid pairs that are in close physical proximity and adds this interaction information to the SnpEff database for variant annotation.

```
Usage: snpEff pdb [options] genome_version

Options:
    -aaSep <number>          : Minimum number of AA separation within sequence.
    -idMap <file>            : ID map file (PDB ID to transcript ID mapping).
    -maxDist <number>        : Maximum distance in Angstrom for atom pairs.
    -maxErr <number>         : Maximum AA sequence difference between PDB and genome.
    -org <name>              : Organism common name.
    -orgScientific <name>    : Organism scientific name.
    -pdbDir <path>           : Path to PDB files.
    -res <number>            : Maximum PDB file resolution.
```

For detailed instructions on obtaining PDB/AlphaFold data and building interaction databases, see [Building databases: PDB and AlphaFold](build_pdb.md).

### SnpEff seq

Translates DNA sequences to protein using the genome's codon table.
This is a simple utility for quick sequence translations from the command line.

```
Usage: snpEff seq [-r] genome seq_1 seq_2 ... seq_N

Options:
    -r    : Reverse Watson-Crick complement before translating.
```

The command shows the protein translation in three formats: 3-letter amino acid code, 1-letter code with spacing, and 1-letter code.

Example:
```
$ java -jar snpEff.jar seq GRCh38.105 ATGCGAGCT
Sequence                   : ATGCGAGCT
Protein (3-Letter)         : Met-Arg-Ala
Protein (1-Letter-space)   :  M  R  A
Protein (1-Letter)         : MRA
```

### SnpEff show

Show a text representation of genes or transcripts including coordinates, DNA sequence and protein sequence.
Useful for visual inspection and debugging of gene annotations.

```
Usage: snpEff show genome_version gene_1 ... gene_N ... trId_1 ... trId_N
```

The command accepts both gene IDs and transcript IDs. It displays an ASCII-art representation of the transcript structure including exons, introns, coding regions, UTRs, and the corresponding DNA and protein sequences. Coordinates are zero-based.

### SnpEff translocReport

Create a translocation report with SVG visualizations from a VCF file containing structural variants (BND, DUP, DEL).
The report shows gene fusions and translocations with transcript-level detail.

```
Usage: snpEff translocReport [options] genome_version input.vcf

Options:
    -onlyOneTr         : Report only one transcript pair per translocation (used for debugging).
    -outPath <dir>     : Create individual output SVG files for each translocation in 'dir'.
    -report <file>     : Output report file name. Default: translocations_report.html
```

The command generates an HTML report (by default `translocations_report.html`) containing SVG visualizations of each translocation. If `-outPath` is specified, individual SVG files are also saved to that directory.

### Scripts

!!! warning
    The Perl utility scripts previously distributed with SnpEff (sam2fastq.pl, fasta2tab.pl, fastaSplit.pl, vcfEffOnePerLine.pl, etc.) have been deprecated and are no longer included in the main `scripts/` directory.
