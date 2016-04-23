[English here!](README.EN.md "")

# NyARToolkit for proce55ing

Copyright (C)2008-2016 Ryo Iizuka

http://nyatla.jp/nyartoolkit/  
airmail(at)ebony.plala.or.jp  
wm(at)nyatla.jp  



## NyARToolkit for Processing

 * NyARToolkit for proce55ingは、processing環境下でNyARToolkitを利用するためのライブラリです。
 * 拡張現実感ライブラリには、ARToolKitの派生ライブラリNyARToolKit for Javaを使用しています。
 * Processing version 2.2.1,3.0.2での動作を確認しています。 (1.xには対応していません。)
 * このライブラリは、processingのcamera()関数で取り込んだ画像や、PImage画像を元にマーカ検出処理を実行できます。
 * レンダリングシステムには、PV3Dをサポートします。

## NyARToolkit for proce55ingの特徴

 * 左手系・右手系両方の座標系をサポートします。
 * NyIdマーカ、ARToolKitマーカ、NFTターゲット(ARToolKit5仕様)に対応します。
 * パターン取得、スクリーン座標のマーカ座標変換等が手軽に使えます。
 * レンダリングにProcessingCoreAPIのみを使います。Graphics3D派生オブジェクト全てに対応します。

## 環境の準備


1. NyARToolkit for proce55ingの実行には、processing/2.2.1、又は3.0.1以上が必要です。Processingウェブサイトからダウンロードして下さい。http://processing.org/download/index.html
  
2. ProcessingのCapture機能を使用できるようにコンピュータを設定してください。Capture機能は、ProcessingのExamples>Libraries>Video(Capture)以下のサンプルの動作で確認できます。（このステップは、キャプチャ機能を使わないときには必要ありません。）

3. ProcessingからOpenGL機能を使用できるように、コンピュータの設定をして下さい。OpenGL機能はProcessingのExamples>Libraries>OpenGL以下のサンプルの動作で確認できます。（このステップは、レンダリングにP3Dを使用するときには必要ありません。）

4. 以上で準備は完了です。


## サンプル実行(ARマーカ)

exampleにある、simpleLiteのサンプルの実行手順です。simpleLiteは、Hiroマーカの上に立方体を表示するシンプルなプログラムです。

1. dataディレクトリにある、pattHiro.pdfを印刷しておいてきます。
 
2. example/simpleLiteにある、simpleLite.pdeを開いて実行してください。マーカを撮影すると、そこに立方体が表示されるはずです。


## サンプル実行(NFT)

exampleにある、simpleNftのサンプルの実行手順です。simpleNftは、画像の上に立方体を表示するシンプルなプログラムです。

1. dataディレクトリにある、infinitycat.pdfを印刷しておいてきます。
 
2. example/simpleNftにある、simpleNft.pdeを開いて実行してください。マーカを撮影すると、そこに立方体が表示されるはずです。


## NyARToolkit for Processingの提供するクラス

NyARToolkit for proce55ingの提供するクラスを説明します。
 
* MultiMarkerクラス (MultiMarker.java)
ARマーカ、NyIdマーカを取り扱うクラスです。複数個同時に使うことができます。

* MultiNftクラス (MultiNft.java)
NFTターゲットを取り扱うクラスです。複数個を同時に使うことができます。


## その他

* パターンファイルやカメラパラメータファイルの互換性について
NyARToolKit for Processingの使用するカメラパラメータファイル、パターンファイルと互換性があります。
* ARマーカの作り方
PNG/JPEG画像をそのまま使用することができます。ARToolKitフォーマットのファイルを使う場合は、外部ツールで作成してください。
* NFTターゲットファイルセットの作り方
スケッチ"nftFileGen"を使うことができます。スケッチを起動するとツールが立ち上がります。Jpegなどの画像からターゲットファイルセットを作ることができます。
* Processing 2.2.1での使用方法
MultiMarker/MultiNftのコンフィギュレーションパラメータに、NyAR4PsgConfig.CONFIG_PSG_PV221を使います。カスタムコンフィギュレーションを使う場合は、バージョンパラメータにNyAR4PsgConfig.PV_221を指定してコンフィギュレーションを作成します。

## ライセンス

* LGPLv3での提供になります。但し、src以下のファイルをのみを使用する場合には、MITライセンスでも使用できます。
* パッケージに含まれるNyARToolkitのソースファイルは、 [https://github.com/nyatla/NyARToolkit/](https://github.com/nyatla/NyARToolkit/ "")よりダウンロードできます。
