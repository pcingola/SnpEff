#!/bin/bash -eu
set -o pipefail

# SnpEff wrapper script
# Extracts -Xm* and -D* JVM args from the argument list.

jardir="$HOME/snpEff"

java=java
if [ -n "${JAVA_HOME:-}" ] && [ -e "$JAVA_HOME/bin/java" ]; then
	java="$JAVA_HOME/bin/java"
fi

default_jvm_mem_opts="-Xms1g -Xmx8g"
jvm_mem_opts=""
jvm_prop_opts=""
pass_args=""
for arg in "$@"; do
    case $arg in
        '-D'*)
            jvm_prop_opts="$jvm_prop_opts $arg"
            ;;
         '-Xm'*)
            jvm_mem_opts="$jvm_mem_opts $arg"
            ;;
         *)
            pass_args="$pass_args $arg"
            ;;
    esac
done

if [ "$jvm_mem_opts" == "" ]; then
    jvm_mem_opts="$default_jvm_mem_opts"
fi

exec $java $jvm_mem_opts $jvm_prop_opts -jar "${jardir}/snpEff.jar" $pass_args
