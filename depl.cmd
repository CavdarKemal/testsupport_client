ECHO +++++++++++++++++++++++++++++++  Batch cit.cmd  +++++++++++++++++++++++++++++++
ECHO clean install [-DskipTests=false]
ECHO -------------------------------  Batch cit.cmd  -------------------------------
call mvn deploy -DskipTests=false -P itest
pause
