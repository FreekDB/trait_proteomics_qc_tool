pylint --max-line-length=120 -f parseable --include-ids=y QC > pylint.txt || exit 0

pep8 QC > pep8.txt || exit 0

nosetests --with-xunit