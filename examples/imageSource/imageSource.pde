//
//  This sketch preview a cube on PNG file.
//  nyAR4psg can handle any PImage object!
//

import processing.video.*;
import jp.nyatla.nyar4psg.*;

PImage img;
MultiMarker nya;

void setup() {
  size(640,480,P3D);
  colorMode(RGB, 100);
  println(MultiMarker.VERSION);
  img=loadImage("../../data/320x240ABGR.png");
  nya=new MultiMarker(this,320,240,"../../data/camera_para.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addARMarker("../../data/patt.hiro",80);
}

void draw()
{
  nya.detect(img);
  background(0);
  nya.drawBackground(img);
  if((!nya.isExist(0))){
    return;
  }
  nya.beginTransform(0);
  fill(0,0,255);
  translate(0,0,20);
  box(40);
  nya.endTransform();
}