# SnpSift Private

Annotate if a variant is private to a family.

A `Private=Family_ID` is added to a variant's INFO field, if the variant is only found in one family.
A TFAM file (see PLINK's documentation) specifies a mapping from sample IDs to family IDs.

E.g.:

    $ java -jar SnpSift.jar private pheotypes.tfam imp.ann.vcf > imp.ann.private.vcf

An annotated variant may look like this:

    1   1005806 rs3934834   C   T   .   PASS    AF=0.091;..;Private=Family_47
This indicates that the variant is only found in members of `Family_47`, according to the definitions in `pheotypes.tfam`.
