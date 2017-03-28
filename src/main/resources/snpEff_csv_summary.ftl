<#-- snpEff summary statistics CSV format -->

<#macro countByType counter>
Type , Count , Percent 
<#list counter.typeList as type>${type} , ${counter.get(type)} , ${ ( 100 * counter.percent(type) ) }% 
</#list>
</#macro>

<#macro intstatsTable intstats>
Values , ${intstats.toStringValues()}
Count , ${intstats.toStringCounts()}
</#macro>

# Summary table 

Name , Value
Genome , ${genomeVersion} 
Date , ${date}
SnpEff_version , ${version}
Command_line_arguments , ${args}
Warnings , ${changeStats.countWarnings} 
Number_of_lines_in_input_file, ${countInputLines}
Number_of_variants_before_filter, ${countVariants}
Number_of_not_variants , ${variantStats.countNonVariants}
Number_of_variants_processed , ${variantStats.count}
Number_of_known_variants (i.e. non-empty ID) , ${variantStats.countNonEmptyId}, ${ ( 100 * variantStats.getKnownRatio() ) }% 
Number_of_effects , ${countEffects} 
Genome_total_length ,${variantStats.genomeLen} 
Genome_effective_length ,${variantStats.genomeLenEffective} 
Change_rate , ${variantStats.rateOfChange} 

# Change rate by chromosome

Chromosome , Length , Changes , Change_rate 
<#list variantStats.chromosomeNamesEffective as chr>${chr} , ${variantStats.getChromosomeLength(chr)} , ${variantStats.getCountByChromosome(chr)} , ${variantStats.getRateOfChangeByChromosome(chr)}
</#list>

# Variantss by type

<@countByType variantStats.countByChangeType />

# Effects by impact

<@countByType changeStats.countByImpact />

# Effects by functional class

<@countByType changeStats.countByFunctionalClass />

Missense_Silent_ratio, ${changeStats.silentRatio}

# Count by effects

<@countByType changeStats.countByEffect /> 

# Count by genomic region

<@countByType changeStats.countByGeneRegion /> 

# Quality 

<#if vcfStats.qualityStats.validData><@intstatsTable vcfStats.qualityStats /></#if>

# InDel lengths

<#if variantStats.indelLen.validData><@intstatsTable variantStats.indelLen /></#if>

# Base changes

base <#list variantStats.bases as newBase > , ${newBase} </#list>
<#list variantStats.bases as oldBase > ${oldBase} <#list variantStats.bases as newBase > , ${variantStats.getBasesChangesCount(oldBase, newBase)} </#list>
</#list>

# Ts/Tv summary

Transitions , ${vcfStats.tsTvStats.transitions}
Transversions , ${vcfStats.tsTvStats.transversions}
Ts_Tv_ratio , ${vcfStats.tsTvStats.tsTvRatio}

<#assign tstv=vcfStats.hasData()>
<#if tstv>
# Ts/Tv : All variants

${vcfStats.tsTvStats}

# Ts/Tv : Known variants

${vcfStats.tsTvStatsKnown}
</#if>

# Allele frequency

<#assign af=vcfStats.hasData()>
<#if af>
# Allele frequency : All variants

<@intstatsTable vcfStats.genotypeStats.alleleFrequency />

# Allele Count

<@intstatsTable vcfStats.genotypeStats.alleleCount />
</#if>

# Hom/Het table

${vcfStats.genotypeStats.homHetTable}

# Codon change table
	
codons <#list changeStats.codonList as newCodon> , ${newCodon} </#list>
<#list changeStats.codonList as oldCodon> ${oldCodon}  <#list changeStats.codonList as newCodon> <#assign count = changeStats.getCodonChangeCount(oldCodon, newCodon)>, ${count} </#list>
</#list>

# Amino acid change table

aa <#list changeStats.aaList as newAa> , ${newAa} </#list>
<#list changeStats.aaList as oldAa> ${oldAa} <#list changeStats.aaList as newAa><#assign count = changeStats.getAaChangeCount(oldAa, newAa)>, ${count} </#list>
</#list>

# Chromosome change table

<#if chromoPlots>
<#list variantStats.chromosomeNamesEffective as chr><#assign chrStats = variantStats.getChrPosStats(chr)>${chrStats}
</#list>
</#if>
