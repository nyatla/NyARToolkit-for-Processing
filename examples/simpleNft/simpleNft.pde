/**
 * NyARToolkit for proce55ing/3.0.5
 * (c)2008-2017 nyatla
 * airmail(at)ebony.plala.or.jp
 * 
 * This sketch handles a NFT target.
 * The NFT target files are infinitycat.*.
 * Any pattern and configuration files are found in libraries/nyar4psg/data inside your sketchbook folder. 
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
  nya=new MultiNft(this,width,height,"../../data/camera_para5.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addNftTarget("../../data/infinitycat",160);//id=0
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