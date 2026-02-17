# SnpSift CaseControl

Allows you to count how many samples are in 'case' and 'control' groups.

### Typical usage

This command counts the number of 'homozygous', 'heterozygous' and 'total' variants in a case and control groups and performs some basic pValue calculation using Fisher exact test and Cochran-Armitage test.

Case and Control groups can be defined either by a command line string or a TFAM file (see [PLINK's documentation](http://zzz.bwh.harvard.edu/plink/data.shtml#tr)).

Case/Control command line string containing plus and minus symbols {'+', '-', '0'} where '+' is case, '-' is control and '0' is neutral (ignored).

E.g. We have ten samples, which means ten genotype columns in the VCF file.
The first four are 'cases', the fifth one is 'neutral', and the last five are 'control'.
So the description string would be "++++0-----" (note that the following output has been edited, only counts are shown, no pValues):
```
$ java -jar SnpSift.jar caseControl "++++0-----" cc.vcf
#CHROM  POS    ID  REF  ALT  QUAL  FILTER  INFO                                FORMAT  Sample_01  Sample_02  Sample_03  Sample_04  Sample_05  Sample_06  Sample_07  Sample_08  Sample_09  Sample_10
1       69496  .   G    A    .     PASS    AF=0.01;Cases=1,2,4;Controls=2,2,6  GT      0/1        1/1        1/0        0/0        0/0        0/1        1/1        1/1        1/0        0/0
```
Cases genotypes are samples 1 to 4 : 0/1, 1/1, 1/0 and 0/0. So there are 1 homozygous, 2 heterozygous, and a total of 4 variants (2 * 1 + 1 * 2 = 4).
Thus the annotation is `Cases=1,2,4`

Control genotypes are samples 6 to 10 : 0/1, 1/1, 1/1, 1/0 and 0/0. So there are 2 homozygous, 2 heterozygous, and a total of 6 variants (2 * 2 + 1 * 2 = 6)
Thus the annotation is `Controls=2,2,6`

!!! info
    You can use the `-tfam` command line option to specify a TFAM file.
    Case, control from are read from phenotype field of a TFAM file (6th column).
    Phenotype order in TFAM files do not need to match VCF sample order (sample IDs are used).
    Phenotype column should be coded as {0,1,2} meaning {Missing, Control, Case} respectively.
    See [PLINK's reference](http://zzz.bwh.harvard.edu/plink/data.shtml#tr) for details about TFAM file format.

!!! info
    You can use the `-name nameString` command line option to add name to the INFO tags.

    This can be used to count different case/control groups in the same dataset (e.g. multiple phenotypes)

        $  java -jar SnpSift.jar caseControl -name "_MY_GROUP" "++++0-----" cc.vcf \
        | java -jar SnpSift.jar caseControl -name "_ANOTHER_GROUP" "+-+-+-+-+-" -

        #CHROM  POS    ID  REF  ALT  QUAL  FILTER  INFO                                                                                                         FORMAT  Sample_01  Sample_02  Sample_03  Sample_04  Sample_05  Sample_06  Sample_07  Sample_08  Sample_09  Sample_10
        1       69496  .   G    A    .     PASS    AF=0.01;Cases_MY_GROUP=1,2,4;Controls_MY_GROUP=2,2,6;Cases_ANOTHER_GROUP=1,3,5;Controls_ANOTHER_GROUP=2,1,5  GT      0/1        1/1        1/0        0/0        0/0        0/1        1/1        1/1        1/0        0/0

---

## Command Options

- `<CaseControlString>` : A string of `{'+', '-', '0'}`, one character per sample, identifying two groups: case (`+`), control (`-`), neutral/ignored (`0`).
- `-tfam file.tfam` : Load case/control assignments from a TFAM pedigree file instead of a command-line string. Phenotype column (6th) is coded as `{0, 1, 2}` meaning `{Missing, Control, Case}`.
- `-name <nameStr>` : Append `<nameStr>` as a suffix to all output INFO field names (e.g., `Cases` becomes `Cases_MY_GROUP`). Useful for running multiple case/control analyses on the same dataset.
- `-chi2` : Use Chi-Square approximation instead of Fisher exact test for the dominant, recessive, and allelic models.

## Output INFO fields

For each variant, the following INFO fields are added (with the optional `-name` suffix appended):

- `Cases` : Variant counts in cases, formatted as `Hom,Het,Count` (homozygous ALT, heterozygous, total variant allele count).
- `Controls` : Variant counts in controls, same format as `Cases`.
- `CC_DOM` : p-value from the dominant model (Fisher exact test).
- `CC_REC` : p-value from the recessive model (Fisher exact test).
- `CC_ALL` : p-value from the allelic model (Fisher exact test).
- `CC_GENO` : p-value from the genotypic/codominant model (Chi-Square test).
- `CC_TREND` : p-value from the Cochran-Armitage trend test.

**Note:** The code automatically detects when the REF allele is actually the minor allele and swaps the counts, so the tests always use the minor allele as reference. This prevents inflated p-values.

---

### p-values
SnpSift caseControl calculates the p-value using different models: dominant, recessive, allelic and co-dominant.

!!! info
    When we say we use Fisher exact test, it means that we use the real Fisher exact test calculation, not approximations (like Chi-Square approximations).
    So the p-values should be correct even for low counts on any of the values in the contingency tables.
    Approximations tend to be wrong when any count in a contingency table is below 5.
    You should not see that problem here.

Models:

* Dominant model (`CC_DOM`): A 2 by 2 contingency table is created:

    --       | Alt (A/a + a/a) | Ref (A/A)
    -------- | --------------- | ---------
    Cases    | N11             | N12
    Controls | N21             | N22

    This means that the first column are the number of samples that have ANY non-reference: either 1 (heterozygous) or 2 (homozygous).
    Fisher exact test is used to calculate the p-value.

* Recessive model (`CC_REC`): A 2 by 2 contingency table is created:

    --       | Alt (a/a) | Ref + Het (A/A + A/a)
    -------- | --------- | ---------
    Cases    | N11       | N12
    Controls | N21       | N22

    This means that the first column are the number of samples that have both non-reference chromosomes: homozygous ALT.
    Fisher exact test is used to calculate the p-value.

* Allelic model (`CC_ALL`): A 2 by 2 contingency table is created:

    --       | Variants | References
    -------- | -------- | ----------
    Cases    | N11 | N12
    Controls | N21 | N22

    This means that the first column are the number of non-reference genotypes.
    For instance homozygous reference samples count as 0, heterozygous count as 1 and homozygous non-reference count as 2.
    Fisher exact test is used to calculate the p-value.

* Genotypic / Codominant model (`CC_GENO`): A 2 by 3 contingency table is created:

    --       | A/A | a/A | a/a
    -------- | --- | --- | ---
    Cases    | N11 | N12 | N13
    Controls | N21 | N22 | N23

    This means that the first column are the number of homozygous reference genotypes. The second column is the number of heterozygous.
    And the third column is the number of homozygous non-reference.

    **Chi-Square** distribution with two degrees of freedom is used to calculate the p-value.

* Cochran-Armitage trend model (`CC_TREND`): A 2 by 3 contingency table is created:

    --       | A/A | a/A | a/a
    -------- | --- | --- | ---
    Cases    | N11 | N12 | N13
    Controls | N21 | N22 | N23
    Weight   | 0.0 | 1.0 | 2.0

    This means that the first column are the number of homozygous reference genotypes.
    The second column is the number of heterozygous.
    And the third column is the number of homozygous non-reference.

    **Cochran-Armitage** test is used to calculate the p-value, using the weights shown in the last row.
