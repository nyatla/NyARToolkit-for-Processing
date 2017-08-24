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

package jp.nyatla.nyar4psg.utils;



import processing.core.*;
import processing.opengl.*;
import jp.nyatla.nyar4psg.NyAR4PsgConfig;
import jp.nyatla.nyar4psg.SingleCameraView;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;



/**
 * このクラスは、NyARToolkit for Processingのベースクラスです。
 * 環境定数などの共通値を定義します。
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
	final public static String VERSION = "NyAR4psg/3.0.6;"+NyARVersion.VERSION_STRING;
	/**　参照するAppletのインスタンスです。*/
	final protected PApplet _ref_papplet;	

	/** 関連付けられているビューです。他のインスタンスと共有するときに使います。*/
	final public SingleCameraView cameraview;
	

	
	/**
	 * コンストラクタです。
	 */
	protected NyARPsgBaseClass(PApplet i_ref_applet,SingleCameraView i_view)
	{
		this._ref_papplet=i_ref_applet;
		this.cameraview=i_view;
		NyARIntSize ss=i_view._view.getARParam().getScreenSize();
		//ProcessingのprojectionMatrixの計算と、Frustumの計算
		i_view.setARClipping(ss.w,ss.h,FRUSTUM_DEFAULT_NEAR_CLIP,FRUSTUM_DEFAULT_FAR_CLIP);//ここ問題があるよ？
		return;
	}

	/**
	 * この関数は、PImageをバックグラウンドへ描画します。PImageはfarclip面+1の部分に描画します。
	 * <div>この関数は、次のコードと等価です。</div>
	 * <hr/>
	 * :<br/>
	 * PMatrix3D om=new PMatrix3D(((PGrapPGraphicsOpenGLhics3D)g).projection);<br/>
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
		this.cameraview.drawBackground(i_img);
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
		this.cameraview.setBackgroundOrtho(i_width, i_height);
	}
	/**
	 * この関数は、ARToolKit準拠のProjectionMatrixをProcessingにセットします。
	 * 関数を実行すると、ProcessingのProjectionMatrixがARToolKitのカメラパラメータのものに変わり、映像にマッチした描画ができるようになります。
	 * ProcessingのデフォルトFrustumに戻すときは、{@link PGraphicsOpenGL#perspective()}を使います。
	 * Frustumの有効期間は、次に{@link PGraphicsOpenGL#perspective()}か{@link PGraphicsOpenGL#perspective()}をコールするまでです。
	 * <p>
	 * Version 1.1.0より、古いprojection matrixを返さなくなりました。古いprojection matrixが必要な時は、{@link PGraphicsOpenGL#projection}を複製して保存して下さい。
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
	 * Version 1.1.0より、古いprojection matrixを返さなくなりました。古いprojection matrixが必要な時は、{@link PGraphicsOpenGL#projection}を複製して保存して下さい。
	 * </p>
	 */	
	final public void setPerspective(PMatrix3D i_projection)
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


	
	/**
	 * [readonly]この関数は、Processing形式のProjectionMatrixの参照値を返します。
	 * @return
	 */	
	final public PMatrix3D getProjectionMatrix()
	{
		return this.cameraview.getProjectionMatrix();
	}
	/**
	 * この関数は、視錐台のクリップ面を設定します。この値のデフォルト値は、{@link #FRUSTUM_DEFAULT_NEAR_CLIP}と{@link #FRUSTUM_DEFAULT_FAR_CLIP}です。
	 * 設定値は、次回の{@link #setARPerspective()}から影響を及ぼします。現在の設定値にただちに影響を及ぼすものではありません。
	 * @param i_near
	 * NearPlaneの値を設定します。単位は[mm]です。
	 * @param i_far
	 * FarPlaneの値を設定します。単位は[mm]です。
	 */
	final public void setARClipping(float i_near,float i_far)
	{
		NyARIntSize s=this.cameraview._view.getARParam().getScreenSize();
		this.cameraview.setARClipping(s.w,s.h, i_near, i_far);
		return;
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

