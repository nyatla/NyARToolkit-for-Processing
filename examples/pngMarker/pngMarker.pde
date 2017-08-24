/**
 * NyARToolkit for proce55ing/3.0.5
 * (c)2008-2017 nyatla
 * airmail(at)ebony.plala.or.jp
 * 
 * マーカファイルの変わりにPNGを使います。
 * PNGは任意解像度の正方形である必要があります。
 * PNG画像にはエッジ部分を含めてください。
 * 全ての設定ファイルとマーカファイルはスケッチディレクトリのlibraries/nyar4psg/dataにあります。
 * 
 * This sketch uses a PNG image instead of the standard patt file.
 * The PNG image must be square form that includes edge.
 * Any pattern and configuration files are found in libraries/nyar4psg/data inside your sketchbook folder. 
 */
import processing.video.*;
import jp.nyatla.nyar4psg.*;

Capture cam;
MultiMarker nya;

void setup() {
  size(640,480,P3D);
  colorMode(RGB, 100);
  println(MultiMarker.VERSION);
  cam=new Capture(this,640,480);
  nya=new MultiMarker(this,width,height,"../../data/camera_para.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addARMarker(loadImage("../../data/hiro.png"),16,25,80);
  cam.start();
}

void draw()
{
  if (cam.available() !=true) {
      return;
  }
  cam.read();
  nya.detect(cam);
  background(0);
  nya.drawBackground(cam);//frustumを考慮した背景描画
  if((!nya.isExist(0))){
    return;
  }
  nya.beginTransform(0);
  fill(0,0,255);
  translate(0,0,20);
  box(40);
  nya.endTransform();
}