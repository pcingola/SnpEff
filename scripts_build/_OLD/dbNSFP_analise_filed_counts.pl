#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Show entries that have the same number of counts
#-------------------------------------------------------------------------------
sub showSame() {
	for( $i=0 ; $i < $#t ; $i++ ) {
		if( $multiple[$i] ) {
			print "\t$names[$i]";
			for( $j=$i+1 ; $j < $#t ; $j++ ) {
				if( $count[$i][$j] ) {
					print "\t$names[$j]";
				}
			}
			print "\n";
		}
	}
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------
for( $lineNum=1 ; $l = <STDIN> ; $lineNum++ ) {
	chomp $l;
	@t = split /\t/, $l;

	if( $lineNum == 1 ) {
		@names = @t;

		for( $i=0 ; $i < $#t ; $i++ ) {
			for( $j=0 ; $j < $#t ; $j++ ) {
				$count[$i][$j] = 1;
			}
		}
	} else {
		
		# Split each field and count number of values
		@countVals = ();
		for( $i=0 ; $i < $#t ; $i++ ) {
			@v = split /;/, $t[$i];
			$countVals[$i] = $#v;
			$multiple[$i] = 1 if( $countVals[$i] > 0 );
		}
		
		# Remove entry if counts differ
		$countSame = 0;
		for( $i=0 ; $i < $#t ; $i++ ) {
			if( $countVals[$i] > 0 ) {
				for( $j=0 ; $j < $#t ; $j++ ) {
					if( $count[$i][$j] ) {
						$countSame++;
						if( $countVals[$i] != $countVals[$j] ) {
							print "Counts differ: $countVals[$i] != $countVals[$j]\t$names[$i] != $names[$j]\t'$t[$i]'\t'$t[$j]'\n";
							$count[$i][$j] = 0 if $countVals[$i] != $countVals[$j];
						}
					}
				}
			}
		}
	}

	if( $lineNum % 1000 == 0 ) {
		print "$lineNum: $countSame\n";
		showSame()
	}
}



