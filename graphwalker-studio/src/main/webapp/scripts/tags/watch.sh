if [ $# -eq 0 ]
then
  echo "usage: $0 <outputdir>"
else
  ls *.tag | entr ./compile.sh $1
fi

