#!/bin/sh

cd db/epigenome/

url="http://www.genboree.org/EdaccData/Current-Release/sample-experiment"

# Download all antries in directory
for dir in `wget -q -O - $url | cut -f 2 -d \" | grep "/$" | grep -v "\.\./"`
do
    echo $dir
    wget -nv -r --no-parent --no-clobber -A "*.bed.gz" "$url/$dir"
done

