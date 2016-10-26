
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>SnpEff: Translocation report</title>

    <!-- Bootstrap -->
    <link href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>
<body>

<center>
<div class="main">

<center> <h1> SnpEff: Translocation report </h1> </center>

<div>
<h3>Summary</h3>
<p>
<table class="table table-striped">
	<tr> 
		<th> SnpEff version</th>
		<td> <pre>${version}</pre> </td>
	</tr>
	<tr> 
		<th> Date &amp; Time </th>
		<td> ${date} </td>
	</tr>
	<tr> 
		<th> Command line arguments </th>
		<td> <pre>${args}</pre> </td>
	</tr>
	<tr>
		<th> Number of translocations </th>
		<td> ${countTranslocations} </td>
	</tr>
</table>
<h3>Index</h3>
<p>
<table class="table table-striped">
	<tr> 
		<th> Effect </th>
		<th> Genes </th>
		<th> Transcripts </th>
		<th> Coordiantes </th>
	</tr>
<#list translocations as transloc>
	<tr> 
		<td> <a href="#${transloc.index}"></a> ${transloc.variantEffect} </td>
		<td> <a href="#${transloc.index}">${transloc.geneName1} - ${transloc.geneName2} </a> </td>
		<td> <a href="#${transloc.index}">${transloc.trId1} - ${transloc.trId2}</a> </td>
		<th> <a href="#${transloc.index}">${transloc.chr1}:${transloc.pos1OneBased} - ${transloc.chr2}:${transloc.pos2OneBased}</a> </th>
	</tr>
</#list>
</table>
</div>

<h3>Details</h3>
<div>
<#list translocations as transloc>
	<a name="${transloc.index}"><h3>${transloc.index}</h3></a>
	<table class="table table-striped">
		<tr> 
			<th> </th>
			<th> BND Endpoint 1 </th>
			<th> BND Endpoint 2 </th>
		</tr>
		<tr> 
			<th> Genes </th>
			<td> ${transloc.geneName1} </td>
			<td> ${transloc.geneName2} </td> 
		</tr>
		<tr> 
			<th> Transcript IDs </th>
			<td> ${transloc.trId1} </td>
			<td> ${transloc.trId2} </td> 
		</tr>
		<tr> 
			<th> Genomic coordinates </th>
			<td> ${transloc.chr1} : ${transloc.pos1OneBased} </td>
			<td> ${transloc.chr2} : ${transloc.pos2OneBased} </td> 
		</tr>
		<tr>
			<th> Putative effect </th> 
			<td colspan=2> ${transloc.variantEffect} </td>
		</tr>
		<tr>
			<th> Putative impact </th> 
			<td colspan=2> ${transloc.impact} </td>
		</tr>
		<tr>
			<th> HGVS.p </th> 
			<td colspan=2> ${transloc.hgvsP} </td>
		</tr>
		<tr>
			<th> HGVS.c </th> 
			<td colspan=2> ${transloc.hgvsC} </td>
		</tr>
	</table>

	<b> Vcf annotation entry (raw data) </b>
	<pre> ${transloc.vcfEffect} </pre>

	${transloc.svgPlot}	
</#list>
	
</div>

</body>