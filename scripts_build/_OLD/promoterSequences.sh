#!/bin/sh

DIR=$HOME/snpEff/

java -Xmx3G \
	-classpath "$DIR/lib/charts4j-1.2.jar:$DIR/lib/flanagan.jar:$DIR/lib/freemarker.jar:$DIR/lib/junit.jar:$DIR/lib/trove-2.1.0.jar:$DIR" \
	ca.mcgill.mcb.pcingola.PromoterSequences \
	$*
