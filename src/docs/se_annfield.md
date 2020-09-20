# Important changes: 'ANN' field

This SnpEff version implements the [**new VCF annotation standard 'ANN' field.**](adds/VCFannotationformat_v1.0.pdf)

This new format specification has been created by the developers of the most widely used variant annotation programs (SnpEff, ANNOVAR and ENSEMBL's VEP)
and attempts to:

* provide a common framework for variant annotation,
* make pipeline development easier,
* facilitate benchmarking, and
* improve some known problems in variant annotations.

Obviously this new 'ANN' field introduces changes respect to the previous 'EFF' field that break compatibility with previous SnpEff versions.

In order to use the old 'EFF' field, you can use the `-formatEff` command line option.