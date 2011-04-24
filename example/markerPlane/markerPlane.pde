/**
  NyARToolkit for proce55ing/1.0.0
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  スクリーン座標をマーカ座標に変換できます。
  Hiroマーカを使います。マーカが認識されたら、マウスカーソルを移動させてみて下さい。
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
  hint(DISABLE_DEPTH_TEST);
  image(cam,0,0);
  hint(ENABLE_DEPTH_TEST);
  if((!nya.isExistMarker(0))){
    return;
  }
  PVector p=nya.screen2MarkerCoordSystem(0,mouseX,mouseY);
  nya.beginTransform(0);
  noFill();
  stroke(100,0,0);
  rect(-40,-40,80,80);
  stroke(100,100,0);
  ellipse((int)p.x,(int)p.y,20-c%20,20-c%20);
  nya.endTransform();
}
