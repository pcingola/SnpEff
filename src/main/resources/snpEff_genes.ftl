# The following table is formatted as tab separated values. <#assign geneRegionTab=changeStats.geneCountByRegionTable>	<#assign geneEffTab=changeStats.geneCountByEffectTable>	<#assign geneImpTab=changeStats.geneCountByImpactTable>	<#assign keyList=geneRegionTab.keyList>	<#assign regions=geneRegionTab.typeList> <#assign effects=geneEffTab.typeList>  <#assign impacts=geneImpTab.typeList>
#GeneName	GeneId	TranscriptId	BioType<#list impacts as imp>	variants_impact_${imp}</#list><#list effects as eff>	variants_effect_${eff}</#list>
<#list keyList as key>${key}	${geneRegionTab.getBioType(key)}<#list impacts as imp>	${geneImpTab.getCounter(imp).get(key)}</#list><#list effects as eff>	${geneEffTab.getCounter(eff).get(key)}</#list>
</#list>
