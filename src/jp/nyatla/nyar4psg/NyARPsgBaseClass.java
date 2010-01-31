/* 
 * PROJECT: NyARToolkit for proce55ing.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 nyatla
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/nyartoolkit/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */

package jp.nyatla.nyar4psg;
import javax.media.opengl.GL;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.*;



/**
 * NyARToolkit for Processingのベースクラス。
 * カメラパラメータに関する機能、そのたすべてのユースケースで使用する機能を提供します。
 * @author nyatla
 *
 */
class NyARPsgBaseClass
{
	/**
	 * RightHand座標系であることを示します。
	 * この値はコンストラクタで使います。
	 * <br/>EN: -
	 */
	public final static int CS_RIGHT=0;
	/**
	 * LeftHand座標系であることを示します。
	 * この値はコンストラクタで使います。
	 * <br/>EN: -
	 */
	public final static int CS_LEFT =1;
	/**
	 * バージョン文字列です。
	 * <br/>EN:
	 * version string.
	 */
	public final String VERSION = "NyAR4psg/0.3.0;NyARToolkit for java/2.5.0+;ARToolKit/2.72.1";
	/**
	 * OpenGLスタイルのProjectionMatrixです。
	 * <br/>EN:
	 * OpenGL form projection matrix.
	 */
	public final double[] projection=new double[16];

	protected PMatrix3D _ps_projection=new PMatrix3D();
	protected PApplet _pa;
	protected NyARParam _ar_param;
	protected NyARPsgBaseClass(PApplet parent,String i_cparam_file, int i_width,int i_htight,int i_projection_coord_system)
	{
		checkCoordinateSystemRange(parent,i_projection_coord_system);
		this._pa=parent;
		try{
			this._ar_param=new NyARParam();
			this._ar_param.loadARParam(this._pa.createInput(i_cparam_file));
			this._ar_param.changeScreenSize(i_width, i_htight);
			initProjection(parent,this._ar_param,i_projection_coord_system);
		}catch(NyARException e){
			this._pa.die("Error while setting up NyARToolkit for java", e);
		}
		return;
	}
	private void initProjection(PApplet i_pa, NyARParam i_param,int i_coord_system)
	{
		NyARMat trans_mat = new NyARMat(3, 4);
		NyARMat icpara_mat = new NyARMat(3, 4);
		double[][] p = new double[3][3], q = new double[4][4];
		int i, j;

		final NyARIntSize size=i_param.getScreenSize();
		final int width = size.w;
		final int height = size.h;
		
		i_param.getPerspectiveProjectionMatrix().decompMat(icpara_mat, trans_mat);

		double[][] icpara = icpara_mat.getArray();
		double[][] trans = trans_mat.getArray();
		for (i = 0; i < 4; i++) {
			icpara[1][i] = (height - 1) * (icpara[2][i]) - icpara[1][i];
		}

		for (i = 0; i < 3; i++) {
			for (j = 0; j < 3; j++) {
				p[i][j] = icpara[i][j] / icpara[2][2];
			}
		}
		q[0][0] = (2.0 * p[0][0] / (width - 1));
		q[0][1] = (2.0 * p[0][1] / (width - 1));
		q[0][2] = -((2.0 * p[0][2] / (width - 1)) - 1.0);
		q[0][3] = 0.0;

		q[1][0] = 0.0;
		q[1][1] = -(2.0 * p[1][1] / (height - 1));
		q[1][2] = -((2.0 * p[1][2] / (height - 1)) - 1.0);
		q[1][3] = 0.0;

		q[2][0] = 0.0;
		q[2][1] = 0.0;
		q[2][2] = (view_distance_max + view_distance_min) / (view_distance_min - view_distance_max);
		q[2][3] = 2.0 * view_distance_max * view_distance_min / (view_distance_min - view_distance_max);

		q[3][0] = 0.0;
		q[3][1] = 0.0;
		q[3][2] = -1.0;
		q[3][3] = 0.0;
		
		switch(i_coord_system){
		case NyARPsgBaseClass.CS_LEFT:
			break;
		case NyARPsgBaseClass.CS_RIGHT:
			q[2][2] = q[2][2]* -1;
			q[2][3] = q[2][3]* -1;
			break;
		default:
			i_pa.die("Please set NyARBoard.CS_LEFT or NyARBoard.CS_RIGHT.");
		}
		
		for (i = 0; i < 4; i++) { // Row.
			// First 3 columns of the current row.
			for (j = 0; j < 3; j++) { // Column.
				this.projection[i + j * 4] = q[i][0] * trans[0][j] + q[i][1] * trans[1][j] + q[i][2] * trans[2][j];
			}
			// Fourth column of the current row.
			this.projection[i + 3 * 4] = q[i][0] * trans[0][3] + q[i][1] * trans[1][3] + q[i][2] * trans[2][3] + q[i][3];
		}
		
		//processingのProjectionMatrixも計算して保存する。
		this._ps_projection.m00=(float)this.projection[0];
		this._ps_projection.m01=(float)this.projection[1];
		this._ps_projection.m02=(float)this.projection[2];
		this._ps_projection.m03=(float)this.projection[3];
		this._ps_projection.m10=(float)this.projection[4];
		this._ps_projection.m11=(float)this.projection[5];
		this._ps_projection.m12=(float)this.projection[6];
		this._ps_projection.m13=(float)this.projection[7];
		this._ps_projection.m20=(float)this.projection[8];
		this._ps_projection.m21=(float)this.projection[9];
		this._ps_projection.m22=(float)this.projection[10];
		this._ps_projection.m23=(float)this.projection[11];
		this._ps_projection.m30=(float)this.projection[12];
		this._ps_projection.m31=(float)this.projection[13];
		this._ps_projection.m32=(float)this.projection[14];
		this._ps_projection.m33=(float)this.projection[15];
		this._ps_projection.transpose();

		return;	
	}

	
	private final static double view_distance_min = 100;//#define VIEW_DISTANCE_MIN		0.1			// Objects closer to the camera than this will not be displayed.
	private final static double view_distance_max = 100000;//#define VIEW_DISTANCE_MAX		100.0		// Objects further away from the camera than this will not be displayed.
	private final static void checkCoordinateSystemRange(PApplet i_pa,int i_cs)
	{
		switch(i_cs){
		case NyARPsgBaseClass.CS_LEFT:
		case NyARPsgBaseClass.CS_RIGHT:
			return;
		default:
			i_pa.die("Please set constant CS_LEFT or CS_RIGHT.");
		}
	}
	private static double view_scale_factor = 1.0;
	protected static void matResult2GLArray(NyARTransMatResult i_src,double[] o_gl_array)
	{
		o_gl_array[0 + 0 * 4] = i_src.m00; 
		o_gl_array[0 + 1 * 4] = i_src.m01;
		o_gl_array[0 + 2 * 4] = i_src.m02;
		o_gl_array[0 + 3 * 4] = i_src.m03;
		o_gl_array[1 + 0 * 4] = -i_src.m10;
		o_gl_array[1 + 1 * 4] = -i_src.m11;
		o_gl_array[1 + 2 * 4] = -i_src.m12;
		o_gl_array[1 + 3 * 4] = -i_src.m13;
		o_gl_array[2 + 0 * 4] = -i_src.m20;
		o_gl_array[2 + 1 * 4] = -i_src.m21;
		o_gl_array[2 + 2 * 4] = -i_src.m22;
		o_gl_array[2 + 3 * 4] = -i_src.m23;
		o_gl_array[3 + 0 * 4] = 0.0;
		o_gl_array[3 + 1 * 4] = 0.0;
		o_gl_array[3 + 2 * 4] = 0.0;
		o_gl_array[3 + 3 * 4] = 1.0;
		if (view_scale_factor != 0.0) {
			o_gl_array[12] *= view_scale_factor;
			o_gl_array[13] *= view_scale_factor;
			o_gl_array[14] *= view_scale_factor;
		}
	}
}

/**
 * 単一マーカ認識ユースケースのベースクラス。同時に一つの座標変換行列を提供します。
 * 座標変換行列の管理関数とプロパティ機能を提供します。
 * @author nyatla
 *
 */
class SingleMarkerBaseClass extends NyARPsgBaseClass
{
	/**
	 * マーカのx,y,zの傾き角度です。
	 * <br/>EN:
	 * The angle value in radian unit of "x,y,z" .
	 */
	public PVector angle;
	/**
	 * マーカのx,y,zの平行移動量です。
	 * <br/>EN:
	 * The translation value in radian unit of "x,y,z".
	 */
	public PVector trans;
	/**
	 * 検出したマーカの4隅の２次元画像上の位置です。
	 * <br/>EN:
	 * The position of 4 corner of marker.
	 */
	public int[][] pos2d;
	/**
	 * 検出したマーカの変換行列です。
	 * <br/>EN:
	 * The transform matrix of detected marker.
	 */
	public double[] transmat;
	/**
	 * 座標変換を実行したMatrixを準備します。
	 * この関数を実行すると、processingの座標系がマーカ表面に設定されます。
	 * 描画終了後には、必ずendTransform関数を呼び出して座標系を戻してください。
	 * <br/>EN:
	 * This function sets corresponding transform matrix to the surface of the marker to OpenGL.
	 * The coordinate system of processing moves to the surface of the marker when this function is executed.
	 * Must return the coordinate system by using endTransform function at the end.
	 * @param i_pgl
	 * PGraphicsOpenGLインスタンスを設定します。processingのgメンバをキャストして設定してください。
	 * <br/>EN:
	 * Specify PGraphicsOpenGL instance.
	 * Set cast "g" member of processing graphics object.
	 */
	public void beginTransform(PGraphicsOpenGL i_pgl)
	{
		if(this._gl!=null){
			this._pa.die("The function beginTransform is already called.", null);			
		}
		this._pgl=i_pgl;
		this._gl=this._pgl.beginGL();

		
		this._gl=i_pgl.gl;
		this._gl.glMatrixMode(GL.GL_PROJECTION);
		this._pa.pushMatrix();
		this._pa.resetMatrix();
		//PGraphicsOpenGLのupdateProjectionのモノマネをします。
		this._gl.glLoadMatrixd(this.projection,0);
		this._old_matrix=this._pgl.projection;
		this._pgl.projection=this._ps_projection;


		this._gl.glMatrixMode(GL.GL_MODELVIEW);
		this._pa.pushMatrix();
		this._pa.resetMatrix();
		this._gl.glLoadMatrixd(this.transmat,0);
				
		this._pa.pushMatrix();
		return;	
	}
	/**
	 * beginTransformによる座標変換を解除して元に戻します。
	 * <br/>EN:
	 * This function recover coordinate system that was changed by beginTransform function.
	 */
	public void endTransform()
	{
		if(this._gl==null){
			this._pa.die("The function beginTransform is never called.", null);			
		}
		this._pgl.projection=this._old_matrix;
		this._pa.popMatrix();
		this._pa.popMatrix();
		this._gl.glMatrixMode(GL.GL_PROJECTION);
		this._pa.popMatrix();
		this._gl.glMatrixMode(GL.GL_MODELVIEW);
		if(this._pgl!=null){
			this._pgl.endGL();
		}
		this._gl=null;
		this._pgl=null;
		return;
	}	
	protected void updateTransmat(NyARSquare i_square,NyARTransMatResult i_src)
	{
		matResult2GLArray(i_src,this.transmat);
		//angle
		i_src.getZXYAngle(this._tmp_d3p);
		
		this.angle.x=(float)this._tmp_d3p.x;
		this.angle.y=(float)this._tmp_d3p.y;
		this.angle.z=(float)this._tmp_d3p.z;
		//trans
		this.trans.x=(float)i_src.m03;
		this.trans.y=(float)i_src.m13;
		this.trans.z=(float)i_src.m23;

		//pos反映
		final NyARDoublePoint2d[] pts=i_square.sqvertex;
		for(int i=0;i<4;i++){
			this.pos2d[i][0]=(int)pts[i].x;
			this.pos2d[i][1]=(int)pts[i].y;
		}		
		
		return;	
	}
	/********
	 * 	protected/private
	 *******/
	private final NyARDoublePoint3d _tmp_d3p=new NyARDoublePoint3d();
	
	
	//キャッシュたち
	private GL _gl=null;
	private PGraphicsOpenGL _pgl=null;	
	private PMatrix3D _old_matrix;
	protected SingleMarkerBaseClass(PApplet parent,String i_cparam_file, int i_width,int i_htight,int i_projection_coord_system)
	{
		super(parent,i_cparam_file,i_width,i_htight,i_projection_coord_system);
	}


	
	
	
	
}