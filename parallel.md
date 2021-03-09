
# VCF format:
	- Parse header
	- Pedigree (cancer)

# Output formatter
	- vcf
		- "normal"
		- gatk
		- oicr
		- classic
		- LOF / NMD
		- Custom fields
	- bed
	- json


### Variants

### VariantsEffects
	- Cancer
		- Needs all variants for comparisson
	- LOF (infered from VariantsEffects)
	- NMD (infered from VariantsEffects)

	- Dependent effects: The impact depeneds on previous effects (e.g. `NON_SYN` vs `SYN`)
		- Interactions: Protein interaction, interaction site, etc.
		- NextProt
			- Highly conserved
			- Phosphorilation site
