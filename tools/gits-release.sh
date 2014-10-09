#!/bin/bash

set -ev
modules="project core java io cli dsl maven-plugin maven-archetype"
for module in $modules
do
    pushd ..
    if [ -d "graphwalker-$module" ]; then
    	pushd "graphwalker-$module"
     	git "$@"
    	popd
	fi
    popd
done
