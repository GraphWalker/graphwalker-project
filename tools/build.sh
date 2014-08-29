#!/bin/bash
set -ev
pushd ../graphwalker-project
mvn -q install
popd
