if [ $# -eq 0 ]
then
  echo "usage: $0 <outputdir>"
  exit 1
else
  [ -d $1 ] || mkdir $1
  cat *.tag > $1/compiled.tag && cd $1
  riot -m compiled.tag Studio.js
  sed -i "10i 'use strict';" Studio.js
  rm compiled.tag
  exit 0
fi
