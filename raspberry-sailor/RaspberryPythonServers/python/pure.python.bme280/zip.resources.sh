#!/bin/bash
pushd $(dirname $0)/web
zip ../web.zip -r ./*
popd
echo -e "Done archiving"
echo -e "Use unzip -l web.zip to see the content of the archive."