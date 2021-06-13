:: +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
:: Copyright (c) 2021 Dart Productions
:: Released under the GNU General Public License version 3
::
:: This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
:: +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

for %%a in (*.jar) do set "filename=%%a"
start javaw.exe -Djava.library.path=lib/ -cp "%filename%" dartproductions.mcleodmassacre.Main