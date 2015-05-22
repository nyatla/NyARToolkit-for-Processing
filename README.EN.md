# NyARToolkit for proce55ing

Copyright (C)2008-2012 Ryo Iizuka

http://nyatla.jp/nyartoolkit/  
airmail(at)ebony.plala.or.jp  
wm(at)nyatla.jp  


## NyARToolkit for Processing

* NyARToolkit for proce55ing is a useful wrapper of [NyARToolkit](http://nyatla.jp/nyartoolkit/) for Processing.
* Based on NyARToolkit which is Augmented reality library.
* Acceptable source images are standard PImage or result of camera() function.
* Supported rendering system are OpenGL and PV3D.
* This version operated on Processing 2.2.0. (Use old version in case of use old version processing.)

## Features of NyARToolkit for proce55ing
* Left and Right projection matrix.
* Multimarker/Single marker usecase.
* Supported NyIdMarker, ARToolkit format Marker.
* Auto thresholed detection.
* Easy to convert 2D(Screen) and 3D coordinates.
* only use pure processing APIs. environment free.

## Setup

1. Download NyARToolkit for proce55ing.
[http://processing.org/download/index.html](http://processing.org/download/index.html)  

2. Prepare Camera system for Processing.It can be check the operation of camera system by running the sample program of processing.

## Run first NyARToolkit sketch.

1. Print AR marker to the paper. PDF file is located at data directory. 
2. Open processing sketch simpleLite.pde
3. Shoot the marker at the camera. Cube will appear in the captured image.


## Classes

* MultiMarker class (MultiMarker.java)  
This class can handle some NyId marker and AR marker at the same time.

* NyARBorad class(NyARBoard.java)  
Old version. Single AR marker. This class is for compatibility.
* SingleARTKMarker class (SingleARTKMarker.java)  
Old version. Single AR marker. This class is for compatibility.
* SingleNyIdMarker class (SingleNyIdMarker.java)  
Old version. Single AR marker. This class is for compatibility.

## Other

* Camera caliblarion
NyARToolkit supports 2 Camera calibration file format. ARToolkit format and OpenCV format.
ARToolkit can not create the files. It is necessary to create by tools of ARToolkit or OpenCV .


## Licence

* NyARToolkit for Processing is provided by LGPLv3.
* Source code of NyARToolkit for Java can be download from https://github.com/nyatla/NyARToolkit/
