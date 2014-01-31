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
Filter, ${seqChangeFilter}
Number_of_variants_filtered_out, ${countVariantsFilteredOut}
Number_of_not_variants , ${seqStats.countNonVariants}
Number_of_variants_processed , ${seqStats.count}
Number_of_known_variants <br>(i.e. non-empty ID) , ${seqStats.countNonEmptyId}, ${ ( 100 * seqStats.getKnownRatio() ) }% 
Number_of_effects , ${countEffects} 
Genome_total_length ,${seqStats.genomeLen} 
Genome_effective_length ,${seqStats.genomeLenEffective} 
Change_rate , ${seqStats.rateOfChange} 

# Change rate by chromosome

Chromosome , Length , Changes , Change_rate 
<#list seqStats.chromosomeNamesEffective as chr>${chr} , ${seqStats.getChromosomeLength(chr)} , ${seqStats.getCountByChromosome(chr)} , ${seqStats.getRateOfChangeByChromosome(chr)}
</#list>

# Changes by type

Type, Total, Homo, Hetero
<#list seqStats.changeType as chType>
${chType} , ${seqStats.countByChangeType.get(chType)} , ${seqStats.countByChangeTypeHom.get(chType)} , ${seqStats.countByChangeTypeHet.get(chType)}
</#list>

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

<#if seqStats.qualityStats.validData><@intstatsTable seqStats.qualityStats /></#if>

# Coverage

<#if seqStats.coverageStats.validData><@intstatsTable seqStats.coverageStats /></#if>

# InDel lengths

<#if seqStats.indelLen.validData><@intstatsTable seqStats.indelLen /></#if>

# Base changes

base <#list seqStats.bases as newBase > , ${newBase} </#list>
<#list seqStats.bases as oldBase > ${oldBase} <#list seqStats.bases as newBase > , ${seqStats.getBasesChangesCount(oldBase, newBase)} </#list>
</#list>

# Ts/Tv summary

Transitions , ${seqStats.transitions} 
Transversions , ${seqStats.transversions} 
Ts_Tv_ratio , ${seqStats.tsTvRatio}

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

<@intstatsTable vcfStats.alleleFrequencyStats.count />

# Allele frequency : Known variants

<@intstatsTable vcfStats.alleleFrequencyStatsKnown.count />
</#if>

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
<#list seqStats.chromosomeNamesEffective as chr><#assign chrStats = seqStats.getChrPosStats(chr)>${chrStats}
</#list>
</#if>
