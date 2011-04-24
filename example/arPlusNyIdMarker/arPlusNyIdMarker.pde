/**
  NyARToolkit for proce55ing/1.0.0
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  ARマーカとIdマーカを同時に使う例です。
  ARマーカはkanji,hiro、idマーカは0,1番のマーカを使う事ができます。
*/
import processing.video.*;
import processing.core.*;
import jp.nyatla.nyar4psg.*;

Capture cam;
MultiMarker nya;
PFont font=createFont("FFScala", 32);

void setup() {
  size(640,480,P3D);
  colorMode(RGB, 100);
  cam=new Capture(this,640,480);
  nya=new MultiMarker(this,width,height,"camera_para.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addARMarker("patt.hiro",80);//id=0
  nya.addARMarker("patt.kanji",80);//id=1
  nya.addNyIdMarker(0,80);//id=2
  nya.addNyIdMarker(1,80);//id=3
}

void draw()
{
  if (cam.available() !=true) {
      return;
  }
  cam.read();
  nya.detect(cam);
  hint(DISABLE_DEPTH_TEST);
  image(cam,0,0);
  hint(ENABLE_DEPTH_TEST);
  for(int i=0;i<4;i++){
    if((!nya.isExistMarker(i))){
      continue;
    }
    nya.beginTransform(i);
    fill(100*(((i+1)/4)%2),100*(((i+1)/2)%2),100*(((i+1))%2));
    translate(0,0,20);
    box(40);
    nya.endTransform();
  }
}

