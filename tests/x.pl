#!/usr/bin/perl

$debug = 0;

while( $l = <STDIN> ) {
	chomp $l;

	if( $l =~ /^#/ ) {
		# Title line
		print "$l\n";
	} else {
		print "$l\n" if $debug;

		# Fields
		@t = split /\t/, $l;

		$alt = $t[4];
		@alts = split /,/, $alt;
	
		# INFO
		@kv = split /;/, $t[7];

		$info = "";
		foreach $inf ( @kv ) {
			# Key-value pairs
			($key, $val) = split /=/, $inf;

			$info .= ";" if $info ne '';

			if( $key eq 'EFF' ) {
				@effs = split /,/, $val;

				# Each effect
				$effsStr = '';
				foreach $eff ( @effs ) {
					if( $eff =~ /(.*)\((.*)\)/ ) {
						($ann, $paren) = ($1, $2);
                    
						print "\t$ann\t$paren\n" if $debug;
						# Split fields
						@f = split /\|/, $paren;

						# Replace GT sub-field
						$gt = $f[10];
						$gtNew = $gt;
						if(($gt ne '') && ($gt > 0)) {
							$gtNew = $alts[$gt-1];
							print "\t\t$gt\t$gtNew\n" if $debug;
						}

						$f[10] = $gtNew;

						$eff = $ann . '(' . join('|', @f) . ')';
					}

	
					$effsStr .= "," if $effsStr ne '';
					$effsStr .= $eff;
				}

				print "\t$effsStr\n" if $debug;
				$info .= "$key=$effsStr";
			} else {
				$info .= $inf;
			}
		}

		$t[7] = $info;

		print join("\t", @t) . "\n";
	}
}
