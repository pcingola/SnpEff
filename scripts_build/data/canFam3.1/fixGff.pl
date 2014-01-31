#!/usr/bin/perl

$debug = 0;

while( $l = <STDIN> ) {
	if( $l =~ /^#/ ) {
		# Show header lines
		print $l;
	} else {
		chomp $l;
		@t = split /\t/, $l;
		$type = $t[2];

		($info, $id, $name) = ('', '', '');

		if( $type ne 'gene' ) {
			print "\n$t[8]\n" if $debug;
        
			# Parse info fields
			@fields = split /;/, $t[8];
			foreach $field ( @fields ) {
				($key, $val) = split /=/, $field;
				print "\t$key => $val\n" if $debug;
        
				# Add 'name' as ID
				if( $key eq 'ID' )		{ $id = $val; }
				elsif( $key eq 'Name' )	{ $name = $val; }
				elsif( $key eq 'gene' )	{ ; }
				elsif( $key eq 'product' )	{ ; }
				elsif( $key eq 'Parent' )	{ 
					$info .= ";" if( $info ne '' );
        
					if( $id{$val} ne '' ) {
						$info .= "$key=$id{$val}";
					} else {
						$info .= "$key=$val";
					}
				} else					{ 
					$info .= ";" if( $info ne '' );
					$info .= "$key=$val";
				}
			}

			if( $name ne '' ) { 
				$info = "ID=$name;$info"; 
				$id{$id} = $name;
			} elsif( $id{$id} ne '' ) { 
				$info = "ID=$id{$id};$info"; 
			} else {
				$info = "ID=$id;$info"; 
			}

			# Replace origincal data and print line
			$t[8] = $info;
		}

		print join("\t", @t) . "\n";
	}
}
