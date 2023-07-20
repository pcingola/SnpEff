# SnpSift Concordance

Calculate concordance between two VCF files.

### Typical usage

This is typically used when you want to calculate concordance between a genotyping experiment and a sequencing experiment.

For instance, you sequenced several samples and, as part of a related experiment or just as quality control, you also genotype the same samples using a genotyping array.
Now you want to compare the two experiments.
Ideally there would be no difference between the variants from genotyping and sequencing, but this is hardly the case in real world.

You can use `SnpSift concordance` to measure the differences between the two experiments.

!!! warning
    It is assumed that both VCF files are sorted by chromosome and position.

!!! warning
    Sample names are defined in '#CHROM' line of the header section.
    Concordance is calculated only if sample label matches in both files.

Example:
```
$ java -Xmx1g -jar SnpSift.jar concordance -v genotype.vcf sequencing.vcf > concordance.txt
00:00:00.000	Indexing file 'genotype.vcf'
        index:	MT	460030998
        index:	1	19705
                1 / 2	45170805 / 45174315
                2 / 3	77052081 / 77055591
                3 / 4	104065531 / 104069041
                4 / 5	124098372 / 124101881
                5 / 6	146535292 / 146538802
                6 / 7	184793526 / 184797035
                7 / 8	206156508 / 206160018
                8 / 9	223072816 / 223076326
                9 / 10	242315995 / 242319505
                10 / 11	261053789 / 261057299
                11 / 12	290190553 / 290194063
                12 / 13	312869636 / 312873146
                13 / 14	321966539 / 321970049
                14 / 15	336131317 / 336134827
                15 / 16	350871669 / 350875179
                16 / 17	368900523 / 368904032
                17 / 18	391305860 / 391309369
                18 / 19	398932237 / 398935747
                19 / 20	425219198 / 425222708
                20 / 21	437022008 / 437025517
                21 / 22	442563678 / 442567188
                22 / X	451783418 / 451786927
                X / Y	459553691 / 459557200
                Y / MT	459588787 / 459592296
00:00:01.137	Open VCF file 'genotype.vcf'
00:00:01.141	Open VCF file 'sequencing.vcf'
00:00:01.176	Chromosome: '1'
00:00:02.127		1:1550992	1:1528859
00:00:02.739		1:2426313	1:2389636
...
00:02:13.780		1:248487058	1:248471945
```

### Output

SnpSift's concordance output is written to STDOUT and two files.
For instance the command `java -jar SnpSift.jar concordance -v genotype.vcf sequencing.vcf` will write:

* Concordance by variant: Written to STDOUT
* Concordance by sample: Written to `concordance_genotyping_sequencing.by_sample.txt`
* Summary:  Written to `concordance_genotyping_sequencing.summary.txt`

#### Concordance by variant

This sections is a table showing concordance details for every entry (chr:position) that both files have in common.
E.g.:
```
chr  pos     ref  alt  change_0_0  change_0_1  change_0_2  change_1_0  change_1_1  change_1_2  change_2_0  change_2_1  change_2_2  missing_genotype_genotype  missing_genotype_sequencing
1    865584  G    A        508         0           0           0           2           0           0           0           0           0                                 5
1    865625  G    A        512         0           0           0           1           0           0           0           0           0                                 1
1    865628  G    A        511         0           0           0           2           0           0           0           0           0                                 1
1    865665  G    A        495         0           0           0           4           0           0           0           0           0                                17
1    865694  C    T        428         0           0           0          82           0           0           0           4           0                                 0
```
Each genotype is coded according to the number of ALT variants. i.e.:

* '0/0' (homozygous reference) is coded as '0'
* '0/1' or '1/0' (heterozygous ALT) coded as '1'
* '1/1' (homozygous ALT) is coded as '1'

So the column "change_X_Y" on the table shows how many genotypes coded 'X' in the first VCF, changed to 'Y' in the second VCF.
For example, 'change_0_1' counts the number of "homozygous reference in genotype.vcf" that changed to "heterozygous ALT in sequencing.vcf".
Or 'change_2_2' counts the number of "homozygous ALT" that did not change (in both files they are '2').

A few rules apply:

* If a VCF entry (chr:pos) is present in only one of the files, obviously we cannot calculate concordance, so it is ignored.
* If a VCF entry (chr:pos) has more than one ALT it is ignored. This means that non-biallelic variants are ignored.
* If, for the same chr:pos, REF field is different between the two files, then the entry is ignored.
* If, for the same chr:pos, ALT field is different between the two files, then the entry is ignored.

#### Concordance by sample

This section shows details in the same format as the previous section.
Here, concordance metrics are shown aggregated for each sample.
E.g.:
```
# Totals by sample
sample  change_0_0  change_0_1  change_0_2  change_1_0  change_1_1  change_1_2  change_2_0  change_2_1  change_2_2  missing_genotype_genotype  missing_genotype_sequencing
ID_003  79          0           0           1           8           0           0           0           2           1                          1
ID_004  83          0           0           1           2           0           0           0           5           0                          1
ID_005  80          0           0           0           7           0           0           0           4           1                          0
ID_006  79          0           0           0           5           0           0           0           6           0                          2
ID_008  81          0           0           0           4           0           0           0           4           0                          3
ID_009  80          0           0           0           7           0           0           0           3           0                          2
ID_012  74          0           0           0           10          0           0           0           1           0                          7
ID_013  79          1           0           0           4           0           0           0           5           0                          3
ID_018  84          0           0           0           5           0           0           0           3           0                          0
...
```

#### Summary

Summary file contains overall information and errors.
Here is an example of a summary file:
```
$ cat concordance_genotyping_sequencing.summary.txt
Number of samples:
    929    File genotype.vcf
    583    File sequencing.vcf
    514    Both files
Errors:
        ALT field does not match	19
```
The header indicates that one file ('genotype.vcf') has 929 samples, the other file has 583 and there are 514 matching sample IDs in both files.

At the end of the file, a footer shows the total for each column followed by number of possible errors (or mismatches).
In this case the were 19 ALT fields that did not match between 'genotype.vcf' and 'sequencing.vcf'.
This can happen, for instance, when there are INDELs, which cannot be detected by genotyping arrays.

!!! info
    Summary messages are shown to STDERR if you use verbose mode (command line option `-v`).
