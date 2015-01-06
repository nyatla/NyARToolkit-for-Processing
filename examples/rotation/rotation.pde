/**
  NyARToolkit for proce55ing/1.0.0
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  
  人マーカの上に右手系、Hiroマーカの上に左手系の立方体を表示します。
  
  This sample program shows rotation of 2 coordinate system.(left and right).
  The marker is "patt.hiro" and "patt.kanji"
*/
 
import processing.video.*;
import jp.nyatla.nyar4psg.*;

Capture cam;
MultiMarker nya_r;
MultiMarker nya_l;
PFont font=createFont("FFScala", 32);

void setup() {
  size(640,480,P3D);
  colorMode(RGB, 100);
  println(MultiMarker.VERSION);
  
  //キャプチャを作成
  cam=new Capture(this,640,480);
  nya_l=new MultiMarker(this,width,height,"camera_para.dat",new NyAR4PsgConfig(NyAR4PsgConfig.CS_LEFT_HAND,NyAR4PsgConfig.TM_NYARTK));
  nya_l.addARMarker("patt.hiro",80);
  
  nya_r=new MultiMarker(this,width,height,"camera_para.dat",new NyAR4PsgConfig(NyAR4PsgConfig.CS_RIGHT_HAND,NyAR4PsgConfig.TM_NYARTK));
  nya_r.addARMarker("patt.kanji",80);
  cam.start();
}

int c=0;
void drawgrid()
{
  pushMatrix();
  stroke(0);
  strokeWeight(2);
  line(0,0,0,100,0,0);
  textFont(font,20.0); text("X",100,0,0);
  line(0,0,0,0,100,0);
  textFont(font,20.0); text("Y",0,100,0);
  line(0,0,0,0,0,100);
  textFont(font,20.0); text("Z",0,0,100);
  popMatrix();
}
void draw()
{
  c++;
  if (cam.available() !=true) {
      return;
  }
  cam.read();
  nya_r.detect(cam);
  nya_l.detect(cam);
  background(0);
  nya_r.drawBackground(cam);//frustumを考慮した背景描画

  //right
  if((nya_r.isExistMarker(0))){
    nya_r.beginTransform(0);
    fill(0,0,255);
    drawgrid();
    translate(0,0,20);
    rotate((float)c/100);
    box(40);
    nya_r.endTransform();
  }
  //left
  if((nya_l.isExistMarker(0))){
    nya_l.beginTransform(0);
    fill(0,255,0);
    drawgrid();
    translate(0,0,20);
    rotate((float)c/100);
    box(40);
    nya_l.endTransform();
  }
}




