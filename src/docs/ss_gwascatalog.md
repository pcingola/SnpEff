# SnpSift GWAS Catalog

Annotate using [GWAS catalog](http://www.genome.gov/gwastudies/).

You need the GWAS catalog file (in TXT format), which can be downloaded [here](https://www.ebi.ac.uk/gwas/api/search/downloads/alternative).
```
$ java -jar SnpSift.jar gwasCat gwascatalog.txt test.vcf | tee test.gwas.vcf
1   1005806 rs3934834   C   T   .   PASS    AF=0.091;GWASCAT=Body_mass_index    
1   2069172 rs425277    C   T   .   PASS    AF=0.400;GWASCAT=Height
1   2069681 rs3753242   C   T   .   PASS    AF=0.211;GWASCAT=Reasoning  
1   2392648 rs2477686   G   C   .   PASS    AF=0.745;GWASCAT=Non_obstructive_azoospermia    
1   2513216 rs734999    C   T   .   PASS    AF=0.547;GWASCAT=Ulcerative_colitis
1   2526746 rs3748816   A   G   .   PASS    AF=0.489;GWASCAT=Celiac_disease
1   3083712 rs2651899   T   C   .   PASS    AF=0.467;GWASCAT=Migraine   
1   3280253 rs6658356   G   A   .   PASS    AF=0.070;GWASCAT=Response_to_statin_therapy
1   4315204 rs966321    G   T   .   PASS    AF=0.522;GWASCAT=Factor_VII
1   5170712 rs7513590   A   G   .   PASS    AF=0.256;GWASCAT=Anthropometric_traits  
1   6279370 rs846111    G   C   .   PASS    AF=0.153;GWASCAT=QT_interval,QT_interval    
1   6631431 rs11587438  C   T   .   PASS    AF=0.906;GWASCAT=White_blood_cell_types
1   7879063 rs2797685   C   T   .   PASS    AF=0.186;GWASCAT=Crohn_s_disease    
1   8021973 rs35675666  G   T   .   PASS    AF=0.093;GWASCAT=Ulcerative_colitis
1   8046672 rs12727642  C   A   .   PASS    AF=0.101;GWASCAT=Celiac_disease
1   8422676 rs2252865   T   C   .   PASS    AF=0.771;GWASCAT=Schizophrenia  
1   8526142 rs4908760   G   A   .   PASS    AF=0.630;GWASCAT=Vitiligo   
```
