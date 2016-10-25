
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
</div>

<h3>Details</h3>
<div>
<#list translocations as transloc>
	<table class="table table-striped">
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