/**
  NyARToolkit for proce55ing/1.0.0
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  
  ARマーカとIdマーカを同時に使う例です。
  ARマーカはkanji,hiro、idマーカは0,1番のマーカを使う事ができます。
  
  This sample handles 2 ARToolkit style markers and 2 NyId markers at same time.
  The ARToolKit marker files are kanji.patt and hiro.patt. NyId marker ids are #0 and #1.
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
  nya.addNyIdMarker(0,80);//id=2
  nya.addNyIdMarker(1,80);//id=3
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


