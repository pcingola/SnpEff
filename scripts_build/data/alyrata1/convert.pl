#!/usr/bin/perl


# Parse command line argument
$gtfFile = $ARGV[0];

#---
# Pass one: Get geneID -> transcriptID 
# Note: It is assumed (and it is true for this particular file) that each genes has only one transcript
#       This assumption os obviously not true for other files & other organisms
#----
print STDERR "Pass 1: Read IDs\n";
open GTF, $gtfFile or die "Cannot open file $gtfFile\n";

while( $l = <GTF> ) {
	chomp $l;
	($seqname, $source, $feature, $start, $end, $score, $strand, $frame, $attr) = split /\t/, $l;

	$gid = $tid = "";
	if( $attr =~ /gene_id "(.*?)";/ )	{ $gid = $1; }
	if( $attr =~ /transcript_id "(.*?)";/ )	{ $tid = $1; }

	if(( $gid ne '' ) && ( $tid ne '' )) {
		# print "\tgid = $gid\n\ttid = $tid\n";

		# Store transcriptId
		if( $tbyg{$gid} eq '' )		{ $tbyg{$gid} = $tid; }

		# Is there more than one transcript per gene? => Error
		if( $tbyg{$gid} ne $tid )	{ die "Error: More than one transcript per gene. We cannot map transcripts!\n\t$tid ne $tbyg{$gid}\n"; }
	}
}

close GTF;

#---
# Pass two: Fill in the missing transcript IDs
#---
print STDERR "Pass 2: Add ID data\n";
open GTF, $gtfFile or die "Cannot open file $gtfFile\n";

while( $l = <GTF> ) {
	chomp $l;
	($seqname, $source, $feature, $start, $end, $score, $strand, $frame, $attr) = split /\t/, $l;

	$gid = "";
	if( $attr =~ /gene_id "(.*?)";/ )	{ $gid = $1; }
	elsif( $attr =~ /gene_id "(.*)"/ )	{ $gid = $1; }
	else					{ print STDERR "Cannot match '$attr'\n"; }
	$tid = $tbyg{$gid};

	if(( $gid ne '' ) && ( $tid ne '' )) { print "$seqname\t$source\t$feature\t$start\t$end\t$score\t$strand\t$frame\tgene_id \"$gid\"; transcript_id \"$tid\";\n"; } 
	else { print STDERR "Skipping line: $l\n\tgid = '$gid'\n\ttid = '$tid'\n"; }
}

close GTF;
