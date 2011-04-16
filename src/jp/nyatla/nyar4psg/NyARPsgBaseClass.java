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


import processing.core.*;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

class NyPsUtils
{
	public static void dumpObject(PMatrix3D i_mat)
	{
		PApplet.println("PMatrix3D");
		PApplet.println(String.format("%f %f %f %f",i_mat.m00,i_mat.m01,i_mat.m02,i_mat.m03));
		PApplet.println(String.format("%f %f %f %f",i_mat.m10,i_mat.m11,i_mat.m12,i_mat.m13));
		PApplet.println(String.format("%f %f %f %f",i_mat.m20,i_mat.m21,i_mat.m22,i_mat.m23));
		PApplet.println(String.format("%f %f %f %f",i_mat.m30,i_mat.m31,i_mat.m32,i_mat.m33));
	}
	public static void dumpObject(double[] i_val)
	{
		PApplet.println("double[]");
		for(int i=0;i<i_val.length;i++){
			PApplet.println(i_val[i]);
		}
	}
}

/**
 * このクラスは、NyARToolkit for Processingのベースクラスです。
 * ARToolkit座標系の環境定数、環境設定機能を継承クラスに対して提供します。
 */
class NyARPsgBaseClass
{
	/**
	 * 定数値です。この値はコンストラクタで使います。
	 * RightHand系の座標を構築します。
	 * RightHand座標系は、ARToolKitと互換性のある座標系ですが、Processingの座標系と互換性がありません。（そのため、text等の出力が鏡像になります。）
	 * <br/>EN: -
	 */
	public final static int CS_RIGHT_HAND=0;
	/**
	 * 定数値です。この値はコンストラクタで使います。
	 * LeftHand座標系を構築します。
	 * RightHand座標系は、ARToolKitと互換性のない座標系ですが、Processingの座標系と互換性があります。
	 * processing関数で描画する場合は、こちらを選択してください。
	 * <br/>EN: -
	 */
	public final static int CS_LEFT_HAND =1;
	/**
	 * バージョン文字列です。
	 * NyAR4psgのバージョン情報を示します。
	 * <br/>EN:
	 * version string.
	 */
	public final String VERSION = "NyAR4psg/0.4.1;NyARToolkit for java/3.0.0;ARToolKit/2.72.1";
	/**
	 * OpenGLスタイルのProjectionMatrixです。
	 * <br/>EN:
	 * OpenGL form projection matrix.
	 */
	public final double[] projection=new double[16];
	/**
	 * ProcessingスタイルのProjectionMatrixです。
	 */
	protected PMatrix3D _ps_projection=new PMatrix3D();
	/**
	 * 参照するAppletのインスタンスです。
	 */
	protected PApplet _ref_papplet;
	/**
	 * ARToolkitパラメータのインスタンスです。
	 */
	protected NyARParam _ar_param;
	protected NyARFrustum _frustum;
	protected int _coord_system;
	protected NyARPsgBaseClass(PApplet parent,String i_cparam_file, int i_width,int i_height,int i_coord_system)
	{
		checkCoordinateSystemRange(parent,i_coord_system);
		this._ref_papplet=parent;
		this._coord_system=i_coord_system;
		try{
			this._frustum=new NyARFrustum();
			this._ar_param=new NyARParam();
			this._ar_param.loadARParam(this._ref_papplet.createInput(i_cparam_file));
			this._ar_param.changeScreenSize(i_width, i_height);

			//ProcessingのprojectionMatrixの計算と、Frustumの計算
			arPerspectiveMat2Projection(this._ar_param.getPerspectiveProjectionMatrix(),i_width,i_height,this._ps_projection,this.projection,this._frustum);
		}catch(NyARException e){
			this._ref_papplet.die("Error while setting up NyARToolkit for java", e);
		}
		return;
	}
	private final static double view_distance_min = 100;
	private final static double view_distance_max = 100000;
	private final static void checkCoordinateSystemRange(PApplet i_pa,int i_cs)
	{
		switch(i_cs){
		case NyARPsgBaseClass.CS_LEFT_HAND:
		case NyARPsgBaseClass.CS_RIGHT_HAND:
			return;
		default:
			i_pa.die("Please set constant CS_LEFT_HAND or CS_RIGHT_HAND.");
		}
	}
	private static void arPerspectiveMat2Projection(NyARPerspectiveProjectionMatrix i_prjmat,int i_w,int i_h,PMatrix3D o_projection,double[] o_gl_projection,NyARFrustum o_frustum)
	{
		NyARDoubleMatrix44 tmp=new NyARDoubleMatrix44();
		i_prjmat.makeCameraFrustumRH(i_w, i_h, view_distance_min, view_distance_max,tmp);
		o_projection.m00=(float)(o_gl_projection[ 0]=tmp.m00);
		o_projection.m01=(float)(o_gl_projection[ 1]=tmp.m10);
		o_projection.m02=(float)(o_gl_projection[ 2]=tmp.m20);
		o_projection.m03=(float)(o_gl_projection[ 3]=tmp.m30);
		o_projection.m10=(float)(o_gl_projection[ 4]=tmp.m01);
		o_projection.m11=(float)(o_gl_projection[ 5]=tmp.m11);
		o_projection.m12=(float)(o_gl_projection[ 6]=tmp.m21);
		o_projection.m13=(float)(o_gl_projection[ 7]=tmp.m31);
		o_projection.m20=(float)(o_gl_projection[ 8]=tmp.m02);
		o_projection.m21=(float)(o_gl_projection[ 9]=tmp.m12);
		o_projection.m22=(float)(o_gl_projection[10]=tmp.m22);
		o_projection.m23=(float)(o_gl_projection[11]=tmp.m32);
		o_projection.m30=(float)(o_gl_projection[12]=tmp.m03);
		o_projection.m31=(float)(o_gl_projection[13]=tmp.m13);
		o_projection.m32=(float)(o_gl_projection[14]=tmp.m23);
		o_projection.m33=(float)(o_gl_projection[15]=tmp.m33);
		o_frustum.setValue(tmp, i_w, i_h);
	}
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
	}
	/**
	 * 左手系変換用の行列
	 */
	private final static PMatrix3D _lh_mat=new PMatrix3D(-1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1);
	
	/**
	 * 変換行列をProcessingのMatrixへ変換します。
	 * @param i_src
	 * @param i_mode
	 * @param o_pmatrix
	 */
	protected static void matResult2PMatrix3D(NyARTransMatResult i_src,int i_mode,PMatrix3D o_pmatrix)
	{
		o_pmatrix.m00 = (float)i_src.m00; 
		o_pmatrix.m01 = (float)i_src.m01;
		o_pmatrix.m02 = (float)i_src.m02;
		o_pmatrix.m03 = (float)i_src.m03;
		o_pmatrix.m10 = (float)i_src.m10;//mirror
		o_pmatrix.m11 = (float)i_src.m11;//mirror
		o_pmatrix.m12 = (float)i_src.m12;//mirror
		o_pmatrix.m13 = (float)i_src.m13;//mirror
		o_pmatrix.m20 = (float)-i_src.m20;
		o_pmatrix.m21 = (float)-i_src.m21;
		o_pmatrix.m22 = (float)-i_src.m22;
		o_pmatrix.m23 = (float)-i_src.m23;
		o_pmatrix.m30 = 0.0f;
		o_pmatrix.m31 = 0.0f;
		o_pmatrix.m32 = 0.0f;
		o_pmatrix.m33 = 1.0f;
		if(i_mode==MultiARTookitMarker.CS_LEFT_HAND)
		{
			o_pmatrix.apply(_lh_mat);
		}
	}	
	/**
	 * この関数は、スクリーン座標を撮像点座標に変換します。
	 * 撮像点の座標系は、カメラ座標系になります。
	 * <p>公式 - 
	 * この関数は、gluUnprojectのビューポートとモデルビュー行列を固定したものです。
	 * 公式は、以下の物使用しました。
	 * http://www.opengl.org/sdk/docs/man/xhtml/gluUnProject.xml
	 * ARToolKitの座標系に合せて計算するため、OpenGLのunProjectとはix,iyの与え方が違います。画面上の座標をそのまま与えてください。
	 * </p>
	 * @param ix
	 * スクリーン上の座標
	 * @param iy
	 * 画像上の座標
	 * @param o_point_on_screen
	 * 撮像点座標
	 */
/*	public final PVector unProject(double ix,double iy)
	{
		double n=(this._frustum_rh.m23/(this._frustum_rh.m22-1));
		NyARDoubleMatrix44 m44=this._inv_frustum_rh;
		double v1=(this._screen_size.w-ix-1)*2/this._screen_size.w-1.0;//ARToolKitのFrustramに合せてる。
		double v2=(this._screen_size.h-iy-1)*2/this._screen_size.h-1.0;
		double v3=2*n-1.0;
		double b=1/(m44.m30*v1+m44.m31*v2+m44.m32*v3+m44.m33);
		o_point_on_screen.x=(m44.m00*v1+m44.m01*v2+m44.m02*v3+m44.m03)*b;
		o_point_on_screen.y=(m44.m10*v1+m44.m11*v2+m44.m12*v3+m44.m13)*b;
		o_point_on_screen.z=(m44.m20*v1+m44.m21*v2+m44.m22*v3+m44.m23)*b;
		return;
	}
*/	/**
	 * この関数は、スクリーン上の点と原点を結ぶ直線と、任意姿勢の平面の交差点を、カメラの座標系で取得します。
	 * この座標は、カメラ座標系です。
	 * @param ix
	 * スクリーン上の座標
	 * @param iy
	 * スクリーン上の座標
	 * @param i_mat
	 * 平面の姿勢行列です。
	 * @param o_pos
	 * 結果を受け取るオブジェクトです。
	 */
/*	public final PVector unProjectOnCamera(double ix,double iy,PMatrix3D i_mat)
	{
		//画面→撮像点
		this.unProject(ix,iy,o_pos);
		//撮像点→カメラ座標系
		double nx=i_mat.m02;
		double ny=i_mat.m12;
		double nz=i_mat.m22;
		double mx=i_mat.m03;
		double my=i_mat.m13;
		double mz=i_mat.m23;
		double t=(nx*mx+ny*my+nz*mz)/(nx*o_pos.x+ny*o_pos.y+nz*o_pos.z);
		o_pos.x=t*o_pos.x;
		o_pos.y=t*o_pos.y;
		o_pos.z=t*o_pos.z;
	}	
*/	/**
	 * 画面上の点と原点を結ぶ直線と任意姿勢の平面の交差点を、平面の座標系で取得します。
	 * ARToolKitの本P175周辺の実装と同じです。
	 * @param ix
	 * スクリーン上の座標
	 * @param iy
	 * スクリーン上の座標
	 * @param i_mat
	 * 平面の姿勢行列です。
	 * @param o_pos
	 * 結果を受け取るオブジェクトです。
	 * @return
	 * 計算に成功すると、trueを返します。
	 */
/*	public final PVector unProjectOnMatrix(double ix,double iy,PMatrix3D i_mat)
	{
		//交点をカメラ座標系で計算
		unProjectOnCamera(ix,iy,i_mat,o_pos);
		//座標系の変換
		NyARDoubleMatrix44 m=new NyARDoubleMatrix44();
		if(!m.inverse(i_mat)){
			return false;
		}
		m.transform3d(o_pos, o_pos);
		return true;
	}
*/	/**
	 * カメラ座標系の点を、スクリーン座標の点へ変換します。
	 * @param i_x
	 * カメラ座標系の点
	 * @param i_y
	 * カメラ座標系の点
	 * @param i_z
	 * カメラ座標系の点
	 * @param o_pos2d
	 * 結果を受け取るオブジェクトです。
	 */
/*	public final PVector project(double i_x,double i_y,double i_z)
	{
		NyARDoubleMatrix44 m=this._frustum_rh;
		double v3_1=1/i_z*m.m32;
		double w=this._screen_size.w;
		double h=this._screen_size.h;
		o_pos2d.x=w-(1+(i_x*m.m00+i_z*m.m02)*v3_1)*w/2;
		o_pos2d.y=h-(1+(i_y*m.m11+i_z*m.m12)*v3_1)*h/2;
		return;
	}
*/}

