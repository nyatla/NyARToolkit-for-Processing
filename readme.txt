NyARToolkit for proce55ing
Copyright (C)2008-2010 R.Iizuka

version 0.3.0

http://nyatla.jp/
airmail(at)ebony.plala.or.jp
--------------------------------------------------
・NyARToolkit for proce55ing

　NyARToolkit for proce55ingは、processing環境下で拡張現実環境を
　利用するためのライブラリです。拡張現実ライブラリには、ARToolKit
　の派生ライブラリNyARToolKit for Javaを使用しています。

　このライブラリは、processingの標準キャプチャクラスCaptureで取り込んだ
　映像からARToolKitの変換行列を計算し、それをOpenGLに設定する手段を提供
　します。

　入力画像には任意のPImageを使用できるので、たとえば動画、静止画の解析も
　可能だと思われます。


・準備
　NyARToolkit for proce55ingの実行には、processing/1.0以上が必要です。
　サイトからダウンロードして下さい。
　http://processing.org/download/index.html

　次に、Capture機能とOpenGL機能を使用できるように、コンピュータの設定をして下さい。

　Capture機能は、ProcessingのExamples>Libraries>Video(Capture)以下のサンプルが
　動作すれば、大丈夫です。

　OpenGL機能は、ProcessingのExamples>Libraries>OpenGL以下のサンプルが動作すれば
　大丈夫です。


・サンプルの実行
　1.example/NyARTest/dataディレクトリにある、pattHiro.pdfを印刷しておいてください。
　　これがマーカになります。
　2.example/NyARTestにある、NyARTest/NyARTest.pdeを開いて実行してください。
　　マーカを撮影すると、そこに立方体が表示されるはずです。


・独自スケッチの作り方
　1.空のスケッチを作り、NyAR2.jarをSketch>Add File...から追加します。jarファイルは、
　　example/NyARTest/code以下にあります。
　2.スケッチのディレクトリにdataディレクトリを作り、そこにexample/NyARTest/data以下
　　にあるパターンファイル(patt.hiro)とカメラパラメータファイル(camera_para.dat)を
　　コピーします。
　3.ファイルが足りないと実行時にエラーが出るので、適時修正してください。



・提供するクラス
　NyARToolkit for proce55ingは、表示方法をいくつかのパターンでモデル化します。
　現在は、マーカを表示用の板に見立てる、NyARBoradクラスのみが実装されています。

　・NyARBoradクラス（NyARBoard.java）
　１種類のマーカを同時に１個認識するクラスです。PImage形式の画像入力から、１個の
　マーカを検出し、変換行列の計算する機能を持ちます。データを単純に映像と合成する時に
　便利です。

　・SingleARTKMarkerクラス（SingleARTKMarker.java）
　複数種類のマーカを同時に１個認識するクラスです。PImage形式の画像入力から、複数の
　種類のマーカを用意して、それぞれのマーカに違うオブジェクトを出すユースケースに
　使用できます。NyARBoardの機能に加え、自動式位置調整や簡易トラッキング機能が使えます。

　・SingleNyIdMarkerクラス（SingleNyIdMarker.java）
　複数種類のIdマーカを同時に１個認識するクラスです。複数の種類のマーカを用意して、
　それぞれのマーカに違うオブジェクトを出すユースケースに使用できます。
　SingleARTKMarkerと比べて、たくさんの種類のマーカを扱える特徴があります。
　Idの仕様は、NyId形式です。


・その他
　カメラパラメータファイル、パターンファイルは、ARToolKitのそれと完全な互換性が
　あります。このライブラリにはこれらを作成する機能がありませんので、ARToolKit、または
　FLARToolKitの機能を利用してください。なお、マーカパターンは16x16で作る必要があります。

　Windows環境下では、arc@dmzさん作のDirectShow Javaベースのキャプチャライブラリ
　CaptureDSを使用することをお勧めします。
　このライブラリは、digitalmuseum http://digitalmuseum.jp/software/nui/processing/
　よりダウンロードできます。


・ライセンス
　src以下のファイルはMITライセンスですが、NyARToolkitがGPLv3ライセンスのため、
　jarファイルのライセンスはGPLv3になります。

　NyARToolkitのソースファイルは、http://sourceforge.jp/projects/nyartoolkit/
　よりダウンロードできます。


・謝辞
　ARToolkitを開発された加藤博一先生と、Human Interface Technology Lab
　に感謝します。
　http://www.hitl.washington.edu/artoolkit/

　Processingを開発されたCasey Reas氏と Benjamin Fry氏に感謝します。
　http://processing.org/

　CaptureDSを開発されたarc@dmz氏に感謝します。
　http://digitalmuseum.jp/

　Processingの座標系問題を解決して頂いた、reco氏に感謝します。
　http://www.hyde-ysd.com/reco-memo/
