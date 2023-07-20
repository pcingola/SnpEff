# SnpSift phastCons

Annotate using PhastCons conservation scores.

!!! info
    You must download PhastCons files [here](http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/).

!!! info
    You also need a chromosome size file, which can be created using `samtools faidx`, or you can download it from [here](../adds/genome.fai).

Full example.
Most of the example deals with downloading and installing PhastCons database, which is done only once.
The real annotation process is done in the last line.
```
# Create a dir for PhastCons database
cd ~/snpEff
mkdir -p db/phastCons/

# Download all PhastCons files
cd db/phastCons/
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr1.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr2.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr3.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr4.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr5.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr6.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr7.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr8.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr9.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr10.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr11.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr12.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr13.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr14.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr15.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr16.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr17.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr18.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr19.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr20.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr21.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chr22.phastCons100way.wigFix.gz                 
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chrM.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chrX.phastCons100way.wigFix.gz                  
wget http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/chrY.phastCons100way.wigFix.gz

# Create a chromosome size file and name it "genome.fai"
samtools faidx path/to/genome/hg19.fa.gz
cp path/to/genome/hg19.fa.gz.fai ./genome.fai

# Now we are ready to annotate
java -Xmx8g -jar SnpSift.jar phastCons ~/snpEff/db/phastCons file.vcf > file.phastCons.vcf
```

You can annotate intervals using BED files and `-bed` command line option.
In the output BED formatted intervals, the score column (fifth column), is the average conservation score of all bases within the interval.

It is possible to extract sub-intervals having at least 'minScore' conservation score and 'len' length by using `-minScore score` and `-extract len` command line options.
For instance, the following command:

    java -jar SnpSift.jar phastCons -minScore 0.8 -extract 10 -bed path/to/phastCons/dir input.bed
extracts all subintervals from each line in `input.bed`, that has at least `10` bases length and a conservation score of `0.8`
