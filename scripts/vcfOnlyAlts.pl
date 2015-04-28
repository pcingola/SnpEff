#!/usr/bin/perl

while( $l = <STDIN> ) {

	if( $l =~ /^#/ ) { 
		print $l; 
	} else {
		@t = split /\t/, $l;
		$alt = $t[4];

		# Show if ALT fiels is not empty and doesn't have '<'
		if(( $alt ne '.' ) && ($alt !~ /^</)) { print $l; }
	}
}
