for %%a in (*.jar) do set "filename=%%a"
java.exe -Djava.library.path=lib/ -jar "%filename%" --debug
PAUSE