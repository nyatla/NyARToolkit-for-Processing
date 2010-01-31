/**	NyARToolkit for proce55ing/0.3.0
	(c)2008-2010 nyatla
	airmail(at)ebony.plala.or.jp
*/
 
import processing.video.*;
import jp.nyatla.nyar4psg.SingleNyIdMarker;
import processing.opengl.*;
import javax.media.opengl.*;

Capture cam;
SingleNyIdMarker nya;
PFont font;


void setup() {
  size(640,480,OPENGL);
  colorMode(RGB, 100);
  font=createFont("FFScala", 32);
  //キャプチャを作成
  cam=new Capture(this,width,height);
  //平面検出クラスを作成
  //Left hand projection matrix
  nya=new SingleNyIdMarker(this,width,height,"camera_para.dat",SingleNyIdMarker.CS_LEFT);
  nya.setIdMarkerSize(80);
  println(nya.VERSION);
}
//この関数は、マーカ頂点の情報を描画します。
void drawMarkerPos(int[][] points)
{
  textFont(font,10.0);
  stroke(100,0,0);
  fill(100,0,0);
  for(int i=0;i<4;i++){
    ellipse(nya.pos2d[i][0], nya.pos2d[i][1],5,5);
  }
  fill(0,0,0);
  for(int i=0;i<4;i++){
    text("("+nya.pos2d[i][0]+","+nya.pos2d[i][1]+")",nya.pos2d[i][0],nya.pos2d[i][1]);
  }
}

String angle2text(float a)
{
  int i=(int)degrees(a);
  i=(i>0?i:i+360);
  return (i<100?"  ":i<10?" ":"")+Integer.toString(i);
}
String trans2text(float i)
{
  return (i<100?"  ":i<10?" ":"")+Integer.toString((int)i);
}

void draw() {
  background(255);
  if (cam.available() !=true) {
    return;
  }
  cam.read();
  //背景を描画
  hint(DISABLE_DEPTH_TEST);
  image(cam,0,0);
  hint(ENABLE_DEPTH_TEST);
  
  //detect結果で処理を分ける。
  switch(nya.detect(cam)){
  case SingleNyIdMarker.ST_NOMARKER:
    //マーカ見つかんない
    break;
  case SingleNyIdMarker.ST_NEWMARKER:
    //マーカが見つかったらしい
    println("Marker appeared. #"+nya.markerid);
    break;
  case SingleNyIdMarker.ST_UPDATEMARKER:
    //マーカの位置を更新中・・・
    
    hint(DISABLE_DEPTH_TEST);
    //マーカの位置を描画
    drawMarkerPos(nya.pos2d);
    hint(ENABLE_DEPTH_TEST);
    
    //キューブを描画
    PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
    nya.beginTransform(pgl);//マーカ座標系での描画を開始する。
    //ここからマーカ座標系
    stroke(255,200,0);
    translate(0,0,20);
    if(nya.markerid==0){fill(255,0,0);}else if(nya.markerid==1){fill(0,0,255);}
    box(40);
    nya.endTransform();//マーカ座標系での描画を終了する。（必ず呼んで！）
    break;
  case SingleARTKMarker.ST_REMOVEMARKER:
    //マーカなくなったでござる。
    println("Marker disappeared.");
    break;
  }
}

