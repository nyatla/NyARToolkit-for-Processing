/**	NyARToolkit for proce55ing/0.3.0
	(c)2008-2010 nyatla
	airmail(at)ebony.plala.or.jp
*/
 
import processing.video.*;
import jp.nyatla.nyar4psg.*;
import processing.opengl.*;
import javax.media.opengl.*;

Capture cam;
NyARBoard nya;
PFont font;



void setup() {
  size(640,480,OPENGL);
  colorMode(RGB, 100);
  font=createFont("FFScala", 32);
  //キャプチャを作成
  cam=new Capture(this,width,height);
  //平面検出クラスを作成
  //Left hand projection matrix
  nya=new NyARBoard(this,width,height,"camera_para.dat","patt.hiro",80);
  print(nya.VERSION);
  //Right hand projection matrix
  //nya=new NyARBoard(this,width,height,"camera_para.dat","patt.hiro",80,NyARBoard.CS_RIGHT);

  //各種プロパティ設定（必要に応じて設定すること。何もしないとデフォルト値が入力される。）
  nya.gsThreshold=120;//画像２値化の閾値(0<n<255) default=110
  nya.cfThreshold=0.4;//変換行列計算を行うマーカ一致度(0.0<n<1.0) default=0.4
  //nya.lostDelay=10;//マーカ消失を無視する回数(0<n) default=10

  /*  他に、読み出しプロパティとして以下のものがある。
  　  これらはdetect関数がtrueを返した時に有効になる。
    double confidence
      マーカの一致度。(0<n<1.0)
    int lostCount
      マーカ認識後に消失した時に加算されるカウンタ。マーカの遅延消失に使用する。
    int pos2d[4][2]
      検出したマーカの画面上の頂点座標×4個
    PVector angle
      マーカの軸を中心にした回転角度(x,y,z)単位はラジアン
    PVector trans
      マーカの中心を基準とした平行移動量。単位はmm
    double transmat[12]
      OpenGLに指定するマーカの変換行列。
      行列から値を逆算する時、beginTransformを使用せずに自分で行列操作を行う時に使用してください。
    double projection[16]
      OpenGLに指定するProjection行列。
      beginTransformを使用せずに自分で行列操作を行う時に使用してください。
  */
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

  //マーカの検出。マーカが発見されるとdetectはTRUEを返す。
  if(nya.detect(cam)){
    hint(DISABLE_DEPTH_TEST);
    //一致度を書く
    textFont(font,25.0);
    fill((int)((1.0-nya.confidence)*100),(int)(nya.confidence*100),0);
    text((int)(nya.confidence*100)+"%",width-60,height-20);
    //マーカの角度、水平位置等
    pushMatrix();
    textFont(font,10.0);
    fill(0,100,0,80);
    translate((nya.pos2d[0][0]+nya.pos2d[1][0]+nya.pos2d[2][0]+nya.pos2d[3][0])/4+50,(nya.pos2d[0][1]+nya.pos2d[1][1]+nya.pos2d[2][1]+nya.pos2d[3][1])/4+50);
    text("TRANS "+trans2text(nya.trans.x)+","+trans2text(nya.trans.y)+","+trans2text(nya.trans.z),0,0);
    text("ANGLE "+angle2text(nya.angle.x)+","+angle2text(nya.angle.y)+","+angle2text(nya.angle.z),0,15);
    popMatrix();    
    //マーカの位置を描画
    drawMarkerPos(nya.pos2d);
    hint(ENABLE_DEPTH_TEST);
    
    PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
    nya.beginTransform(pgl);//マーカ座標系での描画を開始する。
    //ここからマーカ座標系
    stroke(255,200,0);
    translate(0,0,20);
    box(40);
    nya.endTransform();//マーカ座標系での描画を終了する。（必ず呼んで！）
  }
}

