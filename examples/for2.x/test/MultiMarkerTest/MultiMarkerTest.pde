/*
  MultiMarker class function test program

*/
import processing.video.*;
import jp.nyatla.nyar4psg.*;
import processing.opengl.*;

PFont font=createFont("FFScala", 32);
Capture cam;
MultiMarker nya;

void setup() {
  size(640,480,P3D);
  colorMode(RGB, 100);
  cam=new Capture(this,width,height);
  nya=new MultiMarker(this,width,height,"camera_para.dat",new NyAR4PsgConfig(NyAR4PsgConfig.CS_RIGHT_HAND,NyAR4PsgConfig.TM_NYARTK));
  nya.setARClipping(100,1000);
  nya.setConfidenceThreshold(0.6);
  nya.addARMarker("patt.hiro",80);
  nya.addARMarker("patt.kanji",80);
  nya.addNyIdMarker(31,80);
  println(nya.VERSION); //バージョンの表示
  cam.start();
}
int c=0;
void draw() {
  c++;
  if (cam.available() !=true) {
    return;
  }
  background(255);
  cam.read();
  //バックグラウンドを描画
  nya.drawBackground(cam);
  nya.detect(cam);
  if(nya.isExistMarker(0)){
    nya.beginTransform(0);//マーカ座標系に設定
    {
      drawBox(255,0,0);
      drawMarkerXYPos(0);
    }
    nya.endTransform();  //マーカ座標系を終了
    drawMarkerPatt(0);
    drawVertex(0);
    //マーカのスクリーン座標を中心に円を書く
    PVector p=nya.marker2ScreenCoordSystem(0,0,0,0);
    noFill();
    ellipse(p.x,p.y,200,200);
  }
  if(nya.isExistMarker(1)){
    nya.beginTransform(1);//マーカ座標系に設定
    {
      drawBox(0,255,0);
      drawMarkerXYPos(1);
    }
    nya.endTransform();  //マーカ座標系を終了
    drawMarkerPatt(1);
    drawVertex(1);
  }
  if(nya.isExistMarker(2)){
    nya.beginTransform(2);//マーカ座標系に設定
    {
      drawBox(0,0,255);
      drawMarkerXYPos(2);
      println(nya.getLife(2));
    }
    nya.endTransform();  //マーカ座標系を終了
    drawMarkerPatt(2);
    drawVertex(2);
  }
  
}
void drawBox(int ir,int ig,int ib)
{
  pushMatrix();
  drawgrid();
  fill(ir,ig,ib);
  stroke(255,200,0);
  translate(0,0,20);
  box(40);
  noFill();
  translate(0,0,-20);
  rect(-40,-40,80,80); 
  popMatrix();
}

//この関数は、マーカパターンを描画します。
void drawMarkerPatt(int id)
{
  PImage p=nya.pickupMarkerImage(id,
    40,40,
    -40,40,
    -40,-40,
    40,-40,
    100,100);
//  PImage p=nya.pickupRectMarkerImage(id,-40,-40,80,80,100,100);
  image(p,id*100,0);
}

//この関数は、マーカ平面上の点を描画します。
void drawMarkerXYPos(int id)
{
  pushMatrix();
    PVector pos=nya.screen2MarkerCoordSystem(id,mouseX,mouseY);
    translate(pos.x,pos.y,0);
    noFill();
    stroke(0,0,100);
    ellipse(0,0,20-c%20,20-c%20);
  popMatrix();
}

//この関数は、マーカ頂点の情報を描画します。
void drawVertex(int id)
{
  PVector[] i_v=nya.getMarkerVertex2D(id);
  textFont(font,10.0);
  stroke(100,0,0);
  for(int i=0;i<4;i++){
    fill(100,0,0);
    ellipse(i_v[i].x,i_v[i].y,6,6);
    fill(0,0,0);
    text("("+i_v[i].x+","+i_v[i].y+")",i_v[i].x,i_v[i].y);
  }
}

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

