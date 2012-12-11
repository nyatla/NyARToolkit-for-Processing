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

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;



/**
 * このクラスは、NyARToolkit for Processingのベースクラスです。
 * ARToolkit座標系の環境定数、環境設定機能を継承クラスに対して提供します。
 */
public abstract class NyARPsgBaseClass
{
	/**
	 * nearクリップ面のデフォルト値です。
	 */
	public final float FRUSTUM_DEFAULT_NEAR_CLIP=100;
	/**
	 * farクリップ面のデフォルト値です。
	 */
	public final float FRUSTUM_DEFAULT_FAR_CLIP=100000;

	/**
	 * バージョン文字列です。
	 * NyAR4psgのバージョン情報を示します。
	 */
	public final static String VERSION = "NyAR4psg/1.3.1;"+NyARVersion.VERSION_STRING;
	/**　参照するAppletのインスタンスです。*/
	protected PApplet _ref_papplet;	
	/**　バックグラウンド用のModelviewMatrixです。*/
	protected final PMatrix3D _ps_background_mv=new PMatrix3D();
	
	/**　ARToolkitパラメータのインスタンスです。*/
	protected NyAR4PsgConfig _config;
	

	
	private float _clip_far;
	private float _clip_near;
	
	/**
	 * コンストラクタです。
	 */
	protected NyARPsgBaseClass()
	{
	}
	protected void initInstance(PApplet parent,NyAR4PsgConfig i_config) throws NyARException
	{
			this._ref_papplet=parent;
			this._config=i_config;
			//ProcessingのprojectionMatrixの計算と、Frustumの計算
			this.setARClipping(FRUSTUM_DEFAULT_NEAR_CLIP,FRUSTUM_DEFAULT_FAR_CLIP);
		return;
	}
	/**
	 * [readonly]この関数は、Processing形式のProjectionMatrixの参照値を返します。
	 * @return
	 */
	public abstract PMatrix3D getProjectionMatrix();
	/**
	 * この関数は、ProjectionMatrixをi_bufへ複製して返します。
	 * @return
	 * ProjectionMatrixです。
	 */
	public abstract PMatrix3D getProjectionMatrix(PMatrix3D i_buf);
	/**
	 * この関数は、PImageをバックグラウンドへ描画します。PImageはfarclip面+1の部分に描画します。
	 * <div>この関数は、次のコードと等価です。</div>
	 * <hr/>
	 * :<br/>
	 * PMatrix3D om=new PMatrix3D(((PGraphics3D)g).projection);<br/>
	 * setBackgroundOrtho(img.width,img.height)<br/>
	 * pushMatrix();<br/>
	 * resetMatrix();<br/>
	 * translate(0,0,-(far*0.99f));<br/>
	 * image(img,-width/2,-height/2);<br/>
	 * popMatrix();<br/>
	 * setPerspective(om);<br/>
	 * :<br/>
	 * <hr/>
	 * この関数は、PrjectionMatrixとModelViewMatrixを復帰するため、若干のオーバヘッドがあります。
	 * 高速な処理が必要な場合には、展開してください。
	 * @param i_img
	 * 背景画像を指定します。
	 */
	public void drawBackground(PImage i_img)
	{
		PApplet pa=this._ref_papplet;
		PMatrix3D om=new PMatrix3D(((PGraphics3D)pa.g).projection);
		this.setBackgroundOrtho(i_img.width,i_img.height);
		pa.pushMatrix();
		pa.setMatrix(this._ps_background_mv);
		pa.image(i_img,-i_img.width/2,-i_img.height/2);
		pa.popMatrix();
		//行列の復帰
		this.setPerspective(om);
	}
	
	/**
	 * この関数は、視錐台のクリップ面を設定します。この値のデフォルト値は、{@link #FRUSTUM_DEFAULT_NEAR_CLIP}と{@link #FRUSTUM_DEFAULT_FAR_CLIP}です。
	 * 設定値は、次回の{@link #setARPerspective()}から影響を及ぼします。現在の設定値にただちに影響を及ぼすものではありません。
	 * @param i_near
	 * NearPlaneの値を設定します。単位は[mm]です。
	 * @param i_far
	 * FarPlaneの値を設定します。単位は[mm]です。
	 */
	public void setARClipping(float i_near,float i_far)
	{
		this._clip_far=i_far;
		this._clip_near=i_near;
		this._ps_background_mv.reset();
		this._ps_background_mv.translate(0,0,-i_far);
	}
	/**
	 * この関数は、正射影行列をProcessingへセットします。
	 * 画面の中心が0,0にセットされます。
	 * nearクリップには、{@link #setARClipping}でセットしたクリップ面を指定します。
	 *　farクリップには、{@link #setARClipping}でセットしたクリップ面+1を指定します。
	 * <div>この関数は、次のコードと等価です。</div>
	 * <hr/>
	 * :<br/>
	 * //for 1.x<br/>
	 * ortho(-i_width/2, i_width/2,-i_height/2,i_height/2,near,far+1);<br/>
	 * //for 2.x<br/>
	 * ortho(0,i_width,0,i_height,near,far+1);<br/>
	 * :<br/>
	 * <hr/>
	 * @param i_width
	 * 幅を指定します。
	 * @param i_height
	 * 高さを指定します。
	 */
	public void setBackgroundOrtho(int i_width,int i_height)
	{		
		float half_w=i_width/2;
		float half_h=i_height/2;
		this._ref_papplet.ortho(-half_w, half_w,-half_h,half_h,this._clip_near,this._clip_far+1);
	}
	/**
	 * この関数は、ARToolKit準拠のProjectionMatrixをProcessingにセットします。
	 * 関数を実行すると、ProcessingのProjectionMatrixがARToolKitのカメラパラメータのものに変わり、映像にマッチした描画ができるようになります。
	 * ProcessingのデフォルトFrustumに戻すときは、{@link PGraphics3D#perspective()}を使います。
	 * Frustumの有効期間は、次に{@link PGraphics3D#perspective()}か{@link PGraphics3D#perspective()}をコールするまでです。
	 * <p>
	 * Version 1.1.0より、古いprojection matrixを返さなくなりました。古いprojection matrixが必要な時は、{@link PGraphics3D#projection}を複製して保存して下さい。
	 * </p>
	 */
	public void setARPerspective()
	{
		this.setPerspective(this.getProjectionMatrix());
	}
	/**
	 * この関数は、ProjectionMatrixをProcessingにセットします。
	 * @param i_projection
	 * 設定するProjectionMatrixを指定します。
	 * <p>
	 * Processing/1.3になったら、{@link PApplet#matrixMode}使ってきちんと使えるようになると思う。
	 * 今は無理なので、frustum経由
	 * </p>
	 * <p>
	 * Version 1.1.0より、古いprojection matrixを返さなくなりました。古いprojection matrixが必要な時は、{@link PGraphics3D#projection}を複製して保存して下さい。
	 * </p>
	 */	
	public void setPerspective(PMatrix3D i_projection)
	{
		//Projectionをfrustum経由で設定。
		float far=i_projection.m23/(i_projection.m22+1);
		float near=i_projection.m23/(i_projection.m22-1);
		this._ref_papplet.frustum(
				(i_projection.m02-1)*near/i_projection.m00,
				(i_projection.m02+1)*near/i_projection.m00,
				(i_projection.m12-1)*near/i_projection.m11,
				(i_projection.m12+1)*near/i_projection.m11,
				near,far);
		return;
	}

	protected static void PMatrix2GLProjection(PMatrix3D i_in,float[] o_out)
	{
		o_out[ 0]=i_in.m00;
		o_out[ 1]=i_in.m10;
		o_out[ 2]=i_in.m20;
		o_out[ 3]=i_in.m30;
		o_out[ 4]=i_in.m01;
		o_out[ 5]=i_in.m11;
		o_out[ 6]=i_in.m21;
		o_out[ 7]=i_in.m31;
		o_out[ 8]=i_in.m02;
		o_out[ 9]=i_in.m12;
		o_out[10]=i_in.m22;
		o_out[11]=i_in.m32;
		o_out[12]=i_in.m03;
		o_out[13]=i_in.m13;
		o_out[14]=i_in.m23;
		o_out[15]=i_in.m33;		
	}
	protected static void PMatrix2GLProjection(PMatrix3D i_in,double[] o_out)
	{
		o_out[ 0]=i_in.m00;
		o_out[ 1]=i_in.m10;
		o_out[ 2]=i_in.m20;
		o_out[ 3]=i_in.m30;
		o_out[ 4]=i_in.m01;
		o_out[ 5]=i_in.m11;
		o_out[ 6]=i_in.m21;
		o_out[ 7]=i_in.m31;
		o_out[ 8]=i_in.m02;
		o_out[ 9]=i_in.m12;
		o_out[10]=i_in.m22;
		o_out[11]=i_in.m32;
		o_out[12]=i_in.m03;
		o_out[13]=i_in.m13;
		o_out[14]=i_in.m23;
		o_out[15]=i_in.m33;	
	}
	protected static void nyarMat2PsMat(NyARDoubleMatrix44 i_src,PMatrix3D i_dst)
	{
		i_dst.m00=(float)(i_src.m00);
		i_dst.m01=(float)(i_src.m01);
		i_dst.m02=(float)(i_src.m02);
		i_dst.m03=(float)(i_src.m03);
		i_dst.m10=(float)(i_src.m10);
		i_dst.m11=(float)(i_src.m11);
		i_dst.m12=(float)(i_src.m12);
		i_dst.m13=(float)(i_src.m13);
		i_dst.m20=(float)(i_src.m20);
		i_dst.m21=(float)(i_src.m21);
		i_dst.m22=(float)(i_src.m22);
		i_dst.m23=(float)(i_src.m23);
		i_dst.m30=(float)(i_src.m30);
		i_dst.m31=(float)(i_src.m31);
		i_dst.m32=(float)(i_src.m32);
		i_dst.m33=(float)(i_src.m33);
	}

	/**
	 * 左手系変換用の行列
	 */
	private final static PMatrix3D _lh_mat=new PMatrix3D(
		-1,0,0,0,
		 0,1,0,0,
		 0,0,1,0,
		 0,0,0,1);
	
	/**
	 * 変換行列をProcessingのMatrixへ変換します。
	 * @param i_src
	 * @param i_mode
	 * @param o_pmatrix
	 */
	protected static void matResult2PMatrix3D(NyARDoubleMatrix44 i_src,int i_mode,PMatrix3D o_pmatrix)
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
		if(i_mode==NyAR4PsgConfig.CS_LEFT_HAND)
		{
			o_pmatrix.apply(_lh_mat);
		}
	}
	protected static void matResult2GLArray(NyARDoubleMatrix44 i_src,double[] o_gl_array)
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

}

