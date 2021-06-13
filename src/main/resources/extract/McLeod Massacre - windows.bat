:: ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
:: Copyright (c) 2021 Dart Productions
:: Released under the GNU General Public License version 3
::
:: This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
::
:: McLeod Massacre is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
:: +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

for %%a in (*.jar) do set "filename=%%a"
start javaw.exe -Djava.library.path=lib/ -cp "%filename%" dartproductions.mcleodmassacre.Main