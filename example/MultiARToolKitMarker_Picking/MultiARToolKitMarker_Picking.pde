/**
  NyARToolkit for proce55ing/0.4.1
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  
  このプログラムは、ピッキングの実験プログラムです。
  Hiroマーカと同じ平面上に、３Dモデルを表示しマウス。

*/
 
import processing.video.*;
import processing.core.*;
import jp.nyatla.nyar4psg.*;
import processing.opengl.*;
import javax.media.opengl.*;

Capture cam;
MultiARTookitMarker nya;
PFont font;

void setup() {
  size(640,480,OPENGL);
  colorMode(RGB, 100);
  font=createFont("FFScala", 32);
  //キャプチャを作成
  cam=new Capture(this,640,480);
  //左手系で構築(エッジサイズ=25、threshold=100のデフォルト値)
  //thresholdはオートを入れるかも？
//  nya=new MultiARTookitMarker(this,width,height,"camera_para.dat",16,MultiARTookitMarker.CS_RIGHT_HAND);
  nya=new MultiARTookitMarker(this,width,height,"camera_para.dat",16,MultiARTookitMarker.CS_LEFT_HAND);

  //id0にマーカ登録
  nya.addMarker("patt.hiro",80);
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
  background(255);
  if (cam.available() !=true) {
      return;
  }
  cam.read();
  nya.detect(cam);
  //背景を描画
  hint(DISABLE_DEPTH_TEST);
  image(cam,0,0);
  //情報描画
  hint(ENABLE_DEPTH_TEST);
  //ARToolKit Projectionの開始
  nya.beginARTKProjection((PGraphicsOpenGL)g);

  //1個しか登録してないから、1個ループ
    if(!(nya.isExistMarker(0))){
      //nothing
    }else{
      textFont(font,20.0); text(0+":"+(nya.isExistMarker(0)?"enable":"disable")+" "+nya.getConfidence(0),5,20*(0+1));
      setMatrix(nya.allocMarkerMatrix(0));
      //スクリーン座標をマーカ平面の座標に変換する。
      PVector pv=nya.screen2MarkerCoordSystem(0,mouseX, mouseY);
      //移動
      translate(pv.x,pv.y,pv.z);
      //グリッド書いて
      drawgrid();
      //角軸上で回転する四角形。
      pushMatrix();
      translate(c%100,0,0);
      rotateX((float)c/100);
      textFont(font,20.0); text("X box",0,0,0);
      box(20);
      popMatrix();
      pushMatrix();
      translate(0,c%100,0);
      rotateY((float)c/100);
      textFont(font,20.0); text("Y box",0,0,0);
      box(20);
      popMatrix();
      pushMatrix();
      translate(0,0,c%100);
      rotateZ((float)c/100);
      textFont(font,20.0); text("Z box",0,0,0);
      box(20);
      popMatrix();
  }
  nya.endARTKProjection();
}



