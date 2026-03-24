#!/bin/bash

echo Starte das Skript http-test.sh...
JVM_ARGS="-cp .:./lib/*"
java $JVM_ARGS de.creditreform.crefoteam.cte.tesun.httpstest.HttpTestClient $1 $2 $3 $4 $5 $6

rc=$?
echo ERRORLEVEL: $rc
exit $rc
