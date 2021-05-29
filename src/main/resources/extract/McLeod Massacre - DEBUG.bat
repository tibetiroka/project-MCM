for %%a in (*.jar) do set "filename=%%a"
java -Djava.library.path=lib/ -jar "%filename%" --debug 
PAUSE