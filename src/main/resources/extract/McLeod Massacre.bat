for %%a in (*.jar) do set "filename=%%a"
start javaw.exe -Djava.library.path=lib/ -jar "%filename%"