#!/bin/sh

set -e
modules="core java io cli maven-plugin dashboard example"

for module in $modules
do
    pushd ..
    if [ -d "graphwalker-$module" ]; then
    	pushd "graphwalker-$module"
     	git pull
    	popd
    else	
    	git clone "https://github.com/GraphWalker/graphwalker-$module.git"
	fi
    popd
done
