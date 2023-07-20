# SnpSift: Frequently Asked Questions

# Corrupted database VCF files: ClinVar

Some VCF files used as annotation databases can be non-compliant.

Most notably, some ClinVar versions have illegal VCF values, which will make downstream analysis tools, such as `SnpSift` to report the errors.

For example, if you look into the file:

```
$ curl -s ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/vcf_GRCh37/clinvar.vcf.gz | gunzip -c | grep "&base" | head -n 1

13	32890543	125955	G	A	.	.	ALLELEID=131493;CLNDISDB=MedGen:C2675520,OMIM:612555;CLNDN=Breast-ovarian_cancer,_familial_2;CLNHGVS=NC_000013.10:g.32890543G>A;CLNREVSTAT=no_assertion_criteria_provided;CLNSIG=Uncertain_significance;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=Breast_Cancer_Information_Core__(BRCA2):190-16&base_change=G_to_A;GENEINFO=BRCA2:675;MC=SO:0001627|intron_variant;ORIGIN=1;RS=276174799
```

As you can see, the "CLNVI" is:

```
CLNVI=Breast_Cancer_Information_Core__(BRCA2):190-16&base_change=G_to_A
```

This means that the CLNVI contains an illegal `'='` character.
The VCF specification clearly states that the equal sign is not allowed:

```
  Reference: https://samtools.github.io/hts-specs/VCFv4.3.pdf
  Section 1.2: "Character encoding, non-printable characters and characters with special meaning"

    Characters with special meaning (such as field delimiters ’;’ in INFO or ’:’ FORMAT fields) must be represented
    using the capitalized percent encoding:
      %3A : (colon)
      %3B ; (semicolon)
      %3D = (equal sign)
      ...
```

Furthermore, section 1.6.1.8 specifies:

```
  INFO - additional information: (String, no semi-colons or equals-signs permitted; commas are permitted only
  as delimiters for lists of values; characters with special meaning can be encoded using the percent encoding, see
  Section 1.2; space characters are allowed) 
```

#### Finding all ClinVar problems

An easy way to find many of the problems in the VCF file is to use the `SnpSift checkVcf` command:

```
$ java -jar SnpSift.jar vcfCheck clinvar.vcf.gz 2>&1 | head
...WARNING: Malformed VCF entryfile '/home/pcingola/Downloads/clinvar.vcf.gz', line 3655:
	Entry  : 1	25717365	17708	C	C	.	.	ALLELEID=32747;CLNDISDB=.;CLNDN=RH_E/e_POLYMORPHISM;CLNHGVS=NC_000001.10:g.25717365C=;CLNREVSTAT=no_assertion_criteria_provided;CLNSIG=Benign;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=OMIM_Allelic_Variant:111700.0001;GENEINFO=RHCE:6006;MC=SO:0001627|intron_variant,SO:0001819|synonymous_variant;ORIGIN=1;RS=609320
	Errors :
		INFO filed 'CLNHGVS' has an invalid value 'NC_000001.10:g.25717365C=' (no spaces, tabs, '=' or ';' are allowed)

WARNING: Malformed VCF entryfile '/home/pcingola/Downloads/clinvar.vcf.gz', line 3657:
	Entry  : 1	25735202	242743	G	G	.	.	ALLELEID=38411;CLNHGVS=NC_000001.10:g.25735202G=;CLNREVSTAT=no_interpretation_for_the_single_variant;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=OMIM_Allelic_Variant:111700.0002;GENEINFO=RHCE:6006;MC=SO:0001819|synonymous_variant;ORIGIN=1;RS=676785;SSR=1;CLNDISDBINCL=.;CLNDNINCL=RH_C/c_POLYMORPHISM;CLNSIGINCL=17709:Benign
	Errors :
		INFO filed 'CLNHGVS' has an invalid value 'NC_000001.10:g.25735202G=' (no spaces, tabs, '=' or ';' are allowed)
```

OK, it looks like there are quite a few problems, let's count them:

```
$ java -jar SnpSift.jar vcfCheck ~/Downloads/clinvar.vcf.gz 2>&1 | grep WARN | wc -l
1793
```

Well, there seems to be 1793 lines VCF with some sort of problem.
Let's see how to fix them.

### Fixing ClinVar's VCF database

So, you need to fix ClinVar by either: 

1. Remove the offending fields from the VCF file
1. Fix the character coding for the offending values

#### Option 1: Remove the offending fields

This is the easiest way to fix ClinVar's VCF file.
First, let's find the corrupted fields:
```
$ java -jar SnpSift.jar vcfCheck clinvar.vcf.gz 2>&1 | grep "INFO field" | cut -f 2 -d "'" | sort | uniq -c
    212 CLNHGVS
   1583 CLNVI
```
OK, there are 212 lines with corrupted `CLNHGVS` fields and 1583 lines with corrupted `CLNVI` fields.
Let's create a new database without those fields

```
$ java -jar SnpSift.jar rmInfo clinvar.vcf.gz CLNHGVS CLNVI > clinvar.fixed_1.vcf
00:00:00	Reading STDIN
00:00:03	Done

# Let's also compress and index the new file so we can use it as a database
$ bgzip clinvar.fixed_1.vcf 
$ tabix clinvar.fixed_1.vcf.gz 
```

Now we can re-check the new file to make sure it's OK.
```
$ java -jar SnpSift.jar vcfCheck clinvar.fixed_1.vcf.gz 
....................................................................................................
100000	....................................................................................................
200000	..................................................................................................
```
Everything seems OK.

#### Option 2: Fix the encoding

Here we need to fix the encoding of the fields.
We know (see previous section) that the problematic fields are `CLNVI` and `CLNHGVS`, so what exactly are the problems?

```
$ java -jar SnpSift.jar vcfCheck clinvar.vcf.gz 2>&1 | grep "INFO field" | grep CLNVI | head
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):190-16&base_change=G_to_A' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):190-12&base_change=del_TCT' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):190-5&base_change=del_T' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):190-7&base_change=T_to_C' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):195&base_change=T_to_C' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):203&base_change=G_to_A' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):203&base_change=G_to_C' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):203&base_change=G_to_T' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):214&base_change=A_to_C' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNVI' has an invalid value 'Breast_Cancer_Information_Core__(BRCA2):215&base_change=T_to_C' (no spaces, tabs, '=' or ';' are allowed)
```
OK, this one seems easy: All we need to do is change `&base_change=` to `&base_change%3D`
This can be done with a simple `sed` command:

```
$ zcat clinvar.vcf.gz | sed 's/\&base_change=/\&base_change%3D/g'
```

How about the other field?
```
$ java -jar SnpSift.jar vcfCheck clinvar.vcf.gz 2>&1 | grep "INFO field" | grep CLNHGVS | head
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.25717365C=' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.25735202G=' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.25735306T=' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.25735331G=' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.94578548T=' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.98348885G=' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.100672060T=' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.114377568A=' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.161599571T=' (no spaces, tabs, '=' or ';' are allowed)
		INFO field 'CLNHGVS' has an invalid value 'NC_000001.10:g.161599643T=' (no spaces, tabs, '=' or ';' are allowed)
```

This is essentially the same, but we need four `sed` commands (one for each base):
```
$ zcat clinvar.vcf.gz \
    | sed 's/A=;/A%3D;/' \
    | sed 's/C=;/C%3D;/' \
    | sed 's/G=;/G%3D;/' \
    | sed 's/T=;/T%3D;/'
```

**The fix:**

Now, let's put the two previously explained fixes together:
```
$ zcat clinvar.vcf.gz \
    | sed 's/\&base_change=/\&base_change%3D/g' \
    | sed 's/A=;/A%3D;/' \
    | sed 's/C=;/C%3D;/' \
    | sed 's/G=;/G%3D;/' \
    | sed 's/T=;/T%3D;/' \
    > clinvar.fixed.vcf

# Let's also compress and index the new file so we can use it as a database
$ bgzip clinvar.fixed.vcf 
$ tabix clinvar.fixed.vcf.gz 
```

We re-check the new
```
$ java -jar SnpSift.jar vcfCheck clinvar.fixed.vcf.gz 
....................................................................................................
100000	....................................................................................................
200000	..................................................................................................
```
OK, we are done.
