# Building databases

SnpEff needs a database to perform genomic annotations.
There are pre-built databases for over 2,500 genomes, so chances are that your organism of choice already has a SnpEff database available.
In the (unlikely?) event that you need to build one yourself, here we describe how to it.

!!! info
    You can know which genomes are supported by running the following command:

        $ java -jar snpEff.jar databases

!!! warning
    Most people do NOT need to build a database, and can safely use a pre-built one.
    So unless you are working with an rare genome you most likely don't need to do it either.

!!! warning
    Again, chances are that you DO NOT NEED to build a database.
    Sorry to repeat this, but I cannot tell how many times I get emails asking for help to build database that is already available.

### Managing SnpEff databases manually

SnpEff databases for the most popular genomes are already pre-built and available for you to download.
So, chances are that you don't need to build a database yourself (this will save you a LOT of work).

!!! warning
    By default SnpEff automatically downloads and installs the database for you, so you don't need to do it manually.
    The following instructions are for people that want to pre-install databases manually (again, most people don't need to do this).

The easiest way to download and install a pre-built SnpEff database manually, is using the `download` command.
E.g. if you want to install the SnpEff database for the human genome, you can run the following command:

    $ java -jar snpEff.jar download -v GRCh37.75

!!! info
    If you are running SnpEff from a directory different than the one it was installed, you will have to specify where the config file is.
    This is done using the `-c` command line option:

        $ java -Xmx8g -jar snpEff.jar download -c path/to/snpEff/snpEff.config -v GRCh37.75

### Building a database

In order to build a database for a new genome, you need to:

!!! warning
    Most people do NOT need to build a database, and can safely use a pre-built one.
    So unless you are working with a rare genome you most likely don't need to do it either.

1. Configure a new genome in SnpEff's config file `snpEff.config`.

     1. [Add genome entry](#add-a-genome-to-the-configuration-file) to snpEff's configuration
     2. If the genome uses a non-standard codon table: [Add codon table parameter](#configuring-codon-tables-not-always-required)

2. Get the reference genome sequence (e.g. in FASTA format).
3. Get genome annotations. There are four different ways you can do this:

    1. [Option 1:](#option-1-building-a-database-from-gtf-files) Building a database from GTF files (the easiest way)
    2. [Option 2:](#option-2-building-a-database-from-gff-files) Building a database from GFF files
    3. [Option 3:](#option-3-building-a-database-from-refseq-table-from-ucsc) Building a database from RefSeq table from UCSC
    4. [Option 4:](#option-4-building-a-database-from-genbank-files) Building a database from GenBank files

4. Run a command to create the database (i.e. `java -jar snpEff.jar build ...`"`)

**Note:** All files can be compressed using gzip. E.g. the reference file 'hg19.fa' can be compressed to 'hg19.fa.gz', snpEff will automatically decompress the file.

!!! warning
    Some files claimed to be compressed using GZIP are actually not or even use a block compression variant not supported by Java's gzip library.
    If you notice that your build process finishes abruptly for no apparent reason, try uncompressing the files.
    This sometimes happens with ENSEMBL files.

### Configuring a new genome

In order to tell SnpEff that there is a new genome available, you must update SnpEff's configuration file `snpEff.config`.

You must add a new genome entry to `snpEff.config`.

If your genome, or a chromosome, uses non-standard codon tables you must update `snpEff.config` accordingly.
A typical case is when you use mitochondrial DNA. Then you specify that chromosome 'MT' uses `codon.Invertebrate_Mitochondrial` codon table.
Another common case is when you are adding a bacterial genome, then you specify that the codon table is `Bacterial_and_Plant_Plastid`.

#### Add a genome to the configuration file

This example shows how to add a new genome to the config files. For this example we'll use the mouse genome (mm37.61):

1. Edit the config file to create the new genome:

        vi snpEffect.config

    Add the following lines (you are editing snpEffect.config):

        # Mouse genome, version mm37.61
        mm37.61.genome : Mouse

    !!! warning
        You may need to add codon table information for the genome or some parts of it (e.g. mitochondrial "chromosome").
        See next section for details.

2. Optional: Add genome to Galaxy's menu:

        cd /path/to/galaxy
        cd tools/snpEffect/
        vi snpEffect.xml 

    Add the following lines to the file:

        <param name="genomeVersion" type="select" label="Genome">
            <option value="hg37">Human (hg37)<option>
            <option value="mm37.61">Mouse (mm37.61)<option>
        <param>

#### Configuring codon tables (not always required)

Codon tables are provided in the ```snpEff.config``` configuration file under the section ```codon.Name_of_your_codon_table```.
The format is a comma separated list of ```CODON/AMINO_ACID```.

E.g.:

    codon.Invertebrate_Mitochondrial:  TTT/F, TTC/F, TAC/Y, TAA/*, ATG/M+, ATG/M+, ACT/T, ...

Note that codons marked with '*' are STOP codons and codons marked with a '+' are START codons.

In order for you to use them, you have to specify that a given "chromosome" uses one of the tables (otherwise the default codon table is used).

E.g. Here we say the chromosome 'M' from fly genome (dm3) uses Invertebrate_Mitochondrial codon table:

    dm3.M.codonTable : Invertebrate_Mitochondrial

...of course, chromosome 'M' is not a real chromosome, it is just a way to mark the sequence as mitochondrial DNA in the reference genome.

### Reference genome: GTF, GFF, RefSeq or GenBank

As we previously mentioned, reference genome information can be in different formats: GTF, GFF, RefSeq or GenBank.

In the following sub-sections, we show how to build a database for each type of genomic information file.

#### Option 1: Building a database from GTF files

GTF 2.2 files are supported by SnpEff (e.g. ENSEMBL releases genome annotations in this format).

1. Get the genome and uncompress it:

        # Create directory for this new genome
        cd /path/to/snpEff/data/
        mkdir mm37.61
        cd mm37.61
        
        # Get annotation files
        wget ftp://ftp.ensembl.org/pub/current/gtf/mus_musculus/Mus_musculus.NCBIM37.61.gtf.gz
        mv Mus_musculus.NCBIM37.61.gtf.gz genes.gtf.gz
        
        # Get the genome
        cd /path/to/snpEff/data/genomes
        wget ftp://ftp.ensembl.org/pub/current/fasta/mus_musculus/dna/Mus_musculus.NCBIM37.61.dna.toplevel.fa.gz
        mv Mus_musculus.NCBIM37.61.dna.toplevel.fa.gz mm37.61.fa.gz

2. **Note:** The FASTA file can be either in <br> `/path/to/snpEff/data/genomes/mm37.61.fa` <br>or in <br> `/path/to/snpEff/data/mm37.61/sequences.fa`
3. Add the new genome to the config file (see [Add a new genome to the configuration file](#add-a-genome-to-the-configuration-file) for details)
4. Create database:

        cd /path/to/snpEff
        java -jar snpEff.jar build -gtf22 -v mm37.61

#### Option 2: Building a database from GFF files

!!! warning
    Using GFF is discouraged, we recommend you use GTF files instead (whenever possible).

This example shows how to create a database for a new genome using GFF file ((e.g. FlyBase, WormBase, BeeBase release GFF files).
For this example we'll use the Drosophila melanogaster genome (dm5.31):

1. Get a GFF file (into path/to/snpEff/data/dm5.31/genes.gff):

        mkdir path/to/snpEff/data/dm5.31
        cd path/to/snpEff/data/dm5.31
        wget ftp://ftp.flybase.net/genomes/Drosophila_melanogaster/dmel_r5.31_FB2010_08/gff/dmel-all-r5.31.gff.gz
        mv dmel-all-r5.31.gff.gz genes.gff.gz

2. **Note:** GFF3 files can include the reference sequence in the same file. This is done by dumping the fasta file after a '##FASTA' line. You can also add the sequence fasta file to the 'data/genomes/' directory, like it is done in when using GTF format.
3. Add the new genome to the config file (see [Add a new genome to the configuration file](#add-a-genome-to-the-configuration-file) for details)
4. Create database (note the "-gff3" flag):

        cd /path/to/snpEff
        java -jar snpEff.jar build -gff3 -v dm5.31

#### Option 3: Building a database from RefSeq table from UCSC

This example shows how to create a database for a new genome.
For this example we'll use the Human genome (hg19).

!!! warning
    Using UCSC genome tables is highly discouraged, we recommend you use ENSEMBL versions instead.

!!! warning
    UCSC tables sometimes change for different species.
    This means that even if these instructions work for human genome, it might not work for other genomes.
    Obviously creating a new parser for each genome is impractical, so working with UCSC genomes is highly discouraged.
    We recommend to use ENSEMBL genomes instead.

!!! warning
    UCSC genomes provide only major release version, but NOT sub-versions.
    E.g. UCSC's "hg19" has major version 19 but there is no "sub-version", whereas ENSEMBL's GRCh37.70 clearly has major version 37 and minor version 70.
    Not providing a minor version means that they might change the database and two "hg19" genomes are actually be different.
    This creates all sorts of consistency problems (e.g. the annotations may not be the same that you see in the UCSC genome browser, even though both of them are 'hg19' version).
    Using UCSC genome tables is highly discouraged, we recommend you use ENSEMBL versions instead.

In order to build a genome using UCSC tables, you can follow these instructions:

1. Go to [UCSC genome browser](http://genome.ucsc.edu/)
2. Click on "Table" menu
3. Select parameters as shown here:

    ![uscs_ref_Seq](images/ucsc_refSeq.png){: .center}

4. Click on "get output" and save the data to file "/path/to/snpEff/data/hg19/genes.refseq".
5. Add the fasta reference genome. The FASTA file can be either in:

    `/path/to/snpEff/data/genomes/hg19.fa`

    or in:

    `/path/to/snpEff/data/hg19/sequences.fa`

6. Add the new genome to the config file (see [Add a new genome to the configuration file](#add-a-genome-to-the-configuration-file) for details)
7. Create database (note the `-refSeq` flag):

        cd /path/to/snpEff
        java -jar snpEff.jar build -refSeq -v hg19

#### Option 4: Building a database from GenBank files

This example shows how to create a database for a new genome.
For this example we'll use "Staphylococcus aureus":

1. Go to NIH page for [CP000730](http://www.ncbi.nlm.nih.gov/nuccore/CP000730.1)
2. Download the features in geneBank format, by clicking as shown in the following images (red arrows):

    ![genBank0](images/genBank_0.png){: .center}

    Make sure you click the "Update" button!

    Then you go to the "Send" menu:

    ![genBank1](images/genBank_1.png){: .center}

    and then:

    ![genBank2](images/genBank_2.png){: .center}

3. Save the GenBank data to "/path/to/snpEff/data/CP000730/genes.gbk".

    **Note:** If there are more than one genbank file for an organism (e.g. multiple chromosomes), then you can download each file and concatenate them.

    E.g.: Vibrio Cholerae has two chromosomes with GenBank accessions: NC_002505.1 and NC_002506.1.
    You can download both files and save them as snpEff/data/vibrio/NC_002505.1.gbk and snpEff/data/vibrio/NC_002506.1.gbk respectively, and then concatenate both files:

        cat NC_002505.1.gbk NC_002506.1.gbk > genes.gbk

    Add the following entries in the config file:

        # Vibrio Cholerae
        vibrio.genome : Vibrio Cholerae
                vibrio.chromosomes : NC_002505.1, NC_002506.1
                vibrio.NC_002505.1.codonTable : Bacterial_and_Plant_Plastid
                vibrio.NC_002506.1.codonTable : Bacterial_and_Plant_Plastid

4. Create database (note the `-genbank` flag):

        cd /path/to/snpEff
        java -jar snpEff.jar build -genbank -v CP000730

### Example: Building the Human Genome database

This is a full example on how to build the human genome database (using GTF file from ENSEBML), it includes support for regulatory features, sanity check, rare amino acids, etc..
```
# Go to SnpEff's install dir
cd ~/snpeff

# Create database dir
mkdir data/GRCh37.70
cd data/GRCh37.70

# Download annotated genes
wget ftp://ftp.ensembl.org/pub/release-70/gtf/homo_sapiens/Homo_sapiens.GRCh37.70.gtf.gz
mv Homo_sapiens.GRCh37.70.gtf.gz genes.gtf.gz

# Download proteins
# This is used for:
#	- "Rare Amino Acid" annotations
#	- Sanity check (checking protein predicted from DNA sequences match 'real' proteins)
wget ftp://ftp.ensembl.org/pub/release-70/fasta/homo_sapiens/pep/Homo_sapiens.GRCh37.70.pep.all.fa.gz
mv Homo_sapiens.GRCh37.70.pep.all.fa.gz protein.fa.gz

# Download CDSs
# Note: This is used as "sanity check" (checking that CDSs predicted from gene sequences match 'real' CDSs)
wget ftp://ftp.ensembl.org/pub/release-70/fasta/homo_sapiens/cdna/Homo_sapiens.GRCh37.70.cdna.all.fa.gz
mv Homo_sapiens.GRCh37.70.cdna.all.fa.gz cds.fa.gz

# Download regulatory annotations
wget ftp://ftp.ensembl.org/pub/release-70/regulation/homo_sapiens/AnnotatedFeatures.gff.gz
mv AnnotatedFeatures.gff.gz regulation.gff.gz

# Uncompress
gunzip *.gz

# Download genome
cd ../genomes/
wget ftp://ftp.ensembl.org/pub/release-70/fasta/homo_sapiens/dna/Homo_sapiens.GRCh37.70.dna.toplevel.fa.gz
mv Homo_sapiens.GRCh37.70.dna.toplevel.fa.gz GRCh37.70.fa.gz

# Uncompress:
# Why do we need to uncompress?
# Because ENSEMBL compresses files using a block compress gzip which is not compatible with Java's library Gunzip
gunzip GRCh37.70.fa.gz

# Edit snpEff.config file
#
# WARNING! You must do this yourself. Just copying and pasting this into a terminal won't work.
#
# Add lines:
#		GRCh37.70.genome : Homo_sapiens
#		GRCh37.70.reference : ftp://ftp.ensembl.org/pub/release-70/gtf/

# Now we are ready to build the database
cd ~/snpeff
java -Xmx20g -jar snpEff.jar build -v GRCh37.70 2>&1 | tee GRCh37.70.build
```

### Troubleshooting Database builds

!!! warning
    By far the most common problem is that the FASTA file chromosome names are different than the GFF chromosome names.
    Make sure chromosome names are consistent in all the files you use.

**When I build the database using GFF 3 SnpEff reports that Exons don't have sequences**

GFF3 files can have sequence information either in the same file or in a separate fasta file.

In order to add sequence information in the GFF file, you can do this:
```
cat annotations.gff > genes.gff
echo "###"  >> genes.gff
echo "##FASTA"  >> genes.gff
cat sequence.fa  >> genes.gff
```
**When building a database, I get zero protein coding genes**

When building a database, snpEff tries to find which transcripts are protein coding. This is done using the 'bioType' information.

The bioType information is not a standard GFF or GTF feature. So I follow ENSEMBL's convention of using the second column ('source') for bioType, as well as the gene_biotype attribute.

If your file was not produced by ENSEMBL, it probably doesn't have this information. This means that snpEff doesn't know which genes are protein coding and which ones are not.

Having no information, snpEff will treat all genes as protein coding (assuming you have `-treatAllAsProteinCoding Auto` option in the command line, which is the default).

So you will get effects as if all genes were protein coding, then you can filter out the irrelevant genes. Unfortunately, this is the best I can do if there is no 'bioType' information

**When building a database, I get too many warnings**

There are plenty of GFF and GTF files that, unfortunately, do not follow the specification.
SnpEff usually complains about this, but tries hard to correct the problems.
So the database may be OK even after you see many warnings.

You can check the database to see if the features (genes, exons, UTRs) have been correctly incorporated, by taking a look at the database:

    java -jar snpEff.jar dump myGenome | less
