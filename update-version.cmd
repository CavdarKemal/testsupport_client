@ECHO OFF

ECHO +++++++++++++++++++++++++++++++  Maven Goal +++++++++++++++++++++++++++++++
ECHO                auslieferung:minor-snapshot-versions
ECHO -------------------------------  Maven Goal -------------------------------
REM kurze Schreibweise, weil im POM das Plugin eingetragen wurde: call mvne auslieferung:update-versions -DnewVersion=1.17.1-SNAPSHOT
call mvne ctemvn:auslieferung-maven-plugin -DnewVersion=1.17.4-SNAPSHOT
pause
