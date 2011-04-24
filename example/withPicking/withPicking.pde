/**
  NyARToolkit for proce55ing/1.0.0
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  Pickingと一緒に使うために、NyAR4Psgをbegin-endシーケンス以外の方法で使います。

*/
import processing.video.*;
import processing.core.*;
import jp.nyatla.nyar4psg.*;
import processing.opengl.*;
import picking.*;

Capture cam;
MultiMarker nya;
int cr,cg,cb;
PFont font=createFont("FFScala", 32);
Picker picker;

void setup() {
  println(MultiMarker.VERSION);
  size(640,480,P3D);
  colorMode(RGB, 100);
  nya=new MultiMarker(this,width,height,"camera_para.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addARMarker("patt.hiro",80);//id=0
  picker = new Picker(this);
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
  perspective();
  hint(DISABLE_DEPTH_TEST);
  image(cam,0,0);
  hint(ENABLE_DEPTH_TEST);
  //復帰
  if(!nya.isExistMarker(0)){
    return;
  }
  nya.setARPerspective();
  picker.start(0);
  pushMatrix();
  setMatrix(nya.getMarkerMatrix(0));
  fill(cr,cg,cb);
  translate(0,0,20);
  box(40);
  popMatrix();
}

void mouseClicked() {
  int id = picker.get(mouseX, mouseY);
  if (id ==0) {
    cr=int(random(0,100));
    cg=int(random(0,100));
    cb=int(random(0,100));
  }
}

