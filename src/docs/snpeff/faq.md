# SnpEff: Frequently Asked Questions

## Error and Warning messages

SnpEff defines several messages in roughly 3 categories:

- INFO: An informative message
- WARNING: A problem in the reference genome definition that **MAY** result in an incorrect variant annotation
- ERROR: A problem in the reference genome definition that **WILL ALMOST CERTAINLY** result in an incorrect variant annotation

**INFO_REALIGN_3_PRIME**

The variant has been realigned to the most 3-prime position within the transcript.

This is usually done to comply with HGVS specification to always report the most 3-prime annotation. While VCF requires to realign to the left-most of the reference genome, HGSV requires to realign to the most 3-prime. These two specifications are contradicting in some cases, so in order to comply with HGSV, sometimes a local realignment is required.

IMPORTANT: This message is just indicating that a realignment was performed, so ** when this INFO message is present, the original coordinates from the VCF file are not exactly the same as the coordinates used to calculate the variant annotation **

**WARNING_SEQUENCE_NOT_AVAILABLE**

The exon does not have reference sequence information.
The annotation may not be calculated (e.g. incomplete transcripts).

**WARNING_REF_DOES_NOT_MATCH_GENOME**

The genome reference does not match the variant's reference.

For example, if the VCF file indicates that the reference at a certain location is 'A', while SnpEff's database indicates that the reference should be 'C', this WARNING would be added.

Under normal circumstances, there should be none of these warnings (or at most a handful).

IMPORTANT: If too many of these warnings are seen, this indicates a severe problem (version mismatch between your VCF files and the reference genome). A typical case when too many of these warning are seen is when trying to annotate using a different genome than the one used for alignment (e.g. reads are aligned to hg19 but variants are annotated to using hg38)

**WARNING_TRANSCRIPT_INCOMPLETE**

The number of coding bases is NOT multiple of 3, so there is missing information for at least one codon.
This indicates an error in the reference genome gene and/or transcript definition.
This could happen in genomes that are not well understood.

**WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS**

Multiple STOP codons found in a CDS.
There should be only one STOP codon at the end of the transcript, but in this case, the transcript has multiple STOP codons, which is unlikely to be real.

This usually indicates an error on the reference genome (or database).
Could for, for example, indicating frame errors in the reference genome for one or more exons in this transcript.

**WARNING_TRANSCRIPT_NO_START_CODON**

Start codon does not match any 'start' codon in the CodonTable.

This usually indicates an error on the reference genome (or database) but could be also due to a misconfigured codon table for the genome. You should check that the codon table is properly set in `snpEff.config`

**WARNING_TRANSCRIPT_NO_STOP_CODON**

Stop codon does not match any 'stop' codon in the CodonTable.

This usually indicates an error on the reference genome (or database) but could be also due to a misconfigured codon table for the genome. You should check that the codon table is properly set in `snpEff.config`

**ERROR_CHROMOSOME_NOT_FOUND**

Chromosome name not found. Typically due to mismatch in chromosome naming conventions between variants file and database, but can be a more several problems (different reference genome).

See more details (here)[https://github.com/pcingola/SnpEff/wiki/ERROR_CHROMOSOME_NOT_FOUND]

**ERROR_OUT_OF_CHROMOSOME_RANGE**

Variant's genomic position is outside chromosome's range.

Simple, the variant coordinate is outside the reference genome chromosome's length.

IMPORTANT: If too many of these warnings are seen, this indicates a severe problem (version mismatch between your VCF files and the reference genome). A typical case when too many of these warning are seen is when trying to annotate using a different genome than the one used for alignment (e.g. reads are aligned to hg19 but variants are annotated to using hg38)

**ERROR_OUT_OF_EXON**

An exonic variant is falling outside the exon.

**ERROR_MISSING_CDS_SEQUENCE**

Missing coding sequence information.
In this case, the full variant annotation cannot be calculated due to missing CDS information.

This usually indicates an error on the reference genome (or database).



## ERROR_CHROMOSOME_NOT_FOUND: Details

The error is due to a difference between the chromosome names in input VCF file and the chromosome names in SnpEff's database.

Chromosome does not exist in the reference database. Typically this means that there is a mismatch between the chromosome names in your input file and the chromosome names used in the reference genome to build SnpEff's database.

!!! warning
	This error could be caused because you are trying to annotate using a reference genome that is different than the one you used for sequence alignment. Obviously doing this makes no sense and the annotation information you'll get will be garbage. That's why SnpEff shows you an error message.

**Solution**

Sometimes SnpEff database matches the reference genome for your organism, and it's just that the chromosome names are changed. In this case, you can fix the error by changing the chromosome names in your input file.

!!! info
	You can see the chromosome names used by SnpEff's database by using `-v` (verbose) option. SnpEff will show a line like this one:

```
$ java -Xmx4g -jar snpEff.jar -v genomeName my.vcf > my.ann.vcf
...
...
# Chromosomes names [sizes]  : '1' [249250621] '2' [243199373]
...
...
```

!!! info
	You can see the chromosome names in your input VCF file using a command like this one

```
cat input.vcf | grep -v "^#" | cut -f 1 | uniq
```

Once you know the names of the input file and the name used by SnpEff's database, you can adjust the chromosome name using a simple sed command. For example, if you input file's chromosome name is `INPUT_CHR_NAME` and the name in SnpEff's database is `SNPEFF_CHR_NAME`, you could use the following command:

```
cat input.vcf | sed "s/^INPUT_CHR_NAME/SNPEFF_CHR_NAME/" > input_updated_chr.vcf
```



## How to building an NCBI genome (GenBank file)

When building a database with SnpEff if your genomic reference is in NCBI, there is a script that might help you build the database.

The script is `buildDbNcbi.sh` and is located in snpEff's scripts directory.
It takes only one argument, which is the NCBI's ID.

**Example: Salmonella enterica**

In this example, we build the database for _"Salmonella enterica subsp. enterica serovar Typhi str. P-stx-12"_ having accession ID CP003278.1

```
$ cd ~/snpEff

# Note: Output edited for brevity
$ ./scripts/buildDbNcbi.sh CP003278.1
Downloading genome CP003278.1
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 10.2M    0 10.2M    0     0  3627k      0 --:--:--  0:00:02 --:--:-- 3627k
00:00:00	SnpEff version SnpEff 4.3p (build 2017-07-28 14:02), by Pablo Cingolani
00:00:00	Command: 'build'
00:00:00	Building database for 'CP003278.1'
00:00:00	Reading configuration file 'snpEff.config'. Genome: 'CP003278.1'
00:00:00	Reading config file: /home/pcingola/workspace/SnpEff/snpEff.config
00:00:00	done
Chromosome: 'CP003278'	length: 4768352

	Create exons from CDS (if needed): ..................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................
	Exons created for 4690 transcripts.

...
00:00:01	Reading proteins from file '/home/pcingola/workspace/SnpEff/./data/CP003278.1/genes.gbk'...
00:00:01	done (4690 Proteins).
00:00:01	Comparing Proteins...
	Labels:
		'+' : OK
		'.' : Missing
		'*' : Error
	+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
...
	+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	Protein check:	CP003278.1	OK: 4690	Not found: 0	Errors: 0	Error percentage: 0.0%
00:00:02	Saving database
...
00:00:04	Done.
```


## Creating a protein sequence FASTA file

SnpEff `ann` command has a command line option called `-fastaProt` that tells SnpEff to output the "original" and "resulting" protein sequences for each variant into a FASTA file.

This means that for each variant, the output FASTA file will have an entry with protein sequence resulting from applying that variant to the reference sequence.

Here is an example:
```
$ cat z.vcf
17  39684602    .   C   T   .   .   .

$ java -Xmx6g -jar snpEff.jar ann -fastaProt z.prot.fa GRCh38.mane.1.2.ensembl z.vcf > z.ann.vcf
```

The resulting fasta file `z.prot.fa` looks like this (lines edited for readibility):

Notice the difference in the protemic sequeces ('VAF**A**WVS' -> 'VAF**T**WVS')
```
>ENST00000300658.9, gene: PGAP3, protein_id: ENSP00000300658.4, reference
MAGL....VAFAWVS...
>ENST00000300658.9, gene: PGAP3, protein_id: ENSP00000300658.4, variant:q 17:39684602-39684602, ref:'C', alt:'T', HGVS.p: p.Ala143Thr
MAGL....VAFTWVS...
```

When given the command line option `-fastaProtNoRef`, no reference sequence is written to the output FASTA protein sequences file.


## Genome reference

Having a standard reference sequence is the key to establish comparisons and analysis.
In order to compare DNA from different individuals (or samples), we need a *reference genome sequence* and *genomic annotations*.

Alignment and annotations must be based on the exact same reference genome sequence.
Variants are called based on the reference genome, thus variant annotations must be performed using same reference genome.
For instance, performing variant calling respect to hg19 and then performing variant annotations using hg38 genome, would result in completely erroneous results.

Oftentimes lack of consistency between SnpEff annotations and genome coordinated from other data sources (e.g. a genome browser or other online databases) are due to the fact that there is a difference in genome reference versions.
For example, maybe the VCF file was annotated using SnpEff's `GRCh38.99` database, but you are looking at an `hg38` genome browser (both reference are human, version 38, but different transcript versions).


## Genome reference data sources

SnpEff genome databases are built from genomic data sources, such as Ensembl, RefSeq, NCBI, UCSC, etc.

To find which data source was used, sometimes the information is provided in the `snpEff.config` file, under the `genome_name.reference` entry.

**Example 1: GRCh37.75**

If you are looking for the `GRCh37.75` genome, you can search for the entry in `snpEff.conf` file:

```
$ grep -A 1 GRCh37.75 snpEff.config
GRCh37.75.genome : Homo_sapiens
GRCh37.75.reference : ftp://ftp.ensembl.org/pub/release-75/gtf/
```
 As you can see, the genome data is from Ensembl, release 75 (as expected).

**Example 2: hg19**

If you are looking for the `hg19` genome, you can also search for the entry in `snpEff.conf` file:

```
$ grep -i hg19.genome snpEff.config
hg19.genome : Homo_sapiens (USCS)
...
```

In this case, there is no `hg19.reference` entry, but the genome name clearly states that the database was retrieved from UCSC (having RefSeq).
Which exact sub-version is this hg19?
Well, unfortunately, UCSC does not keep track of sub-versions.
A rule of the thumb is that the database is retrieved before it is built, so you can look at the date/time from the snpEff database:

```
$ ls -al data/hg19/snpEffectPredictor.bin
-rw-r--r-- 1 pcingola pcingola 52630202 Mar 19 08:27 data/hg19/snpEffectPredictor.bin
```

So this`hg19` database was retrieved from UCSC around on March 19th.

**Example 3: Salmonella_enterica**

Sometimes the information is in the genome's `reference` entry is not enough to determine which exact version was used, but the `snpEff.config` file provides some additional information in the comments
For example, let's say we'd like to find the data source for `Salmonella_enterica` genome

If we edit the `snpEff.config` and find the entry for Salmonella_enterica, we see something like this:
```
Salmonella_enterica.genome : Salmonella_enterica
Salmonella_enterica.reference : ftp.ensemblgenomes.org
```

OK, it is from Ensembl, but which version?
If you scroll up in the config file, you'll see a comment like this:

```
#---
# ENSEMBL BFMPP release 32
#---
```

Here `ENSEMBL BFMPP` stands for Endembl Bacteria, Fungi, Metazoa, Plants and Protists.
So the comment is indicating that this is Ensembl's release 32.



## Number of variants in VCF and HTML summary do not match

First of all, SnpEff is probably giving you the right numbers, the mismatch might not be a bug, but a simple interpretation issue.

### Counting variants vs lines in a VCF file

It is important to remember that the VCF format specification allows having multiple variants in a single line.
For example, here is a VCF line with multiple variants:

```
#CHROM  POS         ID  REF  ALT       QUAL  FILTER  INFO
chr2    115032192   .   G    GT,GTT    30    PASS    ...
```

In this case, the `ALT` field has more than one value separated by comma.
This means that the line contains two variants: 

- `G -> GT` 
- `G -> GTT`

So counting the number of non-comment lines in the VCF file will not give you the exact number of variants (it does give a lower bound on the number of variants).

### Counting variants vs annotations

Also, a single variant can have more than one annotation, due to:

- Multiple transcripts (isoforms) of a gene (e.g. the human genome has on average 8.8 transcrips per gene)
- Multiple (overlapping) genes in the genomic location of the variant.
- A variant spanning multiple genes (e.g. a translocation, large deletion, etc.)

So counting the number of variants is not equivalent to counting the number of annotations.
SnpEff does consider all these factors when counting the variants and annotations for the summary HTML.

### IUPAC expansion

Sometimes either the `REF` or `ALT` fields have [IUPAC/IUB](https://en.wikipedia.org/wiki/Nucleic_acid_notation) bases.
A common example is when you see an `N` character, which means that the base could be any of `{A, C, G, T}`.
There are several IUPAC characters, please see details in [IUPAC degenarate base symbols table](https://en.wikipedia.org/wiki/Nucleic_acid_notation).

In case of having IUPAC symbols in either the `REF` and/or `ALT` fields, SnpEff will expand them into different variants.
This means that entries with ambiguous symbols will be tranformed into all possible combinations of variants using the IUPAC notation.

!!! info
	You can disable the 'IUPAC/IUB expand' behaviour by using the `-noexpandiub` command line option.


For example, consider the following VCF line:

```
#CHROM  POS         ID  REF  ALT  QUAL  FILTER  INFO
chr1    102947631   .   T    N    30    PASS    ...
```

In this case the ambiguous variant `T -> N` will be expanded into three variants: 

- `T -> A`
- `T -> C`
- `T -> G`

!!! warning
	If the number of degenerate symbols increases, the number of variants expanded will increase exponentiallly.
	Currently SnpEff will not expand more than `MAX_IUB_BASES=10` bases


### Typical counting mistakes

Many people who claim that there is a mismatch between the number of variants in the summary (HTML) file and the number of variants in the VCF file, are just making mistakes when counting the variants because they forget one or more of these previously discussed items.

The most typical mistake is counting the number of non-comment Lines in a VCF file:

```
# This does NOT give the exact number of variants (only a lower bound on the number of variants)
grep -v '^#' myfile.vcf | wc -l
```

Another typical scenario is, when people are "counting missense variants" using something like this:

```
grep missense file.vcf | wc -l
```

This is counting _"lines in a VCF file that have at least one missense variants"_, as opposed to counting _"missense annotations"_ and, as mentioned previously, the number of lines in a VCF file is not the same as the number of annotations or the number of variants.


## SnpEff taking too long

Usually SnpEff runs within minutes.
Unless you are analyzing extremely large files with thousands (or hundreds of thousands) of samples.
But even in those cases SnpEff is efficient and it doesn't take too long.

There are several things you should do to optimize:

1. Run with "-v" option to check progress
1. Use enough memory in your Java process (see "How much memory should I use" FAQ)
1. You can disable the HTML report (command line option `-noStats`). The report is usually quite time consuming, particularly if the number of samples in the VCF is large



## How much memory should I use

How much memory to use is very specifcic to your project / application, but here are some guidelines:
- Default 8 GB: Typically 8G of memory is enough for analyzing a human genome (i.e. `java -Xmx8G -jar snpEff.jar ... ~)
- Medium 16 GB: It is rare that for single sample VCF file annotations more than 8G is required, but for some large genomes and/or VCF with too many samples, you might need more memory.
- Very large 128GB: It is extremely uncommon for SnpEff to require over 128GB of RAM for annotating with SnpEff, but it might happen on very large projects.



## Multiple version of RefSeq transcripts

When using RefSeq transcripts, for instance in the human genome versions `hg38` or `hg19`, can lead to some confusion due to multiply mapped transcripts.

**Example: `NM_001135865.1` from hg38**

From the original RefSeq data, you can see that there are actually four mappings of NM_001135865.1:
```
# Note: Output edited for readbility
$ zgrep NM_001135865.1 ~/snpEff/data/hg38/genes.refseq.gz

751 NM_001135865.1  chr16   -   21834582    21857657    21834717    21857378    11  ...
756 NM_001135865.1  chr16   +   22513522    22536520    22513801    22536385    7   ...
597 NM_001135865.1  chr16_KV880768v1_fix    +   1679394 1702742 1679673 1702607 11  ...
589 NM_001135865.1  chr16_KV880768v1_fix    -   568516  591514  568651  591235  7   ...
```

!!! warning
	To make matters even worse, not only `NM_001135865.1` maps twice to regions in `chr16`, but also one is mapped in the forward strand and the other on the reverse strand (notice the `+` and `-` signs)

How do you know which of the four `NM_001135865.1` version is SnpEff refering to?
When there are multiple mappings for a transcipt SnpEff will make sure each mapping is uniquely identified by appending a number to the original transcript ID.

So the transcript IDs are named (notice that the first one is not changed):

- `NM_001135865.1`
- `NM_001135865.1.2`
- `NM_001135865.1.3`
- `NM_001135865.1.4`

Even though they are mapped to different chromosomes and strands in `chr16`, the protein sequence will be very similar (that's why RefSeq has multiple mappings of the same transcript).


!!! info
	You can get details of each transcript using the SnpEff `show` command (e.g. `java -jar snpEff.jar show ...`)

We can analyse the difference, for instance `NM_001135865.1` and `NM_001135865.1.4` are mapped to `chr16`.
If you look at the protein sequences you'll notice that there is one small difference in amino acid 138 ('G' vs 'V'):

```
$ java -jar snpEff.jar show NM_001135865.1 NM_001135865.1.4 | tee show.txt
# Note: Output edited for readability
#
# Scroll right to see the difference ------>>>                                                                                                           | AA 138
#                                                                                                                                                        |
/Users/kqrw311/snpEff/issue_284$                                                                                                                         |
Showing genes and transcripts using zero-based coordinates                                                                                               |
Transcript (codon table: Standard ) :   16:22513522-22536519, strand: +, id:NM_001135865.1, Protein, DNA check                                           |
    ...                                                                                                                                                  |
    Protein :   MVKLSIVLTPQFLSHDQGQLTKELQQHVKSVTCPCEYLRKVINTLADHHHRGTDFGGSPWLHVIIAFPTSYKVVITLWIVYLWVSLLKTIFWSRNGHDGSTDVQQRAWRSNRRRQEGLRSICMHTKKRVSSFRGNKIGLKDVITLRRHVETKVRAKIRKRKVTTKINHHDKINGKRKTARKQKMFQRAQELRRRAEDYHKCKIPPSARKALCNWVRMA...
    ...                                                                                                                                                  | NM_001135865.1 has a 'G'
                                                                                                                                                         |
Transcript (codon table: Standard ) :   16:21834582-21857656, strand: -, id:NM_001135865.1.4, Protein                                                    |
    ...                                                                                                                                                  |
    Protein :   MVKLSIVLTPQFLSHDQGQLTKELQQHVKSVTCPCEYLRKVINTLADHHHRGTDFGGSPWLHVIIAFPTSYKVVITLWIVYLWVSLLKTIFWSRNGHDGSTDVQQRAWRSNRRRQEGLRSICMHTKKRVSSFRGNKIVLKDVITLRRHVETKVRAKIRKRKVTTKINHHDKINGKRKTARKQKMFQRAQELRRRAEDYHKCKIPPSARKALCNWVRMA...
    ...                                                                                                                                                  | NM_001135865.1.4 has a 'V'

```


## Cannot build database: `ERROR: Database check failed.`

When building databases, SnpEff will attempt to check the database against a CDS FASTA file and a Protein FASTA file.
Please see details in ['Building databases', section 'Step 3: Checking the database'](./build_db.md#step-3-checking-the-database)

If neither a CDS FASTA file nor a Protein FASTA file is provided, this check will fail and SnpEff will refuse to save the database, by showing error message like this one:

```
ERROR: CDS check file './data/MY_GENOME/cds.fa' not found.
ERROR: Protein check file './data/MY_GENOME/protein.fa' not found.
ERROR: Database check failed.
```

To disable these checks, you need to specify *BOTH* commmand line options `-noCheckCds -noCheckProtein`.
