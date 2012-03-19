/**
  NyARToolkit for proce55ing/1.0.0
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  
  begin-endシーケンスを使わない方法です。processingの行列を直接操作して、
  マーカ座標系を反映します。
  
  This sample is not using "begin-end" sequence.
  Projection matrix and Modelview matrix are set to processing directly.
*/
import processing.video.*;
import jp.nyatla.nyar4psg.*;

Capture cam;
MultiMarker nya;
int cr,cg,cb;

void setup() {
  size(640,480,P3D);
  colorMode(RGB, 100);
  println(MultiMarker.VERSION);
  nya=new MultiMarker(this,width,height,"camera_para.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addARMarker("patt.hiro",80);//id=0
  nya.setARPerspective();
  cam=new Capture(this,640,480);
  cr=cg=cb=100;
}

int c=0;
void draw()
{
  c++;
  if (cam.available() !=true) {
      return;
  }
  cam.read();
  nya.detect(cam);
  background(0);
  nya.drawBackground(cam);//frustumを考慮した背景描画
  if(!nya.isExistMarker(0)){
    return;
  }
  nya.setARPerspective();//NyAR projection
  pushMatrix();
  setMatrix(nya.getMarkerMatrix(0)); //load Marker matrix
  fill(cr,cg,cb);
  translate(0,0,20);
  box(40);
  popMatrix();
}
