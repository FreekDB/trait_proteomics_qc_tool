pep8 --ignore="E501" QC > pep8.txt

nosetests --with-xunit
pylint --max-line-length=120 --disable="E0602,W0511,R0801,I0011" -f parseable --include-ids=y QC > pylint.txt || exit 0