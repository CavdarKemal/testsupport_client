@@ECHO OFF
SET JVM_ARGS=-Dfile.encoding=UTF8 -classpath .;lib/*
SET AUFRUF=java %JVM_ARGS% de.creditreform.crefoteam.cte.tesun.httpstest.HttpTestClient %1 %2 %3 %4 %5
ECHO %AUFRUF%
%AUFRUF%

GOTO ENDE

:ENDE
