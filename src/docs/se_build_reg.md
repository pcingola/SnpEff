# Building databases: Regulatory and Non-coding

SnpEff supports regulatory and non-coding annotations.
In this section we show how to build those databases.
As in the previous section, most likely you will never have to do it yourself and can just use available pre-built databases.

There are two ways to add support for regulatory annotations (these are not mutually exclusive, you can use both at the same time):

1. GFF regulation file (from ENSEMBL).
2. BED files.

!!! warning
    Adding regulation support and analyzing data using regulation tracks can take much more memory. For instance, for the human genome I use 10Gb to 20Gb of RAM.

!!! warning
    It is assumed the the genome is already installed, only regulatory tracks are added.

### Option 1: Using a GFF file

This example shows how to create a regulation database for human (GRCh37.65):

1. Get the GFF regulatory annotations (into path/to/snpEff/data/GRCh37.65/regulation.gff):

        cd path/to/snpEff/data/GRCh37.65
        wget ftp:/ftp.ensembl.org/pub/release-65/regulation/homo_sapiens/AnnotatedFeatures.gff.gz
        mv AnnotatedFeatures.gff.gz regulation.gff.gz 

2. Create databases. Note that we use `-onlyReg` flag, because we are only creating regulatory databases. If you omit it, it will create both of "normal' and regulatory databases:

        cd /path/to/snpEff
        java -Xmx20G -jar snpEff.jar build -v -onlyReg GRCh37.65 

    The output looks like this:

        Reading regulation elements (GFF)
            Chromosome '11'	line: 226964
            Chromosome '12'	line: 493780
            ...
            Chromosome '9'	line: 4832434
            Chromosome 'X'  line: 5054301
            Chromosome 'Y'  line: 5166958
        Done
            Total lines                 : 5176289
            Total annotation count      : 3961432
            Percent                     : 76.5%
            Total annotated length      : 3648200193
            Number of cell/annotations  : 266
        Saving database 'HeLa-S3' in file '/path/to/snpEff/data/GRCh37.65/regulation_HeLa-S3.bin'
        Saving database 'HepG2' in file '/path/to/snpEff/data/GRCh37.65/regulation_HepG2.bin'
        Saving database 'NHEK' in file '/path/to/snpEff/data/GRCh37.65/regulation_NHEK.bin'
        Saving database 'GM12878' in file '/path/to/snpEff/data/GRCh37.65/regulation_GM12878.bin'
        Saving database 'HUVEC' in file '/path/to/snpEff/data/GRCh37.65/regulation_HUVEC.bin'
        Saving database 'H1ESC' in file '/path/to/snpEff/data/GRCh37.65/regulation_H1ESC.bin'
        Saving database 'CD4' in file '/path/to/snpEff/data/GRCh37.65/regulation_CD4.bin'
        Saving database 'GM06990' in file '/path/to/snpEff/data/GRCh37.65/regulation_GM06990.bin'
        Saving database 'IMR90' in file '/path/to/snpEff/data/GRCh37.65/regulation_IMR90.bin'
        Saving database 'K562' in file '/path/to/snpEff/data/GRCh37.65/regulation_K562.bin'
        Done.

    As you can see, annotations for each cell type are saved in different files. This makes it easier to load annotations only for the desired cell types when analyzing data.

### Option 2: Using an BED file

This example shows how to create a regulation database for human (GRCh37.65).
We assume we have a file called `my_regulation.bed` which has information for H3K9me3 in Pancreatic Islets (for instance, as a result of a Chip-Seq experiment and peak enrichment analysis).

1. Add all your BED files to `path/to/snpEff/data/GRCh37.65/regulation.bed/` dir:

        cd path/to/snpEff/data/GRCh37.65
        mkdir regulation.bed
        cd regulation.bed
        mv where/ever/your/bed/file/is/my_regulation.bed ./regulation.Pancreatic_Islets.H3K9me3.bed
    **Note:** The name of the file must be `regulation.CELL_TYPE.ANNOTATION_TYPE.bed`. In this case, `CELL_TYPE=Pancreatic_Islets` and `ANNOTATION_TYPE=H3K9me3`

2. Create databases (note the `-onlyReg` flag):

        cd /path/to/snpEff
        java -Xmx20G -jar snpEff.jar build -v -onlyReg GRCh37.65 
    The output looks like this:

        Building database for 'GRCh37.65'
        Reading regulation elements (GFF)
        Cannot read regulation elements form file '/path/to/snpEff/data/GRCh37.65/regulation.gff'
        Directory has 1 bed files and 1 cell types
        Creating consensus for cellType 'Pancreatic_Islets', files: [/path/to/snpEff/data/GRCh37.65/regulation.bed/regulation.Pancreatic_Islets.H3K9me3.bed]
        Reading file '/path/to/snpEff/data/GRCh37.65/regulation.bed/regulation.Pancreatic_Islets.H3K9me3.bed'
            Chromosome '10'	line: 5143
            Chromosome '11'	line: 8521
            ...
            Chromosome 'X'	line: 52481
            Chromosome 'Y'	line: 53340
        Done
            Total lines                 : 53551
            Total annotation count      : 53573
            Percent                     : 100.0%
            Total annotated length      : 75489402
            Number of cell/annotations  : 1
        Creating consensus for cell type: Pancreatic_Islets
        Sorting: Pancreatic_Islets	, size: 53573
        Adding to final consensus
        Final consensus for cell type: Pancreatic_Islets	, size: 53549
        Saving database 'Pancreatic_Islets' in file '/path/to/snpEff/data/GRCh37.65/regulation_Pancreatic_Islets.bin'
        Done
        Finishing up

    **Note:** If there are many annotations, they are saved in one binary file for each cell type (i.e. several BED files for different cell types are collapsed together).
    This makes it easier to load annotations only for the desired cell types when analyzing data.
