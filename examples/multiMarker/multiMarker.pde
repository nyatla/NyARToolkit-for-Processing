/**
  NyARToolkit for proce55ing/1.0.0
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  
  複数のARマーカを扱う例です。Hiroマーカと、Kanjiマーカを用意して下さい。
  
  This sample proglam handles 2 ARToolKit marker.
  The markers are "patt.hiro" and "patt.kanji"
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
  nya.addARMarker("patt.hiro",80);//id=0
  nya.addARMarker("patt.kanji",80);//id=1
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
  for(int i=0;i<2;i++){
    if((!nya.isExistMarker(i))){
      continue;
    }
    nya.beginTransform(i);
    fill(0,100*(i%2),100*((i+1)%2));
    translate(0,0,20);
    box(40);
    nya.endTransform();
  }
}

