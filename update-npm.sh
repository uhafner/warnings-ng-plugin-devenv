#!/bin/bash

files=`ls */package-lock.json`

for lock in $files; do
  dir=`dirname $lock`
  rm -f $lock
  cd $dir
  echo "Running <npm install> in $dir"
  npm install
  npm audit fix --package-lock-only
  cd ..
done
