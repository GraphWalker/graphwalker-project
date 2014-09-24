#!/bin/bash
set -ev
pushd ../graphwalker-project
mvn -q clean install
popd
