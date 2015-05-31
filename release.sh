#!/bin/bash

cd json-view
mvn versions:set -DnewVersion=$1 -DgenerateBackupPoms=false && cd ..
cd spring-json-view
mvn versions:set -DnewVersion=$1 -DgenerateBackupPoms=false && cd ..

if [[ $1 == *"-SNAPSHOT" ]]; then
  git commit -a -m "incrementing snapshot version"
else
  git commit -a -m "$1 release"
fi