#!/bin/bash

cd json-view
mvn versions:set -DnewVersion=0.4 -DgenerateBackupPoms=false && cd ..
cd spring-json-view
mvn versions:set -DnewVersion=0.4 -DgenerateBackupPoms=false && cd ..