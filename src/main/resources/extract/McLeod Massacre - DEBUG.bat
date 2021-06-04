for %%a in (*.jar) do set "filename=%%a"
java.exe -Djava.library.path=lib/ -cp "%filename%" dartproductions.mcleodmassacre.Main --debug
PAUSE