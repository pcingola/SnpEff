<#-- BunFinder summary statistics -->

<!--==========================================================================
	FTL macros 
	========================================================================== -->

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

<center> <h3> BunFinder: Chip-Seq analysis </h3> </center>

<div style="margin-left: .5em">
<table class="toc"><tr><td>
	<center><b>Contents</b></center>
	<a href="#summary">Summary</a><br>
	<#if bunFinder.qualityStats.validData>
	<a href="#quality">Quality histogram and data</a><br>
	</#if>
	<a href="#coverageTable">Coverage table</a><br>
	<#if bunFinder.coverageTotal.validData>
	<a href="#coverage">Coverage histogram</a><br>
	</#if>
	<#if bunFinder.coveragePlus.validData>
	<a href="#coveragePlus">Coverage histogram (plus strand)</a><br>
	</#if>
	<#if bunFinder.coverageMinus.validData>
	<a href="#coverageMinus">Coverage histogram (minus strand)</a><br>
	</#if>
	<a href="#errors">Errors and Warnings (details)</a><br>
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
	<tr> 
		<td valign=top> <b> Genome </b> </td>
		<td> ${genomeVersion} </td>
	</tr>
	<tr> 
		<td valign=top> <b> Date </b> </td>
		<td> ${date} </td>
	</tr>
	<tr> 
		<td valign=top> <b> BunFinder version </b> </td>
		<td> <pre>${version}</pre> </td>
	</tr>
	<tr> 
		<td valign=top> <b> Command line arguments </b> </td>
		<td> <pre>${args}</pre> </td>
	</tr>
	<tr> 
		<td valign=top> <b> Errors </b> </td>
		<#assign color="#ffffff">
		<#if bunFinder.errorCount &gt; 0> <#assign color="#ff0000"> </#if> 
		<td bgcolor="${color}"> ${bunFinder.errorCount} </td>
	</tr>
	<tr> 
		<td valign=top> <b> SAM entries </b> </td>
		<td> <pre>${samEntries}</pre> </td>
	</tr>
	<tr> 
		<td valign=top> <b> Mapped SAM entries </b> </td>
		<td> <pre>${mappedEntries}</pre> </td>
	</tr>
	
</table>
<p>

</center>


<!--==========================================================================
	Quality histogram
	========================================================================== -->

<#if bunFinder.qualityStats.validData>
	<hr>
	<a name="quality"> 
	<center><b> Quality: </b></center><p>
	
	<img src="${bunFinder.qualityStats.toStringPlot("Quality histogram", "Quality", true) }"><br>
	<@intstatsTable bunFinder.qualityStats />
	</pre>
</#if>

<!--==========================================================================
	Coverage histogram
	========================================================================== -->

<#if bunFinder.coverageTotal.validData>
	<hr> 
	<a name="coverage"> 
	<center><b> Coverage histograms </b></center><p>
	<img src="${bunFinder.coverageTotal.toStringPlot("Coverage histogram", "Coverage", true) }"><br>
	<@intstatsTable bunFinder.coverageTotal />
</#if>

<#if bunFinder.coveragePlus.validData>
	<hr> 
	<a name="coveragePlus"> 
	<center><b> Coverage histograms (plus strand) </b></center><p>
	<img src="${bunFinder.coveragePlus.toStringPlot("Coverage histogram (+ strand)", "Coverage", true) }"><br>
	<@intstatsTable bunFinder.coveragePlus />
</#if>

<#if bunFinder.coverageMinus.validData>
	<hr> 
	<a name="coverageMinus"> 
	<center><b> Coverage histograms (minus strand) </b></center><p>
	<img src="${bunFinder.coverageMinus.toStringPlot("Coverage histogram (- strand)", "Coverage", true) }"><br>
	<@intstatsTable bunFinder.coverageMinus />
</#if>

<!--==========================================================================
	CG-content histogram
	========================================================================== -->

<#if bunFinder.cgContent.validData>
	<hr> 
	<a name="cgContent"> 
	<center><b> CG-content histogram (number of reads) </b></center><p>
	<img src="${bunFinder.cgContent.toStringPlot("CG content histogram", "CG-Content %", true) }"><br>
	<@intstatsTable bunFinder.cgContent />
</#if>

<!--==========================================================================
	Strand histogram
	========================================================================== -->

<#if bunFinder.strandHisto.validData>
	<hr> 
	<a name="strandHisto"> 
	<center><b> Strand histogram (number of reads) </b></center><p>
	<img src="${ bunFinder.strandHisto.toStringPlot("String histogram", "Strand", true) }"><br>
	<@intstatsTable bunFinder.strandHisto />
</#if>

<!--==========================================================================
	Stats by chromosome
	========================================================================== -->

<hr> 
<center>
<a name="chromo">
<b> Stats by chromosome </b><p>
<table border=0>
	<tr>
		<th> Chromosome </th>
		<th> Length </th>
		<th> Zeros </th>
	</tr> 
	<#list bunFinder.chromoBuns as chrBun> 
		<tr>
			<td> ${chrBun.chromosome.id} </td>
			<td> ${chrBun.chromosome.size()} </td>
			<td> ${chrBun.countZeros} </td>
		</tr> 
	</#list>
</table> 

<#if chromoPlots>
	<#list bunFinder.chromoBuns as chrBun>
		<p>
		<b> Chromosome: ${chrBun.chromosome.id} </b><p>
		
		<img src="${ chrBun.coverageTotal.toStringPlot("Coverage: " + chrBun.chromosome.id, "Position", true) }"><br>
		<@intstatsTable chrBun.coverageTotal />		
		
		<#assign winSize = 100>
		<b> Coverage histogram using non-overlapping windows (window size: ${winSize} )  </b><p>
		<#assign winCoverage = chrBun.windowCoverageStats(winSize, false)>
		<img src="${ winCoverage.toStringPlot("Coverage (window size: " + winSize + "/false) " + chrBun.chromosome.id, "Max reads per window", true) }"><br>
		<@intstatsTable winCoverage />		
		
	</#list>
</#if>

</center>

<!--==========================================================================
	Errors and warnings
	========================================================================== -->

<hr>	
<a name="errors"> 
<center><b> Errors and warnings </b><p></center>

<table>
	<tr> 
		<td valign=top> <b> Total number or Errors / Warnings: </b> </td>
		<#assign color="#ffffff">
		<#if bunFinder.errorCount &gt; 0> <#assign color="#ff0000"> </#if> 
		<td bgcolor="${color}"> ${bunFinder.errorCount} </td>
	</tr>
</table>
<p>
<table>
	<tr> <th>Number</th> <th>Details</th> </tr>
	<#assign errnum=1>
	<#list bunFinder.errors as err>
		<tr><td>${errnum}</td><td><pre>${err}</pre></td></tr>
		<#assign errnum=errnum+1>
	</#list>
</table>

</div>
</center>
