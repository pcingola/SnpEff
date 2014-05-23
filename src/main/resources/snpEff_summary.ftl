<#-- snpEff summary statistics -->

<!--==========================================================================
	FTL macros 
	========================================================================== -->

<#macro countByType counter>
<table border=0>
	<thead>
		<tr> 
			<th><b> Type (alphabetical order)  </b></th>
			<th> &nbsp; </th>
			<th> Count </th>
			<th> Percent </th>
		</tr>
	</thead>
	<#list counter.typeList as type>
		<tr> 
			<td> <b> ${type} </b> </td> 
			<th> &nbsp; </th>
			<td class="numeric" bgcolor="${counter.getColorHtml(type)}"> ${counter.get(type)} </td> 
			<td class="numeric" bgcolor="${counter.getColorHtml(type)}"> ${ ( 100 * counter.percent(type) )?string("0.###") }% </td>
		</tr>
	</#list>
</table><br>
</#macro>

<#macro intstatsTable intstats>
	<#if intstats.validData>
	<table class="histo">
		<tr> <th width=15%>Min</th><td>${intstats.min}</td> </tr>
		<tr> <th>Max</th><td>${intstats.max}</td> </tr>
		<tr> <th>Mean</th><td>${intstats.mean}</td> </tr>
		<tr> <th>Median</th><td>${intstats.median}</td> </tr>
		<tr> <th>Standard deviation</th><td>${intstats.std}</td> </tr>
		<tr> <th>Values</th><td>${intstats.toStringValues()}</td> </tr>
		<tr> <th>Count</th><td>${intstats.toStringCounts()}</td> </tr>
		</tr>
	</table>
	</#if>
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

<center> <h3> SnpEff: Variant analysis </h3> </center>

<div style="margin-left: .5em">
<table class="toc"><tr><td>
	<center><b>Contents</b></center>
	<a href="#summary">Summary</a><br>
	<a href="#changeRateChr"> Change rate by chromosome</a><br>
	<a href="#changesByType">Variants by type</a><br>
	<a href="#effectsImpact"> Number of variants by impact </a><br>
	<a href="#effectsImpact"> Number of variants by functional class </a><br>
	<a href="#effects"> Number of variants by effect </a><br>
	<a href="#baseChages">Base change table</a><br>
	<a href="#tstv">Transition vs transversions (ts/tv)</a><br>
	<a href="#alleleFreq"> Frequency of alleles </a><br>
	<a href="#codonChanges"> Codon change table </a><br>
	<a href="#aaChanges"> Amino acid change table </a><br>
	<a href="#chrChanges"> Chromosome change plots </a><br>
	<a href="${genesFile}"> Details by gene </a><br>
</tr></td></table>
</div>


<!--==========================================================================
	Summary table 
	========================================================================== -->

<hr>
<a name="summary">
<center>
<b>Summary</b><p>

<table border=0>
	<tr bgcolor=ffffff> 
		<td valign=top> <b> Genome </b> </td>
		<td> ${genomeVersion} </td>
	</tr>
	<tr bgcolor=dddddd> 
		<td valign=top> <b> Date </b> </td>
		<td> ${date} </td>
	</tr>
	<tr bgcolor=ffffff> 
		<td valign=top> <b> SnpEff version </b> </td>
		<td> <pre>${version}</pre> </td>
	</tr>
	<tr bgcolor=dddddd> 
		<td valign=top> <b> Command line arguments </b> </td>
		<td> <pre>${args}</pre> </td>
	</tr>
	<tr bgcolor=ffffff> 
		<td valign=top> <b> Warnings </b> </td>
		<#assign color="#ffffff">
		<#if changeStats.countWarnings &gt; 0> <#assign color="#ff0000"> </#if> 
		<td bgcolor="${color}"> ${changeStats.countWarnings} </td>
	</tr>	
	<tr bgcolor=dddddd>
		<td valign=top> <b> Number of lines (input file) </b> </td>
		<td> ${countInputLines} </td>
	</tr>
	<tr bgcolor=ffffff>
		<td valign=top> <b> Number of variants (before filter) </b> </td>
		<td> ${countVariants} </td>
	</tr>
	<tr bgcolor=ffffff>
		<td valign=top> <b> Number of not variants <br>(i.e. reference equals alternative) </b> </td>
		<td> ${seqStats.countNonVariants} </td>
	</tr>
	<tr bgcolor=dddddd>
		<td valign=top> <b> Number of variants processed <br> (i.e. after filter and non-variants) </b> </td>
		<td> ${seqStats.count} </td>
	</tr>
	<tr bgcolor=ffffff>
		<td valign=top> <b> Number of known variants <br>(i.e. non-empty ID) </b> </td>
		<td> 
		${seqStats.countNonEmptyId}
		( ${ ( 100 * seqStats.getKnownRatio() )?string("0.###") }% ) 
		</td>
	</tr>
	<tr bgcolor=ffffff>
		<td valign=top> <b> Number of effects </b> </td>
		<td> ${countEffects} </td>
	</tr>
	<tr bgcolor=dddddd>
		<td valign=top> <b> Genome total length </b> </td>
		<td> ${seqStats.genomeLen} </td>
	</tr>
	<tr bgcolor=ffffff>
		<td valign=top> <b> Genome effective length </b> </td>
		<td> ${seqStats.genomeLenEffective} </td>
	</tr>
	<tr bgcolor=dddddd>
		<td valign=top> <b> Change rate </b> </td>
		<td> 1 change every ${seqStats.rateOfChange} bases </td>
	</tr>
</table>
<p>
</center>

<!--==========================================================================
	Change rate by chromosome
	========================================================================== -->

<hr> 
<a name="changeRateChr"> 

<center>
<b> Change rate details </b><p>

<table border=1>
	<tr><th> Chromosome </th><th> Length </th><th> Changes </th><th> Change rate </th></tr> 
	<#list seqStats.chromosomeNamesEffective as chr> 
		<tr>
			<td> ${chr} </td>
			<td class="numeric"> ${seqStats.getChromosomeLength(chr)} </td>
			<td class="numeric"> ${seqStats.getCountByChromosome(chr)} </td>
			<td class="numeric"> ${seqStats.getRateOfChangeByChromosome(chr)} </td>
		</tr> 
	</#list>
	<tr>
		<th> Total </th>
		<th class="numeric"> ${seqStats.genomeLenEffective} </th>
		<th class="numeric"> ${seqStats.count} </th>
		<th class="numeric"> ${seqStats.rateOfChange} </th>
	</tr> 
</table> 
</center>

<!--==========================================================================
	Changes by type
	========================================================================== -->

<hr> 
<a name="changesByType"> 
<center>
<b> Number changes by type</b><p>

<table border=1>
	<thead>
		<tr>
			<th> <b> Type   </b> </th>
			<th> <b> Total  </b> </th>
		</tr>
	</thead>
    <tbody>
    	<#list seqStats.changeType as chType>
	    <tr>
	    	<td> <b> ${chType} </b> </td>
	    	<td class="numeric" bgcolor="${seqStats.countByChangeType.getColorHtml(chType)}"> ${seqStats.countByChangeType.get(chType)} </td>
	    </tr>
    	</#list>
    </tbody>
    <tfoot>
	    <tr>
	    	<th><b>Total </b> </th>
	    	<th class="numeric"> ${seqStats.countByChangeType.get("Total")} </th>
	    </tr>
    </tfoot>
</table>
</center>

<!--==========================================================================
	Effects by impact
	========================================================================== -->

<hr> 
<a name="effectsImpact"> 
<center>
<b> Number of effects by impact </b> <p> 

<@countByType changeStats.countByImpact />
<p>
</center>

<!--==========================================================================
	Effects by functional class
	========================================================================== -->

<hr> 
<a name="effectsFuncClass"> 
<center>
<b> Number of effects by functional class </b> <p> 

<@countByType changeStats.countByFunctionalClass />
<p>

Missense / Silent ratio: </th><td class="numeric"> ${changeStats.silentRatio?string("0.####")}

</center>

<!--==========================================================================
	Effects stats
	========================================================================== -->

<hr> 
<a name="effects"> 
<center>
<b> Number of effects by type and region </b> <p> 

<table border=0>
	<tr>
		<th> Type </th>
		<th> Region </th>
	</tr>
	<tr>
		<td> <@countByType changeStats.countByEffect /> </td>
		<td> <@countByType changeStats.countByGeneRegion /> </td>
	</tr>
</table>
<p>

<img src="${changeStats.plotGene}" border=1><p>

</center>

<!--==========================================================================
	InDels
	========================================================================== -->

<#if seqStats.indelLen.validData>
	<hr> 
	<b> Insertions and deletions length:</b> 
	<p>
	<pre>
		<@intstatsTable seqStats.indelLen />		
		<img src="${seqStats.indelLenHistoUrl}"><br>
	</pre>
</#if>

<!--==========================================================================
	Base changes
	========================================================================== -->

<hr> 
<a name="baseChages"> 
<center>
<b> Base changes (SNPs) </b> <p>

<table border=1>
    <tr>
    	<td> &nbsp; </td> <#list seqStats.bases as newBase > <th> <b> ${newBase} </b> </th></#list>
    </tr>
    <#list seqStats.bases as oldBase >
	    <tr> <th> <b> ${oldBase} </b> </th><#list seqStats.bases as newBase ><td class="numeric" bgcolor="${seqStats.getBasesChangesColor(oldBase, newBase)}"> ${seqStats.getBasesChangesCount(oldBase, newBase)} </td></#list> </tr>
    </#list>
</table>
</center>

<p>

<!--==========================================================================
	Ts/Tv
	========================================================================== -->

<hr> 
<a name="tstv">
<center> <b> Ts/Tv (transitions / transversions) </b> <p> </center>

<small>
<b>Note:</b> Only SNPs are used for this statistic.<br>
<b>Note:</b> This Ts/Tv ratio is a 'raw' ratio. Some people prefer to use a ratio of rates, not observed events. In that case, you need to multiply by 2.0 (since there are twice as many possible transitions than transversions, E[Ts/Tv] ratio is twice the ratio of events).
</small>
<p>

<center>
<table border=1>
	<tr> <th> Transitions </th><td class="numeric"> ${seqStats.transitions} </td> </tr>
	<tr> <th> Transversions </th><td class="numeric"> ${seqStats.transversions} </td> </tr>
	<tr> <th> Ts/Tv ratio </th><td class="numeric"> ${seqStats.tsTvRatio?string("0.####")} </td> </tr>
</table>
</center>
<p>

<#assign tstv=vcfStats.hasData()>
<#if tstv>
	<b>All variants:</b>
	<pre>${vcfStats.tsTvStats}</pre>
	<p>
	<b>Only known variants</b> (i.e. the ones having a non-empty ID field):
	<pre>${vcfStats.tsTvStatsKnown}</pre>
</#if>
<p>

<!--==========================================================================
	Allele frequency
	========================================================================== -->

<hr> 
<a name="alleleFreq">
<center> <b> Frequency of alleles </b> <p> </center>

Note: Number of times an allele appears once (singleton), twice (doubletons), etc.<p>

<#assign af=vcfStats.hasData()>
<#if af>
	<b>All variants:</b>
	<img src="${vcfStats.alleleFrequencyHistoUrl}"><br>
	<@intstatsTable vcfStats.alleleFrequencyStats.count />
	<p>
	<b>Only known variants</b> (i.e. the ones having a non-empty ID field):
	<@intstatsTable vcfStats.alleleFrequencyStatsKnown.count />
</#if>
<p>


<!--==========================================================================
	Codon change table
	========================================================================== -->
	
<hr> 
<a name="codonChanges">
<center>
<b> Codon changes</b> <p>

	<div class="note">
		How to read this table: <br>
		- Rows are reference codons and columns are changed codons. E.g. Row 'AAA' column 'TAA' indicates how many 'AAA' codons have been replaced by 'TAA' codons.<br>
		- Red background colors indicate that more changes happened (heat-map).<br>
		- Diagonals are indicated using grey background color <br> 
		- WARNING: This table may include different translation codon tables (e.g. mamalian DNA and mitochondrial DNA).<br>
		<p>
	</div><p>

  
<table border=1>
	<thead>
		<tr> 
			<th> &nbsp; </th>
			<#list changeStats.codonList as newCodon> <th> ${newCodon} </th> </#list>
		</tr>
	</thead>
	<#list changeStats.codonList as oldCodon>
			<tr> 
				<th> ${oldCodon} </th>
				<#list changeStats.codonList as newCodon>
					<#assign count = changeStats.getCodonChangeCount(oldCodon, newCodon)>
					<#if count == 0> 
						<#assign count="&nbsp"> 
					</#if>
					
					<#if oldCodon == newCodon> 
						<th class="numeric"> ${count} </th>
					<#else>
						<td class="numeric" bgcolor="${changeStats.getCodonChangeColor(oldCodon, newCodon)}"> ${count} </td>
					</#if>
				</#list>
			</tr>
	</#list>
</table>
</center>

<!--==========================================================================
	Amino acid change table
	========================================================================== -->

<hr> 
<a name="aaChanges">
<center>
<b> Amino acid changes</b><p> 

	<div class="note">
		How to read this table: <br>
		- Rows are reference amino acids and columns are changed amino acids. E.g. Row 'A' column 'E' indicates how many 'A' amino acids have been replaced by 'E' amino acids.<br>
		- Red background colors indicate that more changes happened (heat-map).<br>
		- Diagonals are indicated using grey background color <br> 
		- WARNING: This table may include different translation codon tables (e.g. mamalian DNA and mitochondrial DNA).<br>
		<p>
	</div><p>

<table border=1>
	<thead>
		<tr> 
			<th> &nbsp; </th>
			<#list changeStats.aaList as newAa> <th> ${newAa} </th> </#list>
		</tr>
	</thead>
	<#list changeStats.aaList as oldAa>
			<tr> 
				<th> ${oldAa} </th>
				<#list changeStats.aaList as newAa>
					<#assign count = changeStats.getAaChangeCount(oldAa, newAa)>
					<#if count == 0> 
						<#assign count="&nbsp"> 
					</#if>
					
					<#if oldAa == newAa> 
						<th class="numeric"> ${count} </th>
					<#else>
						<td class="numeric" bgcolor="${changeStats.getAaChangeColor(oldAa, newAa)}"> ${count} </td>
					</#if>
				</#list>
			</tr>
	</#list>
</table>
</center>

<!--==========================================================================
	Chromosome change table
	========================================================================== -->

<#if chromoPlots>
<hr> 
<a name="chrChanges">
<center>
<b> Changes by chromosome</b><p> 

<center>
<#list seqStats.chromosomeNamesEffective as chr> 
	<#assign chrStats = seqStats.getChrPosStats(chr)>
	<pre>
		<img src="${chrStats.toStringHistoPlot("Changes histogram: " + chr, "Position", "Changes")}"><br>
		${chrStats}
	</pre>
</#list>
</center>
</#if>

<!--==========================================================================
	Changes by gene 
	========================================================================== -->

<center>
	<hr>
	<b> Details by gene </b><p>
	<p>
	<b><a href="${genesFile}">Here</a></b> you can find a tab-separated table.
</center>
</div>
</center>