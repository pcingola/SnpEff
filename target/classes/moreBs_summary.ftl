<#-- MoreBs summary statistics -->

<!--==========================================================================
	FTL macros 
	========================================================================== -->

<#macro counterTable counter>
<table border=0>
	<thead>
		<tr> 
			<th><b> Type (alphabetical order)  </b></th>
			<th> &nbsp; </th>
			<th> Analyzed </th>
			<th> Methylated </th>
			<th> Methylated % </th>
		</tr>
	</thead>
	<#list counter.typeList as type>
		<tr> 
			<td> <b> ${type} </b> </td> 
			<th> &nbsp; </th>
			<td class="numeric"> ${counter.getBasesByTypeTotal(type)} </td> 
			<td class="numeric" bgcolor="${counter.getColorMethHtml(type)}"> ${counter.getBasesByTypeMeth(type)} </td> 
			<td class="numeric" bgcolor="${counter.getColorMethProbHtml(type)}"> ${ ( 100 * counter.getProbBasesByType(type) )?string("0.###") }% </td>
		</tr>
	</#list>
</table><br>
</#macro>

<#macro counterTablePvalue counter>
<table border=0>
	<thead>
		<tr> 
			<th><b> Type (alphabetical order)  </b></th>
			<th> &nbsp; </th>
			<th> Analyzed </th>
			<th> Methylated </th>
			<th> Methylated % </th>
			<th> p-value </th>
		</tr>
	</thead>
	<#list counter.typeList as type>
		<tr> 
			<td> <b> ${type} </b> </td> 
			<th> &nbsp; </th>
			<td class="numeric"> ${counter.getBasesByTypeTotal(type)} </td> 
			<td class="numeric" bgcolor="${counter.getColorMethHtml(type)}"> ${counter.getBasesByTypeMeth(type)} </td> 
			<td class="numeric" bgcolor="${counter.getColorMethProbHtml(type)}"> ${ ( 100 * counter.getProbBasesByType(type) )?string("0.###") }% </td>
			<td class="numeric"> ${counter.getPvalue(type)?string("0.####E0")} </td>
		</tr>
	</#list>
</table><br>
</#macro>

<#macro intstatsTable intstats>
	<table class="histo">
		<tr> <th width=15%>Min</th><td>${intstats.min}</td> </tr>
		<tr> <th>Max</th><td>${intstats.max}</td> </tr>
		<tr> <th>Mean</th><td>${intstats.mean}</td> </tr>
		<tr> <th>Median</th><td>${intstats.median}</td> </tr>
		<tr> <th>Standard deviation</th><td>${intstats.std}</td> </tr>
		<tr> <th>Vaues</th><td>${intstats.toStringValues()}</td> </tr>
		<tr> <th>Count</th><td>${intstats.toStringCounts()}</td> </tr>
		</tr>
	</table>
</#macro>


<!--==========================================================================
	CSS styles 
	========================================================================== -->


<style type="text/css">

body {
	background-color: #dddddd;
}

/* Table styles. */

table {
  border-color: #000;
  border-spacing: 0px;
  border-style: solid;
  border-width: 1px;
  cell-spacing: 0px;
}

.noBorder {
  border-width: 0px;
}

td, th {
  font-family: Arial, Helvetica, sans-serif;
  font-size: 10pt;
  padding: 2px 0.5em;
  white-space: nowrap;
}

td.numeric {
  text-align: right;
}

th {
  background-color: #c0c0c0;
}

th.mainHeader {
  background-color: #808080;
  color: #ffffff;
  text-align: left;
}

th a {
  color: #000080;
  text-decoration: none;
}

th a:visited {
  color: #000080;
}

th a:active, th a:hover {
  color: #800000;
  text-decoration: underline;
}

.toc {
	border: 1px solid #aaa;
	background-color: #eeeeee;
	padding: 5px;
	font-size: 95%;
}

.note {
	border: 1px solid #aaa;
	background-color: #eeeeee;
	padding: 5px;
	font-size: 90%;
	text-align: left;
}

.main {
	margin-top: 15px;
	width: 1000px;
	text-align: left;
	background: #ffffff;
	border: 3px solid #777777;
}

.histo {
	table-layout:fixed;
	width:100%;
	border:1px solid #aaa;
	word-wrap:break-word;
}

</style>

<!--==========================================================================
	Index 
	========================================================================== -->

<center>
<div class="main">

<center> <h3> MoreBs: Bisulphite sequencing analysis </h3> </center>

<div style="margin-left: .5em">
<table class="toc"><tr><td>
	<center><b>Contents</b></center>
	<a href="#summary">Summary</a><br>
	<#if methStats.qualityStats.validData>
	<a href="#quality">Quality histogram and data</a><br>
	</#if>
	<a href="#coverageTable">Coverage table</a><br>
	<#if methStats.coverage.validData>
	<a href="#coverage">Coverage histogram and data</a><br>
	</#if>
	<a href="#symm">Methylation context and symmetry</a><br>
	<a href="#type">Methylation by genetic region</a><br>	
	<a href="#codon">Methylation by codon</a><br>	
	<a href="#codonAa">Methylation by Amino acid</a><br>	
	<a href="#geneType">Methylation by gene type</a><br>
	<a href="#strand">Methylation by Strand</a><br>
	<a href="#cpgbias">Methylation vs CpG bias</a><br>
	<a href="#typePlots">Plots: Methylation vs position (genetic region)</a><br>
	<a href="#chromo">Methylation chromosome</a><br>
	<#if chromoPlots>
	<a href="#chromoPlots">Methylation chromosome plots</a><br>
	</#if>	
	<a href="#genePerc">Gene methylation percentage histogram</a><br>
	<#if calcPvalues>
	<a href="#pvaluegene">P-value by Genomic region</a><br>
	</#if>
	<a href="#errors">Errors and Warnings (details)</a><br>
</tr></td></table>
</div>


<!--==========================================================================
	Summary table 
	========================================================================== -->

<center>
	<hr>
	<a name="summary">
	<b>Summary</b><p>
	
	<table border=0>
		<tr> 
			<td valign=top> <b> Genome </b> </td>
			<td> ${genomeVersion} </td>
		</tr>
		<tr> 
			<td valign=top> <b> Date </b> </td>
			<td> ${date} </td>
		</tr>
		<tr> 
			<td valign=top> <b> MoreBs version </b> </td>
			<td> <pre>${version}</pre> </td>
		</tr>
		<tr> 
			<td valign=top> <b> Command line arguments </b> </td>
			<td> <pre>${args}</pre> </td>
		</tr>
		<tr> 
			<td valign=top> <b> Errors </b> </td>
			<#assign color="#ffffff">
			<#if methStats.errorCount &gt; 0> <#assign color="#ff0000"> </#if> 
			<td bgcolor="${color}"> ${methStats.errorCount} </td>
		</tr>
		<tr>
			<td valign=top> <b> Number of bases </b> </td>
			<td>
				<table class="noBorder" width=100%>
					<tr><th> Bases analyzed </th><th> Bases methylated </th><th> Methyltated bases % </th></tr> 
					<tr><td class="numeric"> ${methStats.countBases} </td><td class="numeric"> ${methStats.countMethBases} </td><td class="numeric"> ${( 100 * methStats.probBases )?string("0.###")}% </td></tr> 		 
				</table>
			</td>
		</tr>
		<tr>
			<td valign=top> <b> Input filter </b> </td>
			<td> ${filter} </td>
		</tr>
		<tr>
			<td valign=top> <b> Entries filtered out </b> </td>
			<td> ${filterNotPass} </td>
		</tr>
		<tr>
			<td valign=top> <b> Genome total length </b> </td>
			<td> ${methStats.genomeLen} </td>
		</tr>
		<tr>
			<td valign=top> <b> Genome effective length </b> </td>
			<td> ${methStats.genomeLenEffective} </td>
		</tr>
		<tr>
			<td valign=top> <b> Methylated genes </b> </td>
			<td> ${methStats.geneMethylationTable.countMethGenes(true)} </td>
		</tr>
		<tr>
			<td valign=top> <b> Unmethylated genes </b> </td>
			<td> ${methStats.geneMethylationTable.countMethGenes(false)} </td>
		</tr>
	</table>
	<p>	
	<img src="${methStats.geneMethPieChart}"><p>
</center>

<!--==========================================================================
	Coverage
	========================================================================== -->

<#if methStats.qualityStats.validData>
<center> 
	<hr>
	<a name="quality"> 
	<center><b> Quality: </b></center><p>
	
	<@intstatsTable methStats.qualityStats />
	<img src="${methStats.qualityStats.toStringPlot("Quality histogram", "Quality", true) }"><br>
	</pre>
</center>
</#if>

<!--==========================================================================
	Quality plots
	========================================================================== -->

<center>
	<hr>
	<a name=coverageTable> 
	<b> Methylation by Coverage </b> <p>  
	<@counterTable methStats.counterCoverage/>

	<#if methStats.coverage.validData>
		<hr> 
		<a name="coverage"> 
		<center><b> Coverage histogram </b></center><p>
		<@intstatsTable methStats.coverage />
		<img src="${methStats.coverage.toStringPlot("Coverage histogram", "Coverage", true) }"><br>
	</#if>
</center>

<!--==========================================================================
	Symmetric methylation 
	========================================================================== -->

<center>
	<hr> 
	<a name="symm">
	<b> Methylation site and symmetry </b> <p>  
	
	<table border=0>
		<thead>
			<tr> 
				<th><b> Context  </b></th>
				<th> Total bases </th>
				<th> Symmetrically methylated bases </th>
				<th> % </th>
			</tr>
		</thead>
		<#list symmStats.contextList as context>
			<tr> 
				<td> <b> ${context} </b> </td> 
				<td class="numeric"> ${symmStats.getTotal(context)} </td> 
				<td class="numeric"> ${symmStats.getSymmetric(context)} </td> 
				<td class="numeric"> ${( 100 * symmStats.getPercent(context) )?string("0.###")}% </td>
			</tr>
		</#list>
	</table><br>
</center> 


<!--==========================================================================
	Methylation by marker
	========================================================================== -->

<center>
	<hr> 
	<a name="type">
	<b> Methylation by genetic region </b> <p>
	<@counterTable methStats.counterMarker/>
	Note: One methylation site may be classified in multiple types <p>
	<img src="${methStats.plotGene}" border=1><p>
</center>

<!--==========================================================================
	Methylation by codon
	========================================================================== -->

<center>
	<hr> 
	<a name="codon">
	<b> Methylation by codon </b> <p>
	<@counterTablePvalue methStats.counterCodon/>
	Note: One methylation site may be classified in multiple types (e.g. multiple transcripts)<p>
</center>

<!--==========================================================================
	Methylation by Amino acid
	========================================================================== -->

<center>
	<hr> 
	<a name="codonAa">
	<b> Methylation by amino acid </b> <p>
	<@counterTablePvalue methStats.counterCodonAa/>
	Note: One methylation site may be classified in multiple types (e.g. multiple transcripts)<p>
</center>

<!--==========================================================================
	Methylation counter tables
	========================================================================== -->

<center>
	<hr> 
	<a name="geneType">
	<b> Methylation by Gene type </b> <p>  
	<@counterTablePvalue methStats.counterGeneType/> 
</center>

<!--==========================================================================
	Methylation by strand
	========================================================================== -->

<center>
	<hr> 
	<a name="strand">
	<b> Methylation by Strand </b> <p>  
	<@counterTablePvalue methStats.counterStrand/>
</center>

<!--==========================================================================
	Methylation vs CpG bias
	========================================================================== -->

<center>
	<hr> 
	<a name="cpgbias">
	<b> Methylation vs CpG bias </b><p>
	
		<div class="note">
		Note: The plots show the number of methylated and unmethylated genes vs CpG bias (exon), where:
			<ul>
				<li> CpG bias for a gene is number of CpG sites in each exon of each transcript divided by the sum of expected CpG sites in each exon. Note than an exon can be counted multiple times if it is included in many transcripts.
				<li> CpG bias in an exon is calculated as the number of CpG sites divided by the expected number of CpG
				<li> Expected number of CpGs is just the exon length divided by 16.
				<li> A gene is considered to be methylated if the number of methylated bases in all exons is more than a given threshold.
			</ul>
		</div>
		
	<img src="${methStats.geneMethylationTable.geneMethCpG.toStringPlot("Methylated genes vs CpG bias histogram", "CpG bias (%)", true)}"> <p>
	<img src="${methStats.geneMethylationTable.geneUnmethCpG.toStringPlot("Unmethylated genes vs CpG bias histogram", "CpG bias (%)", true)}"> <p>
</center>

<!--==========================================================================
	Methylation by marker plots
	========================================================================== -->

<center>
	<hr> 
	<a name="typePlots">
	<b> Plots: Methylation vs position </b> <br> 
	<#list methStats.plotByTypeList as type>
		<#assign plot = methStats.getPlotByType(type)>
		<#if plot != "" >
			<img src="${plot}"><br>
			<#assign text = methStats.getStatsByType(type)>
			<pre>Meth(${type}): ${text}</pre>
		</#if>
	</#list>
</center>

<!--==========================================================================
	Methylation by chromosome
	========================================================================== -->

<center>
	<hr> 
	<a name="chromo">
	<b> Methylation by chromosome </b><p>
	<table border=0>
		<tr>
			<th> Chromosome </th>
			<th> Length </th>
			<th> &nbsp; </th>
			<th> Analyzed bases </th>
			<th> Methylated bases </th>
			<th> Methylated bases % </th>
		</tr> 
		<#list methStats.chromosomeNamesEffective as chr> 
			<tr>
				<td> ${chr} </td>
				<td class="numeric"> ${methStats.getChromosomeLength(chr)} </td>
				<th> &nbsp; </th>
				<td class="numeric"> ${methStats.getByChromosomeTotalBases(chr)} </td>
				<td class="numeric"> ${methStats.getByChromosomeMethBases(chr)} </td>
				<td class="numeric"> ${ ( 100 * methStats.getByChromosomeProbBases(chr) )?string("0.###")}% </td>
			</tr> 
		</#list>
		<tr>
			<th> Total </th>
			<th class="numeric"> ${methStats.genomeLenEffective} </th>
			<th> &nbsp; </th>
			<th class="numeric"> ${methStats.countBases} </th>
			<th class="numeric"> ${methStats.countMethBases} </th>
			<th class="numeric"> ${ ( 100 * methStats.probBases )?string("0.###")}% </th>
		</tr> 
	</table> 
	
	<#if chromoPlots>
		<hr> 
		<a name="chromoPlot">
		<b> Methylation by chromosome (or contig):</b><p> 
		
		<#list methStats.chromosomeNamesEffective as chr> 
			<#assign plot = methStats.getChrPosStats(chr).toStringHistoPlotBases("Methylation: " + chr, "Position", "Methylation")>
			<img src="${plot}"><br>
			<#assign text = methStats.getChrPosStats(chr)>
			<pre>${text}</pre>
		</#list>
	</#if>
</center>

<!--==========================================================================
	Methylation by gene
	========================================================================== -->

<center>
	<hr> 
	<b> Methylation by gene </b><p>	
	<#assign geneMethStats=methStats.geneMethylationTable.getMethPercentStats("Gene")>
	<#if geneMethStats.validData>
		<hr> 
		<a name="genePerc">
		<center> <b> Gene methylation percentage</b> </center> 
		<p>
		<@intstatsTable geneMethStats />
		<img src="${geneMethStats.toStringPlot("Gene methylation histogram", "Methylation %", true)}"><br>
		</pre>
	</#if>
</center>

<!--==========================================================================
	P-Value by gene
	========================================================================== -->

<#if calcPvalues>

	<#assign methTab=methStats.geneMethylationTable>
	<#assign geneList=genome.genesSorted>
	<#assign types=methTab.typeList>
	
	<center>
		<hr>
		<a name="pvaluegene">
		<b> P-values by Genomic regions </b><p>
		
		<div class="note">
		Note: In order to capture hyper-methylated and hypo-methylated gene regions, p-values are calculated as the minimum of lower-tail and upper-tail Fisher exact tests. The hypergeometric distribution <i>h(k, N, m, n)</i> (reference: <a href="http://en.wikipedia.org/wiki/Hypergeometric_distribution">Wikipedia</a>) where  
		<ul>
			<li> <i>k</i> is the number of methylated bases
			<li> <i>N</i> is the total number of bases analyzed 
			<li> <i>m</i> is the total number of methylated bases
			<li> <i>n</i> is the number of bases analyzed in this gene
		</ul>
		Note: FDR is used to adjust for multiple testing.
		</div>
		
	</center>
	<p>
	<b>Note:</b> The following table is formatted as tab separated values
	
	<pre>
#GeneId<#list types as type>	Analyzed bases (${type})	Methylated bases (${type})	Methylated bases % (${type})	pValue (${type})</#list>
<#list geneList as gene>${gene.id}<#list types as type>	${methTab.getCounter(type).getBasesByTypeTotal(gene.id)}	${methTab.getCounter(type).getBasesByTypeMeth(gene.id)}	${(100 * methTab.getCounter(type).getProbBasesByType(gene.id) )?string("0.####")}%	${methTab.getCounter(type).getPvalue(gene.id)?string("0.####E0")}</#list>
</#list>
	</pre>
</#if>
			
<!--==========================================================================
	Errors and warnings
	========================================================================== -->

<center>
	<hr>	
	<a name="errors"> 
	<b> Errors and warnings </b><p>
	
	<table>
		<tr> 
			<td valign=top> <b> Total number or Errors / Warnings: </b> </td>
			<#assign color="#ffffff">
			<#if methStats.errorCount &gt; 0> <#assign color="#ff0000"> </#if> 
			<td bgcolor="${color}"> ${methStats.errorCount} </td>
		</tr>
	</table>
	<p>
	<table>
		<tr> <th>Number</th> <th>Details</th> </tr>
		<#assign errnum=1>
		<#list methStats.errors as err>
			<tr><td>${errnum}</td><td><pre>${err}</pre></td></tr>
			<#assign errnum=errnum+1>
		</#list>
	</table>
</center>

<!--==========================================================================
	End of 'main' div
	========================================================================== -->
</div>
</center>
