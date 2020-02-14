## Error chromosome not found

The error is due to a difference between the chromosome names in input VCF file and the chromosome names in SnpEff's database.

Chromosome does not exits in reference database.
Typically this means that there is a mismatch between the chromosome names in your input file and the chromosome names used in the reference genome to build SnpEff's database.

!!! warning
    This error could be caused because you are trying to annotate using a reference genome that is different than the one you used for sequence alignment.
    Obviously doing this makes no sense and the annotation information you'll get will be garbage.
    That's why SnpEff shows you an error message.

**Possible solution:** Sometime SnpEff database matches the reference genome for your organism, and it's just that the chromosome names are changed.
In this case, you can fix the error by changing the chromome names in your input file.

!!! info
    You can see the chromosome names used by SnpEff's database by using `-v` (verbose) option.

SnpEff will show a line like this one:

    # Chromosomes names [sizes]  : '1' [249250621] '2' [243199373] ...

!!! info
    You can see the chromosome names in your input VCF file using a command like this one:

        cat input.vcf | grep -v "^#" | cut -f 1 | uniq

Once you know the names of the input file and the name used by SnpEff's database, you can adjust the chromosome name using a simple `sed` command.
For example, if you input file's chromosome name is 'INPUT_CHR_NAME' and the name in SnpEff's database is 'SNPEFF_CHR_NAME', you could use the following command:

    cat input.vcf | sed "s/^INPUT_CHR_NAME/SNPEFF_CHR_NAME/" > input_updated_chr.vcf



