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
import javax.media.opengl.*;
import processing.core.*;
import processing.opengl.*;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.detector.*;



/**
 * This class calculate a ARToolKit base transformation matrix from PImage.
 * This class handles one marker, and calculates the transformation matrix that displays the object above the marker.
 * <pre>
 * NyARBoard needs two an external configuration file "Marker file" and "Camera parameter" files.
 * These are compatible with the original ARToolKit. 
 * Place those files to under the data directory of the sketch. (see example.)
 * </pre>
 * <br/>JP:
 * このクラスは、PImageからARToolKit準拠の変換行列を求めるクラスです。
 * マーカを１枚の板に見立てて、そこを中心にした座標系を計算します。
 * 同時検出可能なマーカは１種類、１個です。
 * <pre>
 * NyARBoardは、２つの外部設定ファイル"マーカファイル"と"カメラパラメータ"ファイルを必要とします。
 * これらはARToolKitのものと互換性があります。
 * これらはスケッチのdataディレクトリ以下に配置をして下さい。（exampleを見てください。）
 * </pre>
 * @author nyatla
 *
 */
public class NyARBoard extends NyARPsgBaseClass
{
	/**
	 * NyARBoard ignores that it lost the marker while under specified number.
	 * Must be "n&gt;=0".
	 * <br/>JP:
	 * 消失遅延数。マーカ消失後、指定回数は過去の情報を維持します。
	 * 値は0以上であること。
	 */
	public int lostDelay =10;
	/**
	 * It is a number in which it continuously lost the marker.
	 * This value range is "0&lt;n&lt;lostDelay"
	 * <br/>JP:
	 * 連続でマーカを見失った回数。この値は0<n<lostDelayをとります。
	 */
	public int lostCount = 0;
	/**
	 * The threshold value of marker pattern confidence.
	 * This value range is "0.0&lt;n&lt;1.0".
	 * When marker confidence is larger than this value, NyARBoard detects the marker.
	 * <br/>JP:
	 * マーカの座標変換を行う閾値。0.0&lt;n&lt;1.0の値をとります。
	 * この数値より一致度が大きい場合のみ、マーカを検出したと判定され、座標計算が行われます。
	 */
	public double cfThreshold=0.4;
	/**
	 * The angle value in radian unit of "x,y,z" .
	 * <br/>JP:
	 * マーカのx,y,zの傾き角度です。
	 */
	public final PVector angle=new PVector();
	/**
	 * The translation value in radian unit of "x,y,z".
	 * <br/>JP:
	 * マーカのx,y,zの平行移動量です。
	 */
	public final PVector trans=new PVector();
	/**
	 * The confidence value of detected marker.
	 * <br/>JP:
	 * 検出したマーカの一致度です。
	 */
	public double confidence=0.0;
	/**
	 * The position of 4 corner of marker.
	 * <br/>JP:
	 * 検出したマーカの4隅の２次元画像上の位置です。
	 */
	public final int[][] pos2d=new int[4][2];
	/**
	 * The transform matrix of detected marker.
	 * <br/>JP:
	 * 検出したマーカの変換行列です。
	 */
	public final double[] transmat=new double[16];
	/**
	 * The threshold value of labeling process from gray scale image.
	 * This value range is "0&lt;=n&lt;=255".
	 * <br/>JP:
	 * マーカ検出時のグレースケール閾値を指定します。この値は、0&lt;=n&lt;=255をります。
	 */
	public int gsThreshold=110;
	
	
	
	private final NyARTransMatResult _result=new NyARTransMatResult();
	private NyARSingleDetectMarker _nya;
	private PImageRaster _raster;
	/**
	 * This function is constructor.
	 * <br/>JP:
	 * この関数はコンストラクタです。
	 * @param parent
	 * Specify processing instance.
	 * <br/>JP:
	 * processingのインスタンスを指定します。
	 * @param i_width
	 * Width of source image size for "detect()".
	 * <br/>JP:
	 * detect()に渡す入力画像の幅を指定します。
	 * @param i_htight
	 * Height of source image size for "detect()".
	 * <br/>JP:
	 * detectに渡す入力画像の高さを指定します。
	 * @param i_cparam
	 * The file name of the camera parameter of ARToolKit format.
	 * Place the file to "data" directory at sketch.
	 * <br/>JP:
	 * ARToolKitのパラメータファイル名を指定します。パラメータファイルはdataディレクトリにおいて下さい。
	 * @param i_patt
	 * The file name of the marker pattern file of ARToolkit.
	 * Place the file to "data" directory at sketch.
	 * The marker resolution must be 16x16.
	 * <br/>JP:
	 * マーカのパターンファイル名を指定します。パターンファイルは、dataディレクトリにおいて下さい。
	 * マーカの解像度は、16x16である必要があります。
	 * @param i_patt_width
	 * The length of one side of a square marker in millimeter unit.
	 * <br/>JP:
	 * マーカのサイズを指定します。単位はmmです。
	 * @param i_projection_coord_system
	 * Coordinate system flag of projection Matrix. Should be NyARBoard.CS_RIGHT or NyARBoard.Left(default)
	 * <br/>JP:
	 * Projection Matrixの座標系を指定します。NyARBoard.CS_RIGHT か NyARBoard.Left(規定値)を指定します。
	 */
	public NyARBoard(PApplet parent, int i_width,int i_height,String i_cparam,String i_patt,int i_patt_width,int i_projection_coord_system)
	{
		super(parent,i_cparam,i_width,i_height,i_projection_coord_system);
		initInstance(i_width,i_height,i_patt,i_patt_width);
		return;
	}
	/**
	 * This function is constructor same as i_projection_coord_system=CS_LEFT.
	 * <br/>JP:
	 * この関数はi_projection_coord_systemにCS_LEFTを設定するコンストラクタです。
	 * @param parent
	 * Specify processing instance.
	 * <br/>JP:
	 * processingのインスタンスを指定します。
	 * @param i_width
	 * Width of source image size for "detect()".
	 * <br/>JP:
	 * detect()に渡す入力画像の幅を指定します。
	 * @param i_htight
	 * Height of source image size for "detect()".
	 * <br/>JP:
	 * detectに渡す入力画像の高さを指定します。
	 * @param i_cparam
	 * The file name of the camera parameter of ARToolKit format.
	 * Place the file to "data" directory at sketch.
	 * <br/>JP:
	 * ARToolKitのパラメータファイル名を指定します。パラメータファイルはdataディレクトリにおいて下さい。
	 * @param i_patt
	 * The file name of the marker pattern file of ARToolkit.
	 * Place the file to "data" directory at sketch.
	 * The marker resolution must be 16x16.
	 * <br/>JP:
	 * マーカのパターンファイル名を指定します。パターンファイルは、dataディレクトリにおいて下さい。
	 * マーカの解像度は、16x16である必要があります。
	 * @param i_patt_width
	 * The length of one side of a square marker in millimeter unit.
	 * <br/>JP:
	 * マーカのサイズを指定します。単位はmmです。
	 */	
	public NyARBoard(PApplet parent, int i_width,int i_height,String i_cparam,String i_patt,int i_patt_width)
	{
		super(parent,i_cparam,i_width,i_height,CS_LEFT);
		initInstance(i_width,i_height,i_patt,i_patt_width);
		return;
	}
	
	private void initInstance(int i_width,int i_height,String i_patt,int i_patt_width)
	{
		try{
			this._raster=new PImageRaster(i_width, i_height);
			NyARCode code=new NyARCode(16,16);
			code.loadARPatt(this._pa.createInput(i_patt));
			this._nya=new NyARSingleDetectMarker(this._ar_param,code,i_patt_width,this._raster.getBufferType());
		}catch(NyARException e){
			this._pa.die("Error while setting up NyARToolkit for java", e);
		}
		return;
	}
	
	/**
	 * This function detect a marker which is must higher confidence in i_image.
	 * When function detects marker, properties (pos2d,angle,trans,confidence,transmat) are updated.
	 * <br/>JP:
	 * i_imageから最も一致度の高いマーカを検出し、cfThreshold以上の一致度であれば、
	 * pos2d,angle,trans,confidence,transmatのプロパティを更新します。
	 * @param i_image
	 * Specify source image.
	 * <br/>JP:
	 * 検出するイメージを設定します。
	 * @return
	 * TRUE if marker found;otherwise FALSE.
	 * <br/>JP:
	 * マーカが検出され、有効な値が得られればTRUEを返します。
	 * TRUEであれば、プロパティが更新されています。
	 */
	public boolean detect(PImage i_image)
	{
		boolean is_marker_exist=false;
		try{
			this._raster.wrapBuffer(i_image);
			//マーカの検出をするよ。
			is_marker_exist = this._nya.detectMarkerLite(this._raster,this.gsThreshold);
			//マーカ見つかったら一致度確認
			if(is_marker_exist){
				double cf=this._nya.getConfidence();
				if(cf<this.cfThreshold){
					is_marker_exist=false;
				}
			}
			//マーカとして処理？
			if(is_marker_exist){
				final NyARDoublePoint2d[] pts=this._nya.refSquare().sqvertex;
				for(int i=0;i<4;i++){
					this.pos2d[i][0]=(int)pts[i].x;
					this.pos2d[i][1]=(int)pts[i].y;
				}
				this.confidence=this._nya.getConfidence();
				this.lostCount=0;
				//座標変換
				this._nya.getTransmationMatrix(this._result);
				updateTransmat(this._result);

			}else if(this.lostCount<this.lostDelay){
				this.lostCount++;
				is_marker_exist=true;
			}else{
				is_marker_exist=false;				
			}
			return is_marker_exist;
		}catch(NyARException e){
			this._pa.die("Error while marker detecting up NyARToolkit for java", e);
		}
		return is_marker_exist;
	}
	//キャッシュたち
	private GL _gl=null;
	private PGraphicsOpenGL _pgl=null;

	/**
	 * This function sets corresponding transform matrix to the surface of the marker to OpenGL.
	 * The coordinate system of processing moves to the surface of the marker when this function is executed.
	 * Must return the coordinate system by using endTransform function at the end.
	 * <br/>JP:
	 * 座標変換を実行したMatrixを準備します。
	 * この関数を実行すると、processingの座標系がマーカ表面に設定されます。
	 * 描画終了後には、必ずendTransform関数を呼び出して座標系を戻してください。
	 * @param i_pgl
	 * Specify PGraphicsOpenGL instance.
	 * Please cast and set a "g" member of processing.
	 * <br/>JP:
	 * PGraphicsOpenGLインスタンスを設定します。processingのgメンバをキャストして設定してください。
	 */
	public void beginTransform(PGraphicsOpenGL i_pgl)
	{
		if(this._gl!=null){
			this._pa.die("The function beginTransform is already called.", null);			
		}
		this._pgl=i_pgl;
		beginTransform(i_pgl.beginGL());
		return;
	}
	public void beginTransform(GL i_gl)
	{
		if(this._gl!=null){
			this._pa.die("The function beginTransform is already called.", null);			
		}
		
		this._gl=i_gl;
		i_gl.glMatrixMode(GL.GL_PROJECTION);
		this._pa.pushMatrix();
		this._pa.resetMatrix();
		i_gl.glLoadMatrixd(this.projection,0);
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		this._pa.pushMatrix();
		this._pa.resetMatrix();
		i_gl.glLoadMatrixd(this.transmat,0);
				
		this._pa.pushMatrix();
		return;
	}
	/**
	 * This function recover coordinate system that was changed by beginTransform function.
	 * <br/>JP:
	 * beginTransformによる座標変換を解除して元に戻します。
	 */
	public void endTransform()
	{
		if(this._gl==null){
			this._pa.die("The function beginTransform is never called.", null);			
		}
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
	private final NyARDoublePoint3d _tmp_d3p=new NyARDoublePoint3d();

	private void updateTransmat(NyARTransMatResult i_src)
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
		
		return;	
	}
}
