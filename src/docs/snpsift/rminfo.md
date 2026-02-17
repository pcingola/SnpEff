# SnpSift RmInfo

Remove INFO fields from a VCF file.

This is typically used before re-annotating a VCF file: since SnpEff and SnpSift add annotations without replacing existing ones, you should first remove old annotations and then re-annotate. This avoids ambiguity about whether a value comes from old or new annotations.

### Usage

```
java -jar SnpSift.jar rmInfo [options] file.vcf infoField_1 [infoField_2 ... infoField_N] > output.vcf
Options:
    -id : Also remove the ID column (set to empty).
```

The first positional argument is the VCF file. All subsequent positional arguments are names of INFO fields to remove. Multiple fields can be removed in a single pass.

!!! info
    The `##INFO` header definitions for removed fields are NOT removed from the header. Only the actual values in data lines are stripped.

### Example

Remove the `EFF` and `ANN` fields before re-annotating:
```
$ java -jar SnpSift.jar rmInfo input.vcf EFF ANN > input.clean.vcf

$ java -jar SnpSift.jar rmInfo input.vcf EFF
#CHROM  POS     ID      REF  ALT  QUAL  FILTER  INFO
1       734462  1032    G    A    .     s50     AC=348
```
