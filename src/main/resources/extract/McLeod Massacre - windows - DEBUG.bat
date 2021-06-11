Rem +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
Rem Copyright (c) 2021 Dart Productions
Rem Released under the GNU General Public License version 3
Rem
Rem This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
Rem +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

for %%a in (*.jar) do set "filename=%%a"
java.exe -Djava.library.path=lib/ -cp "%filename%" dartproductions.mcleodmassacre.Main --debug
PAUSE