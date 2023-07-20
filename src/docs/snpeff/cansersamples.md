# Cancer samples

Here we describe details about annotating cancer samples.

** Single multi-sample VCF vs. multiple VCF files. **

It is common practice, to have all samples in a single "multi-sample VCF file" (having two or more separate VCF files is highly discouraged).
This is also the "gold standard" in cancer analysis standard, so all samples (both somatic and germline) should be in one VCF file.

SnpEff requires that you follow gold standard practices, thus requires a single multi-sample VCF (it is not possible to run cancer analysis using multiple VCF files).

### Running in cancer analysis mode

Using the `-cancer` command line option, you can compare somatic vs germline samples.

So an example command line would be:

    $ java -Xmx8g -jar snpEff.jar -v -cancer GRCh37.75 cancer.vcf > cancer.ann.vcf

### Representing cancer data

In a typical cancer sequencing experiment, we want to measure and annotate differences between germline (healthy) and somatic (cancer) tissue samples from the same patient.
The complication is that germline is not always the same as the reference genome, so a typical annotation does not work.

For instance, let's assume that at a given genomic position (e.g. chr1:69091), reference genome is 'A', germline is 'C' and somatic is 'G'.
This should be represented in a VCF file as:
```
#CHROM  POS    ID  REF  ALT    QUAL  FILTER  INFO  FORMAT  Patient_01_Germline  Patient_01_Somatic
1       69091  .   A    C,G    .     PASS    AC=1  GT      1/0                  2/1
```

!!! warning
    Some people tend to represent this by changing REF base 'A' using germline 'C'.
    This is a mistake, REF must always represent the reference genome, not one of your samples.

Under normal conditions, SnpEff would provide the effects of changes "A -&gt; C" and "A -&gt; G".
But in case of cancer samples, we are actually interested in the difference between somatic and germline, so we'd like to calculate the effect of a "C -&gt; G" mutation.
Calculating this effect is not trivial, since we have to build a new "reference" by calculating the effect of the first mutation ("A -&gt; C") and then calculate the effect of the second one ("C -&gt; G") on our "new reference".

!!! info
    In order to activate cancer analysis, you must use `-cancer` command line option.

### Defining cancer samples

As we already mentioned, cancer data is represented in a VCF file using multiple ALTs (REF field always is reference genome).
In order to specify which samples are somatic and which ones are germline, there are two options:

* Use a TXT file using `-cancerSamples` command line option.
* Use the PEDIGREE meta information in your VCF file's header. This is the default, but some people might find hard to edit / change information in VCF file's headers.

!!! warning
    If you do not provide either `PEDIGREE` meta information or a TXT samples file, SnpEff will not know which somatic samples derive from which germline samples.
    Thus it will be unable to perform cancer effect analysis.

**TXT file**

This is quite easy.
All you have to do is to create a tab-separated TXT file having two columns: the first column has the germline sample names and the second column has the somatic sample names.
Make sure that sample names match exactly the ones in the VCF file.

E.g.: Create a TXT file named 'samples_cancer.txt'
```
Patient_01_Germline	Patient_01_Somatic
Patient_02_Germline	Patient_02_Somatic
Patient_03_Germline	Patient_03_Somatic
Patient_04_Germline	Patient_04_Somatic
```
Then you have to specify this TXT file when invoking SnpEff, using the `-cancerSamples` command line option.

E.g. In our example, the file name is 'samples_cancer.txt', so the command line would look like this:
```
$ cat examples/samples_cancer_one.txt
Patient_01_Germline    Patient_01_Somatic

$ java -Xmx8g -jar snpEff.jar -v \
                -cancer \
                -cancerSamples examples/samples_cancer_one.txt \
                GRCh37.75 \
                examples/cancer.vcf \
                > cancer.ann.vcf
```
**VCF header**

This is the default method and the main advantage is that you don't have to carry information on a separate TXT file (all the information is within your VCF file).
You have to add the `PEDIGREE` header with the appropriate information to your VCF file.
Obviously this requires you to edit you VCF file's header.

!!! warning
    How to edit VCF headers is beyond the scope of this manual (we recommend using `vcf-annotate` from VCFtools). But if you find adding PEDIGREE information to your VCF file difficult, just use the TXT file method described in the previous sub-section.

E.g.: Pedigree information in a VCF file would look like this:
```
$ cat examples/cancer_pedigree.vcf
##PEDIGREE=<Derived=Patient_01_Somatic,Original=Patient_01_Germline>
#CHROM  POS ID  REF ALT QUAL    FILTER  INFO        FORMAT  Patient_01_Germline Patient_01_Somatic
1   69091   .   A   C,G .       PASS    AF=0.1122   GT      1/0                 2/1
1   69849   .   G   A,C .       PASS    AF=0.1122   GT      1/0                 2/1
1   69511   .   A   C,G .       PASS    AF=0.3580   GT      1/1                 2/2


$ java -Xmx8g -jar snpEff.jar -v -cancer GRCh37.75 examples/cancer_pedigree.vcf > examples/cancer_pedigree.ann.vcf
```
Here we say that the sample called `Patient_01_Somatic` is derived from the sample called `Patient_01_Germline`.
In this context, this means that cancer sample is derived from the healthy tissue.

### Interpreting Cancer annotations

Interpretation of `ANN` field cancer sample relies on 'Allele' sub-field.
Just as a reminder, `ANN` field has the following format:

```
ANN = Allele | Annotation | Annotation_Impact | Gene_Name | Gene_ID | Feature_Type | Feature_ID | Transcript_BioType | Rank | HGVS.c | HGVS.p | cDNA.pos / cDNA.length | CDS.pos / CDS.length | AA.pos / AA.length | Distance | ERRORS_WARNINGS_INFO
```

The `Allele` field tells you which effect relates to which genotype.
More importantly, genotype difference between Somatic and Germline.

Example: when there are multiple ALTs (e.g. REF='A' ALT='C,G') and the genotype field says:

* Allele = "C": it means is the effect related to the first ALT ('C')
* Allele = "G" if it's the effect related to the second ALT ('G')
* Allele = "G-C" means that this is the effect of having the second ALT as variant while using the first ALT as reference ("C -&gt; G").
It is important that you understand the meaning of the last one, because you'll use it often for your cancer analysis.

Example: Sample output for the previously mentioned VCF example would be (the output has been edited for readability reasons)

For the first line we get (edited for readability):
```
$ java -Xmx8g -jar snpEff.jar -v -cancer -cancerSamples examples/samples_cancer_one.txt GRCh37.75 examples/cancer.vcf > examples/cancer.eff.vcf

1   69091   .   A   C,G .   PASS    AF=0.1122;
                  ANN=G|start_lost|HIGH|OR4F5|ENSG00000186092|transcript|ENST00000335137|protein_coding|1/1|c.1A>G|p.Met1?|1/918|1/918|1/305||
                 ,G-C|start_lost|HIGH|OR4F5|ENSG00000186092|transcript|ENST00000335137|protein_coding|1/1|c.1A>G|p.Leu1?|1/918|1/918|1/305||
                 ,C|initiator_codon_variant|LOW|OR4F5|ENSG00000186092|transcript|ENST00000335137|protein_coding|1/1|c.1A>C|p.Met1?|1/918|1/918|1/305||
               GT  1/0 2/1
```
What does it mean:

1. In this case, we have two ALTs = 'C' and 'G'.
2. Germline sample is heterozygous 'C/A' (GT = '1/0')
3. Somatic tissue is heterozygous 'G/C' (GT = '2/1')
4. Change A -&gt; C and A -&gt; G are always calculated by SnpEff (this is the "default mode").

    * A -&gt; C produces this effect:

            C|initiator_codon_variant|LOW|OR4F5|ENSG00000186092|transcript|ENST00000335137|protein_coding|1/1|c.1A>C|p.Met1?|1/918|1/918|1/305||

         Note that the last field (genotype field) is 'C' indicating this is produced by the first ALT.

    * A -&gt; G produces this effect:

            G|start_lost|HIGH|OR4F5|ENSG00000186092|transcript|ENST00000335137|protein_coding|1/1|c.1A>G|p.Met1?|1/918|1/918|1/305||

         Note that the last field (genotype field) is 'G' indicating this is produced by the second ALT.

5. Finally, this is what you were expecting for, the cancer comparisons. Since both germline and somatic are heterozygous (GT are '1/0' and '2/1'), there are 4 possible comparisons to make:
    * G vs C : This is the Somatic vs Germline we are interested in. SnpEff reports this one
    * G vs A : This compares ALT to REF, so it was already reported in "default mode". SnpEff doesn't report this one again.
    * C vs C : This is not a variant, since both og them ar '1'. SnpEff skips this one.
    * C vs A : This compares ALT to REF, so it was already reported in "default mode". SnpEff doesn't report this one again.

    I know is confusing, but the bottom line is that only the first comparison one makes sense, and is the one SnpEff reports.
    So 'C -&gt; G' produces the following effect:

          G-C|start_lost|HIGH|OR4F5|ENSG00000186092|transcript|ENST00000335137|protein_coding|1/1|c.1A>G|p.Leu1?|1/918|1/918|1/305||

    !!! warning
        Notice the genotype field is "G-C" meaning the we produce a new reference on the fly using ALT 1 ('C') and then used ALT 2 ('G') as the variant. So we compare 'G' (ALT) to 'C' (REF).

#### Cancer annotations using 'EFF' field:

Interpretation of `EFF` field cancer sample relies on 'Genotype' sub-field.
Just as a reminder, `EFF` field has the following format:
```
EFF = Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_Change| Amino_Acid_Length | Gene_Name | Transcript_BioType | Gene_Coding | Transcript_ID | Exon_Rank  | Genotype_Number [ | ERRORS | WARNINGS ] )
```

For the previous example, we get (edited for readability):
```
$ java -Xmx8g -jar snpEff.jar -v -classic -cancer -cancerSamples examples/samples_cancer_one.txt GRCh37.75 examples/cancer.vcf > examples/cancer.eff.vcf

1	69091	.	A	C,G	.	PASS	AC=1;
                     EFF=START_LOST(HIGH|MISSENSE|Atg/Gtg|M1V|305|OR4F5|protein_coding|CODING|ENST00000335137|1|G)
                        ,START_LOST(HIGH|MISSENSE|Ctg/Gtg|L1V|305|OR4F5|protein_coding|CODING|ENST00000335137|1|G-C)
                        ,NON_SYNONYMOUS_START(LOW|MISSENSE|Atg/Ctg|M1L|305|OR4F5|protein_coding|CODING|ENST00000335137|1|C)
```

The `GenotypeNum` field tells you which effect relates to which genotype.
More importantly, genotype difference between Somatic and Germline.

Example: when there are multiple ALTs (e.g. REF='A' ALT='C,G') and the genotype field says:

* GenotypeNum = "1": it means is the effect related to the first ALT ('C')
* GenotypeNum = "2" if it's the effect related to the second ALT ('G')
* GenotypeNum = "2-1" means that this is the effect of having the second ALT as variant while using the first ALT as reference ("C -&gt; G").
