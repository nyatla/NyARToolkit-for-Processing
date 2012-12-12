/**
  NyARToolkit for proce55ing/1.2.0
  (c)2008-2012 nyatla
  airmail(at)ebony.plala.or.jp
  
  マーカファイルの変わりにPNGを使います。
  PNGは任意解像度の正方形である必要があります。
  エッジ部分のパターンは含めてください。
  
  This sample program uses a PNG image instead of the patt file.
  The PNG image must be square form that includes edge.
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
  nya=new MultiMarker(this,width,height,"camera_para.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addARMarker(loadImage("hiro.png"),16,25,80);
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
  if((!nya.isExistMarker(0))){
    return;
  }
  nya.beginTransform(0);
  fill(0,0,255);
  translate(0,0,20);
  box(40);
  nya.endTransform();
}


