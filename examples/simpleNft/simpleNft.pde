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
MultiNft nya;

void setup() {
  size(640,480,P3D);
  colorMode(RGB, 100);
  println(MultiMarker.VERSION);
  cam=new Capture(this,640,480);
  nya=new MultiNft(this,width,height,"camera_para5.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addNftTarget("infinitycat",160);//id=0
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
    if(!nya.isExist(0)){
      return;
    }
    nya.beginTransform(0);
    fill(255,0,0);
    translate(-80,55,20);
    box(40);
    nya.endTransform();
  
}