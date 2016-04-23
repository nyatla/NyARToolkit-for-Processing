# NyARToolkit for proce55ing

Copyright (C)2008-2016 Ryo Iizuka

http://nyatla.jp/nyartoolkit/  
airmail(at)ebony.plala.or.jp  
wm(at)nyatla.jp  


## NyARToolkit for Processing

* NyARToolkit for proce55ing is a useful wrapper of [NyARToolkit](http://nyatla.jp/nyartoolkit/) for Processing.
* The library can handle NyIdMarker, ARToolkit format Marker, NFT target.
* Supported platform are processing 2.2.1/3.0.2. ( 
* Based on NyARToolkit which is Augmented reality library.


## Features of NyARToolkit for proce55ing
* Simple API - applications can be implemented in a short code.
* Flexible coordinate system - you can use both of Processing or OpenGL coordinate system. Also it has access API to the captured image.
* Platform free - The library is implemented with Java and processing core API. Anywhere will work.

## Setup

1. Download NyARToolkit for proce55ing.[https://github.com/nyatla/NyARToolkit-for-Processing/releases](https://github.com/nyatla/NyARToolkit-for-Processing/releases)  

2. Download Processing.[http://processing.org/download/index.html](http://processing.org/download/index.html)  

2. Prepare Camera system for Processing.It can be check the operation of camera system by running the sample program of processing.

## Run first NyARToolkit sketch(ARMarker).

1. Print AR marker(pattHiro.pdf) to the paper. PDF file is located at data directory. 
2. Open processing sketch simpleLite.pde
3. Take the marker in the camera. Cube will appear in the screen.

## Run second NyARToolkit sketch(NFT).

1. Print AR marker(infinitycat.pdf) to the paper. PDF file is located at data directory. 
2. Open processing sketch simpleNft.pde
3. Take the image in the camera. Cube will appear in the screen.

## Classes

* MultiMarker class (MultiMarker.java)  
This class can handle some NyId marker and AR marker at the same time.

* MultiNft class (MultiNft.java)  
This class can handle some NFT targets.

## Other

* Files compatibility with ARToolKit  
NyARToolkit can read all files of ARToolKit5.
* Camera caliblarion  
NyARToolkit supports 2 Camera calibration file format. ARToolkit format and OpenCV format.
ARToolkit can not create the files. It is necessary to create by tools of ARToolkit or OpenCV .
* How to make your AR marker  
NyARToolKit can use anyimage file as AR-Marker. You can use the ARToolKit format patt, but NyARToolkit has not generator.
* How to make your NFT target  
You can use the sketch "nftFileGen". It can make NFT target files from an image file.


## Licence

* NyARToolkit for Processing is provided by LGPLv3.
* Source code of NyARToolkit for Java can be download from https://github.com/nyatla/NyARToolkit/
