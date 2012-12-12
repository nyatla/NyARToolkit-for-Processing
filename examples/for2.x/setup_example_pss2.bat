cd %~dp0
echo off
echo For Processing 2.x.
echo This batch file copies NyARToolkit library jar files for Processing 2.x to example code directories.
set libpsg=..\..\library\NyAR4psg2b.jar
set libnyar=..\..\library\NyARToolkit.jar

pause

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
