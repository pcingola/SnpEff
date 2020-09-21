# Additional annotations

SnpEff can also provide non-coding and regulatory annotations. Here we show how to annotate them.

### Regulatory annotations

!!! warning
    Non-coding and regulatory annotations databases are available only for a few organisms (e.g. human, mouse, etc.).
    We intend to incorporate more non-coding annotations as soon as public databases are available, but your organism of choice might not have a non-coding/regulatory database available.

First of all, you need to see if your organism has a regulatory database.
You can just look into the database directory to see if `regulation_*.bin` files are there.
For instance, for human genome:
```
$ cd ~/snpEff
$ cd data/GRCh37.75/
$ ls -al
drwxrwxr-x 2 pcingola pcingola     4096 Aug 26 19:51 .
drwxrwxr-x 3 pcingola pcingola     4096 Aug 26 19:51 ..
-rw-rw-r-- 1 pcingola pcingola  5068097 Aug 26 19:51 motif.bin
-rw-rw-r-- 1 pcingola pcingola  5469036 Aug 26 19:51 nextProt.bin
-rw-rw-r-- 1 pcingola pcingola    38000 Aug 26 19:51 pwms.bin
-rw-rw-r-- 1 pcingola pcingola  6399582 Aug 26 19:51 regulation_CD4.bin
-rw-rw-r-- 1 pcingola pcingola  2516472 Aug 26 19:51 regulation_GM06990.bin
-rw-rw-r-- 1 pcingola pcingola  8064939 Aug 26 19:51 regulation_GM12878.bin
-rw-rw-r-- 1 pcingola pcingola  6309932 Aug 26 19:51 regulation_H1ESC.bin
-rw-rw-r-- 1 pcingola pcingola  5247586 Aug 26 19:51 regulation_HeLa-S3.bin
-rw-rw-r-- 1 pcingola pcingola  7506893 Aug 26 19:51 regulation_HepG2.bin
-rw-rw-r-- 1 pcingola pcingola  4064952 Aug 26 19:51 regulation_HMEC.bin
-rw-rw-r-- 1 pcingola pcingola  4644239 Aug 26 19:51 regulation_HSMM.bin
-rw-rw-r-- 1 pcingola pcingola  5641615 Aug 26 19:51 regulation_HUVEC.bin
-rw-rw-r-- 1 pcingola pcingola  5617233 Aug 26 19:51 regulation_IMR90.bin
-rw-rw-r-- 1 pcingola pcingola   546871 Aug 26 19:51 regulation_K562b.bin
-rw-rw-r-- 1 pcingola pcingola  8542718 Aug 26 19:51 regulation_K562.bin
-rw-rw-r-- 1 pcingola pcingola  3119671 Aug 26 19:51 regulation_NH-A.bin
-rw-rw-r-- 1 pcingola pcingola  5721741 Aug 26 19:51 regulation_NHEK.bin
-rw-rw-r-- 1 pcingola pcingola 94345546 Aug 26 19:51 snpEffectPredictor.bin
```
So we can annotate using any of those tracks.

E.g. To use 'HeLa-S3' and 'NHEK' tracks, you can run:
```
$ java -Xmx8g -jar snpEff.jar -v -reg HeLa-S3 -reg NHEK GRCh37.75 examples/test.1KG.vcf > test.1KG.ann_reg.vcf
00:00:00.000	Reading configuration file 'snpEff.config'. Genome: 'GRCh37.75'
00:00:00.377	done
00:00:00.377	Reading database for genome version 'GRCh37.75' from file '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/snpEffectPredictor.bin' (this might take a while)
00:00:25.845	done
00:00:25.878	Reading regulation track 'NHEK'
00:00:30.137	Reading regulation track 'HeLa-S3'
...


# Show one example of "regulatory_region" (output edited for readability)
$ grep -i regulatory_region test.1KG.ann_reg.vcf | head -n 1 | ./scripts/vcfInfoOnePerLine.pl
1    10291    .    C    T    2373.79    .    ANN=T|regulatory_region_variant|MODIFIER|||REGULATION&H3K36me3:NHEK|NHEK_H3K36me3_5|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&H3K27me3:NHEK|NHEK_H3K27me3_4|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&Max:HeLa-S3|HeLa-S3_Max_26|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&Cfos:HeLa-S3|HeLa-S3_Cfos_30|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&FAIRE:HeLa-S3|HeLa-S3_FAIRE_49|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&H3K27ac:HeLa-S3|HeLa-S3_H3K27ac_88|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&PolII:NHEK|NHEK_PolII_59|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&CTCF:NHEK|NHEK_CTCF_42|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&Cmyc:HeLa-S3|HeLa-S3_Cmyc_16|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&H4K20me1:NHEK|NHEK_H4K20me1_122|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&H3K4me3:NHEK|NHEK_H3K4me3_133|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&DNase1:HeLa-S3|HeLa-S3_DNase1_108|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&DNase1:NHEK|NHEK_DNase1_63|||||||||
                                                ,T|regulatory_region_variant|MODIFIER|||REGULATION&FAIRE:NHEK|NHEK_FAIRE_149|||||||||
```

### ENCODE

ENCODE project's goal is to find all functional elements in the human genome.
You can perform annotations using ENCODE's data.

[ENCODE](http://genome.ucsc.edu/ENCODE/) project has produced huge amounts of data (see also [Nature's](http://www.nature.com/encode) portal).
This information is available for download and can be used to annotate genomic variants or regions.
An overview of all the data available from ENCODE is shown as an [experimental data matrix](http://genome.ucsc.edu/ENCODE/dataMatrix/encodeDataMatrixHuman.html).
The download site is [here](http://genome.ucsc.edu/ENCODE/downloads.html).

Data is available in "BigBed" format, which can be feed into SnpEff using `-interval` command line option (you can add many `-interval` options).

Here is a simple example:
```
# Create a directory for ENCODE files
mkdir -p db/encode

# Download ENCODE experimental results (BigBed file)
cd db/encode
wget "http://ftp.ebi.ac.uk/pub/databases/ensembl/encode/integration_data_jan2011/byDataType/openchrom/jan2011/fdrPeaks/wgEncodeDukeDnase8988T.fdr01peaks.hg19.bb"

# Annotate using ENCODE's data:
java -Xmx8g -jar snpEff.jar -v -interval db/encode/wgEncodeDukeDnase8988T.fdr01peaks.hg19.bb GRCh37.75 examples/test.1KG.vcf > test.1KG.ann_encode.vcf

# Annotations are added as "CUSTOM" intervals:
$ grep CUSTOM test.1KG.ann_encode.vcf | head
1    564672    .    A    C    812.29    .    ANN=|custom|MODIFIER|||CUSTOM&wgEncodeDukeDnase8988T|wgEncodeDukeDnase8988T:564666_564815|||||||||
1    564687    .    C    T    308.21    .    ANN=T|custom|MODIFIER|||CUSTOM&wgEncodeDukeDnase8988T|wgEncodeDukeDnase8988T:564666_564815|||||||||
...
1    956676    .    G    A    120.88    .    ANN=A|custom|MODIFIER|||CUSTOM&wgEncodeDukeDnase8988T|wgEncodeDukeDnase8988T:956646_956795|||||||||
...
```

### Epigenome

Epigenome Roadmap Project has produced large amounts of information that can be used by SnpEff.

[Epigenome Roadmap Project](http://www.roadmapepigenomics.org) goal is
"to map DNA methylation, histone modifications, chromatin accessibility and small RNA transcripts
in stem cells and primary ex vivo tissues selected to represent the normal counterparts of tissues
and organ systems frequently involved in human disease".
A [data matrix](http://www.roadmapepigenomics.org/data) shows the experimental set ups currently available.

Unfortunately the project is not (currently) providing results files that can be used directly by annotation software, such as SnpEff.
They will be available later in the project.
So, for the time being, data has to be downloaded an pre-processed.
We'll be processing these information and making it available (as SnpEff databases) as soon as we can.

The latest Epigenome project processed information, can be found [here](https://snpeff.blob.core.windows.net/databases/epigenome_latest.tgz/download).
This includes genomic intervals for high confidence peaks in form of `BED` files.

To annotate you can do:
```
# Download Epigenome project database (pre-processed as BED files)
wget https://snpeff.blob.core.windows.net/databases/epigenome_latest.tgz/download

# Open tar file
tar -xvzf epigenome_latest.tgz

# Annotate using SnpEff and "-interval" command line
java -Xmx8g -jar snpEff.jar -v -interval db/epigenome/BI_Pancreatic_Islets_H3K4me3.peaks.bed GRCh37.75 test.vcf > test.ann.vcf

# See the data represented as "CUSTOM" EFF fields
$ grep CUSTOM test.ann.vcf
1	894573	.	G	A	.	PASS	AC=725;EFF=CUSTOM[BI_Pancreatic_Islets_H3K4me3](MODIFIER||||||MACS_peak_8||||1),INTRON(MODIFIER||||749|NOC2L|protein_coding|CODING|ENST00000327044|1|1),INTRON(MODIFIER|||||NOC2L|processed_transcript|CODING|ENST00000487214|1|1),INTRON(MODIFIER|||||NOC2L|retained_intron|CODING|ENST00000469563|1|1),UPSTREAM(MODIFIER||||642|KLHL17|protein_coding|CODING|ENST00000338591||1),UPSTREAM(MODIFIER|||||KLHL17|nonsense_mediated_decay|CODING|ENST00000466300||1),UPSTREAM(MODIFIER|||||KLHL17|retained_intron|CODING|ENST00000463212||1),UPSTREAM(MODIFIER|||||KLHL17|retained_intron|CODING|ENST00000481067||1),UPSTREAM(MODIFIER|||||NOC2L|retained_intron|CODING|ENST00000477976||1)
1	948692	.	G	A	.	PASS	AC=896;EFF=CUSTOM[BI_Pancreatic_Islets_H3K4me3](MODIFIER||||||MACS_peak_9||||1),INTERGENIC(MODIFIER||||||||||1),UPSTREAM(MODIFIER||||165|ISG15|protein_coding|CODING|ENST00000379389||1),UPSTREAM(MODIFIER|||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555||1)
1	948921	.	T	C	.	PASS	AC=904;EFF=CUSTOM[BI_Pancreatic_Islets_H3K4me3](MODIFIER||||||MACS_peak_9||||1),UPSTREAM(MODIFIER|||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555||1),UTR_5_PRIME(MODIFIER||||165|ISG15|protein_coding|CODING|ENST00000379389|1|1)
1	1099342	.	A	C	.	PASS	AC=831;EFF=CUSTOM[BI_Pancreatic_Islets_H3K4me3](MODIFIER||||||MACS_peak_10||||1),INTERGENIC(MODIFIER||||||||||1),UPSTREAM(MODIFIER|||||MIR200A|miRNA|NON_CODING|ENST00000384875||1),UPSTREAM(MODIFIER|||||MIR200B|miRNA|NON_CODING|ENST00000384997||1)
```
### NextProt

NextProt has useful proteomic annotations than can help to identify variants causing reduced protein functionality or even loss of function.

[Nextprot](http://www.nextprot.org/) project provides proteomic information that can be used for genomic annotations.
NextProt provides only human data.

Starting from SnpEff version 4.0, these annotations are automatically added if the database is available for the genome version you are using (in older SnpEff versions
the `-nextprot` command line option was used).
NextProt databases are available by for in some GRCh37 genomes (e.g. file `data/GRCh37.75/nextProt.bin`).

Annotations example:
```
$ java -Xmx8g -jar snpEff.jar -v GRCh37.75 examples/test.chr22.vcf > test.chr22.ann.vcf
00:00:00.000	Reading configuration file 'snpEff.config'. Genome: 'GRCh37.75'
00:00:00.374	done
00:00:00.374	Reading database for genome version 'GRCh37.75' from file '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/snpEffectPredictor.bin' (this might take a while)
00:00:25.880	done
00:00:25.913	Reading NextProt database from file '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/nextProt.bin'
...

# Show some results (edited for readability)
$ cat test.chr22.ann.vcf
...
22  17280941  .  T  C  .  .  ANN=C|sequence_feature|LOW|XKR3|ENSG00000172967|transmembrane_region:Transmembrane_region|ENST00000331428|protein_coding|2/4|c.336-27A>G||||||,C|sequence_feature|LOW|XKR3|ENSG00000172967|transmembrane_region:Transmembrane_region|ENST00000331428|protein_coding|3/4|c.336-27A>G||||||,C|intron_variant|MODIFIER|XKR3|ENSG00000172967|transcript|ENST00000331428|protein_coding|2/3|c.336-27A>G||||||
...
22  17472785  .  G  A  .  .  ANN=A|sequence_feature|LOW|GAB4|ENSG00000215568|domain:PH|ENST00000400588|protein_coding|2/10|c.456C>T||||||,A|sequence_feature|LOW|GAB4|ENSG00000215568|domain:PH|ENST00000400588|protein_coding|1/10|c.456C>T||||||,A|non_coding_exon_variant|MODIFIER|GAB4|ENSG00000215568|transcript|ENST00000465611|nonsense_mediated_decay|2/9|n.339C>T||||||,A|non_coding_exon_variant|MODIFIER|GAB4|ENSG00000215568|transcript|ENST00000523144|processed_transcript|2/4|n.341C>T||||||
...
22  50722408  .  T  C  .  .  ANN=C|sequence_feature|MODERATE|PLXNB2|ENSG00000196576|glycosylation_site:N-linked__GlcNAc..._|ENST00000359337|protein_coding|14/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|22/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|8/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|21/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|16/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|15/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|17/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|11/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|14/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|19/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|18/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|9/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|20/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|6/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|3/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|12/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|7/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|10/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|4/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|5/37|c.2275A>G||||||,C|sequence_feature|LOW|PLXNB2|ENSG00000196576|topological_domain:Extracellular|ENST00000359337|protein_coding|13/37|c.2275A>G||||||,C|upstream_gene_variant|MODIFIER|PLXNB2|ENSG00000196576|transcript|ENST00000479701|retained_intron||n.-1A>G|||||1417|,C|upstream_gene_variant|MODIFIER|PLXNB2|ENSG00000196576|transcript|ENST00000463165|retained_intron||n.-1A>G|||||2045|,C|upstream_gene_variant|MODIFIER|PLXNB2|ENSG00000196576|transcript|ENST00000492578|retained_intron||n.-1A>G|||||1973|,C|upstream_gene_variant|MODIFIER|PLXNB2|ENSG00000196576|transcript|ENST00000427829|protein_coding||c.-3A>G|||||1099|WARNING_TRANSCRIPT_INCOMPLETE,C|downstream_gene_variant|MODIFIER|PLXNB2|ENSG00000196576|transcript|ENST00000434732|protein_coding||c.*253A>G|||||188|WARNING_TRANSCRIPT_INCOMPLETE,C|downstream_gene_variant|MODIFIER|PLXNB2|ENSG00000196576|transcript|ENST00000432455|protein_coding||c.*1620A>G|||||4008|WARNING_TRANSCRIPT_NO_STOP_CODON,C|intron_variant|MODIFIER|PLXNB2|ENSG00000196576|transcript|ENST00000411680|protein_coding|2/5|c.202+5007A>G||||||WARNING_TRANSCRIPT_INCOMPLETE,C|non_coding_exon_variant|MODIFIER|PLXNB2|ENSG00000196576|transcript|ENST00000496720|processed_transcript|6/8|n.510A>G||||||
```
The last line in the example shows a `glycosylation_site` marked as `MODERATE` impact, since a modification of such a site might impair protein function.

### Motif

Motif annotations provided by ENSEMBL and Jaspar can be added to the standard annotations.

ENSEMBL provides transcription factor binding sites prediction, for human and mouse genomes, using [Jaspar](http://jaspar.genereg.net/) motif database.

As of SnpEff version 4.0, these annotations are added automatically, if the database is available for the genome version you are using (files `motif.bin` and `pwms.bin`).
Older versions requires using the `-motif` command line option.

Example of transcription factor binding sites prediction predictions:
```
$ java -Xmx8g -jar snpEff.jar -v GRCh37.75 examples/test.chr22.vcf > test.chr22.ann.vcf
00:00:00.000	Reading configuration file 'snpEff.config'. Genome: 'GRCh37.75'
00:00:00.393	done
00:00:00.394	Reading database for genome version 'GRCh37.75' from file '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/snpEffectPredictor.bin' (this might take a while)
00:00:26.214	done
00:00:26.248	Reading NextProt database from file '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/nextProt.bin'
00:00:27.386	NextProt database: 523361 markers loaded.
00:00:27.387	Adding transcript info to NextProt markers.
00:00:28.072	NextProt database: 706289 markers added.
00:00:28.072	Loading Motifs and PWMs
00:00:28.072		Loading PWMs from : /home/pcingola/snpEff_v4_0/./data/GRCh37.75/pwms.bin
00:00:28.103		Loading Motifs from file '/home/pcingola/snpEff_v4_0/./data/GRCh37.75/motif.bin'
00:00:28.862		Motif database: 284122 markers loaded.
...

# Show some examples (output edited for readability)
$ cat test.chr22.ann.vcf
...
22  18301084  .  G  A  .  .  ANN=A|TF_binding_site_variant|MODIFIER|||Nrsf|MA0138.2|||||||||,A|TF_binding_site_variant|MODIFIER|||Nrsf|MA0138.1|||||||||,...
...
22  23523309  .  C  T  .  .  ANN=T|TF_binding_site_variant|LOW|||Gabp|MA0062.2|||||||||,...
...
22  36629223  .  G  C  .  .  ANN=C|TF_binding_site_variant|LOW|||SP1|MA0079.1|||||||||,...
...
```
So, for instance, the last annotation shown in the example is `TF_binding_site_variant|LOW|||SP1|MA0079.1` corresponding to motif
[MA0079.1](http://jaspar.genereg.net/cgi-bin/jaspar_db.pl?ID=MA0079.1&rm=present&collection=CORE), which you can look up in Jaspar.
