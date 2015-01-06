/**
  NyARToolkit for proce55ing/1.0.0
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  
  Pickingと一緒に使うサンプルです。
  Picking Libraryはこちらからダウンロードしてください。
  http://code.google.com/p/processing-picking-library/
  Picking Libraryは version 0.1.5で動作を確認しています。
  0.1.6ではうまく動作しません。
  ----
  This sample program is sample with picking library.
  The cube can be clicked.
  The marker file is "patt.hiro".  
  Download picking library from   http://code.google.com/p/processing-picking-library/
  Should use version 0.1.5.  Version 0.1.6 may does not work.

*/
import processing.video.*;
import jp.nyatla.nyar4psg.*;
import processing.opengl.*;
import picking.*;

Capture cam;
MultiMarker nya;
int cr,cg,cb;
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
  cam.start();
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
  if(!nya.isExistMarker(0)){
    return;
  }
  picker.start(0);
  nya.beginTransform(0);
  fill(cr,cg,cb);
  translate(0,0,20);
  box(40);
  nya.endTransform();
}

void mouseClicked() {
  int id = picker.get(mouseX, mouseY);
  if (id ==0) {
    cr=int(random(0,100));
    cg=int(random(0,100));
    cb=int(random(0,100));
  }
}


