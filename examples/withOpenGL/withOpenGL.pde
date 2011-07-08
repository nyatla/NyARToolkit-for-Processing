/**
  NyARToolkit for proce55ing/1.0.0
  (c)2008-2011 nyatla
  airmail(at)ebony.plala.or.jp
  
  OpenGLAPIで使用する例です。
  OpenGLにAPIを集約することで、Processing行列スタックとの衝突を回避します。
  
  This sample uses OpenGL API only.
  This sample avoids the matrix processing conflict of Processing and OpenGL.

*/
import processing.video.*;
import jp.nyatla.nyar4psg.*;
import processing.opengl.*;
import javax.media.opengl.*; 

Capture cam;
MultiMarker nya;

float[] gl_projection=new float[16];

void setup() {
  size(640,480,OPENGL);
  colorMode(RGB, 100);
  println(MultiMarker.VERSION);  
  cam=new Capture(this,640,480);
  nya=new MultiMarker(this,width,height,"camera_para.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addARMarker("patt.hiro",80);//id=0
  
  //ProjectionMatrixをOpenGLスタイルの行列で得る
  PMatrix3D m=nya.getProjectionMatrix().get();
  m.transpose();
  m.get(gl_projection);
}


void draw()
{
  float[] mv=new float[16];

  if (cam.available() !=true) {
      return;
  }
  cam.read();
  nya.detect(cam);
  background(0);
  nya.drawBackground(cam);//frustumを考慮した背景描画

  PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
  GL gl = pgl.beginGL();
  
  //matrixmodeの退避
  int current_mode=getCurrentMatrixMode(gl);
  
  for(int i=0;i<1;i++){
    if((!nya.isExistMarker(i))){
      continue;
    }
    
    //projectionMarixの適応
    gl.glMatrixMode(gl.GL_PROJECTION);
    gl.glLoadMatrixf(gl_projection, 0 );
    
    //ModelView行列の適応
    PMatrix3D m=nya.getMarkerMatrix(i);
    m.transpose();
    m.get(mv);
    gl.glMatrixMode( gl.GL_MODELVIEW );
    gl.glLoadIdentity();
    
    gl.glScalef( 1.0, -1.0, 1.0 );
    gl.glMultMatrixf(mv,0);
    

    //OpenGLオブジェクトの描画
    drawGLObject(gl);
  }

  //matrixmodeの復帰
  gl.glMatrixMode(current_mode);
  pgl.endGL();
}

//現在のMatrixmodeを取得する。
int getCurrentMatrixMode(GL i_gl)
{
  int[] tmp=new int[1];
  i_gl.glGetIntegerv(GL.GL_MATRIX_MODE,tmp,0);
  return tmp[0];
}

void drawGLObject(GL i_gl)
{
  // 座標軸の描画
  i_gl.glBegin(GL.GL_LINES);
  i_gl.glColor4f(1, 0, 0, 1);
  i_gl.glVertex3f(0,0,0);
  i_gl.glVertex3f(100,0,0);
  i_gl.glColor4f(0, 1, 0, 1);
  i_gl.glVertex3f(0,0,0);
  i_gl.glVertex3f(0,100,0);
  i_gl.glColor4f(0, 0, 1, 1);
  i_gl.glVertex3f(0,0,0);
  i_gl.glVertex3f(0,0,100);
  i_gl.glEnd();
}


