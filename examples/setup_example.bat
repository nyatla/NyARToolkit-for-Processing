cd %~dp0
echo off
echo This batch file copies library jar files to example code directories.
pause
copy ..\library\*.jar arPlusNyIdMarker\code\*
copy ..\library\*.jar imagePickup\code\*
copy ..\library\*.jar markerPlane\code\*
copy ..\library\*.jar multiMarker\code\*
copy ..\library\*.jar nonBeginSequence\code\*
copy ..\library\*.jar rotation\code\*
copy ..\library\*.jar simpleLite\code\*
copy ..\library\*.jar simpleNyId\code\*
copy ..\library\*.jar test\MultiMarkerTest\code\*
copy ..\library\*.jar test\NyARBoardTest\code\*
copy ..\library\*.jar test\SingleARTKMarkerTest\code\*
copy ..\library\*.jar test\SingleNyIdMarkerTest\code\*
copy ..\library\*.jar test\withOpenGL\code\*
copy ..\library\*.jar withPicking\code\*
pause