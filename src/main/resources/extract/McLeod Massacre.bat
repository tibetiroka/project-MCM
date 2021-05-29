for %%a in (*.jar) do set "filename=%%a"
start javaw -Djava.library.path=lib/ -jar "%filename%"