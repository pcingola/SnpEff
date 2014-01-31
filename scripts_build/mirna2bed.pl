#!/usr/bin/perl


while( $l = <STDIN> ) {
	chomp $l;
	if( $l !~ /^#/ ) {
		($mirbase_acc, $mirna_name, $gene_id, $gene_symbol, $transcript_id, $ext_transcript_id, $mirna_alignment, $alignment, $gene_alignment, $mirna_start, $mirna_end, $gene_start, $gene_end, $genome_coordinates, $conservation, $align_score, $seed_cat, $energy, $mirsvr_score) = split /\t/, $l;

		($gen, $chr, $pos, $strand) = split /:/, $genome_coordinates;
		$strand =~ tr/\]//d;
		($start, $end) = split /-/, $pos;
		if( $end =~ /(.*?),.*/ )	{ $end = $1; }

		# print "$chr\t$start\t$end\t$mirna_name;$strand;$mirbase_acc;$strand;$align_score;$mirna_alignment;$gene_alignment\n";
		print "$chr\t$start\t$end\t$mirna_name\n";
	}
}
