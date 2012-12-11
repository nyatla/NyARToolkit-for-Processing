cd %~dp0
echo off
echo For Processing 1.5.x.
echo This batch file copies NyARToolkit library jar files for Processing 1.5.x to example code directories.
set libpsg=..\library\NyAR4psg.jar
set libnyar=..\library\NyARToolkit.jar

pause
del /q arPlusNyIdMarker\code\*.jar
del /q imagePickup\code\*.jar
del /q markerPlane\code\*.jar
del /q multiMarker\code\*.jar
del /q nonBeginSequence\code\*.jar
del /q rotation\code\*.jar
del /q simpleLite\code\*.jar
del /q simpleNyId\code\*.jar
del /q test\MultiMarkerTest\code\*.jar
del /q test\NyARBoardTest\code\*.jar
del /q test\SingleARTKMarkerTest\code\*.jar
del /q test\SingleNyIdMarkerTest\code\*.jar
del /q withOpenGL\code\*.jar
del /q withPicking\code\*.jar
del /q pngMarker\code\*.jar
copy /y %libpsg% arPlusNyIdMarker\code\
copy /y %libpsg% imagePickup\code\
copy /y %libpsg% markerPlane\code\
copy /y %libpsg% multiMarker\code\
copy /y %libpsg% nonBeginSequence\code\
copy /y %libpsg% rotation\code\
copy /y %libpsg% simpleLite\code\
copy /y %libpsg% simpleNyId\code\
copy /y %libpsg% test\MultiMarkerTest\code\
copy /y %libpsg% test\NyARBoardTest\code\
copy /y %libpsg% test\SingleARTKMarkerTest\code\
copy /y %libpsg% test\SingleNyIdMarkerTest\code\
copy /y %libpsg% withOpenGL\code\
copy /y %libpsg% withPicking\code\
copy /y %libpsg% pngMarker\code\

copy /y %libnyar% arPlusNyIdMarker\code\
copy /y %libnyar% imagePickup\code\
copy /y %libnyar% markerPlane\code\
copy /y %libnyar% multiMarker\code\
copy /y %libnyar% nonBeginSequence\code\
copy /y %libnyar% rotation\code\
copy /y %libnyar% simpleLite\code\
copy /y %libnyar% simpleNyId\code\
copy /y %libnyar% test\MultiMarkerTest\code\
copy /y %libnyar% test\NyARBoardTest\code\
copy /y %libnyar% test\SingleARTKMarkerTest\code\
copy /y %libnyar% test\SingleNyIdMarkerTest\code\
copy /y %libnyar% withOpenGL\code\
copy /y %libnyar% withPicking\code\
copy /y %libnyar% pngMarker\code\
pause