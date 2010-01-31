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
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.detector.*;



/**
 * このクラスは、PImageからARToolKit準拠の変換行列を求めるクラスです。
 * マーカを１枚の板に見立てて、そこを中心にした座標系を計算します。
 * 同時検出可能なマーカは１種類、１個です。
 * <pre>
 * NyARBoardは、２つの外部設定ファイル"マーカファイル"と"カメラパラメータ"ファイルを必要とします。
 * これらはARToolKitのものと互換性があります。
 * これらはスケッチのdataディレクトリ以下に配置をして下さい。（exampleを見てください。）
 * </pre>
 * <br/>EN:
 * This class calculate a ARToolKit base transformation matrix from PImage.
 * This class handles one marker, and calculates the transformation matrix that displays the object above the marker.
 * <pre>
 * NyARBoard needs two external configuration file "Marker file" and "Camera parameter" files.
 * These are compatible with the original ARToolKit. 
 * Place those files to under the data directory of the sketch. (see example.)
 * </pre>
 * <br/>JP:
 * @author nyatla
 *
 */
public class NyARBoard extends SingleMarkerBaseClass
{
	/**
	 * 消失遅延数。マーカ消失後、指定回数は過去の情報を維持します。
	 * 値は0以上であること。
	 * <br/>EN:
	 * NyARBoard ignores that it lost the marker while under specified number.
	 * Must be "n&gt;=0".
	 */
	public int lostDelay =10;
	/**
	 * 連続でマーカを見失った回数。この値は0<n<lostDelayをとります。
	 * <br/>EN:
	 * It is a number in which it continuously lost the marker.
	 * This value range is "0&lt;n&lt;lostDelay"
	 */
	public int lostCount = 0;
	/**
	 * マーカの座標変換を行う閾値。0.0&lt;n&lt;1.0の値をとります。
	 * この数値より一致度が大きい場合のみ、マーカを検出したと判定され、座標計算が行われます。
	 * <br/>EN:
	 * The threshold value of marker pattern confidence.
	 * This value range is "0.0&lt;n&lt;1.0".
	 * When marker confidence is larger than this value, NyARBoard detects the marker.
	 */
	public double cfThreshold=0.4;
	/**
	 * 検出したマーカの一致度です。
	 * <br/>EN:
	 * The confidence value of detected marker.
	 */
	public double confidence=0.0;
	/**
	 * マーカ検出時のグレースケール閾値を指定します。この値は、0&lt;=n&lt;=255をります。
	 * <br/>EN:
	 * The threshold value of labeling process from gray scale image.
	 * This value range is "0&lt;=n&lt;=255".
	 */
	public int gsThreshold=110;
	
	
	
	private final NyARTransMatResult _result=new NyARTransMatResult();
	private NyARSingleDetectMarker _nya;
	private PImageRaster _raster;
	
	/**
	 * この関数はコンストラクタです。
	 * <br/>EN:
	 * This function is constructor.
	 * @param parent
	 * processingのインスタンスをしていします。
	 * <br/>EN:
	 * Specify processing instance.
	 * @param i_width
	 * detect()に渡す入力画像の幅を指定します。
	 * <br/>EN:
	 * Width of source image size for "detect()".
	 * @param i_htight
	 * detectに渡す入力画像の高さを指定します。
	 * <br/>EN:
	 * Height of source image size for "detect()".
	 * @param i_cparam
	 * ARToolKitのパラメータファイル名を指定します。パラメータファイルはdataディレクトリにおいて下さい。
	 * <br/>EN:
	 * The file name of the camera parameter of ARToolKit format.
	 * Place the file to "data" directory at sketch.
	 * @param i_patt
	 * マーカのパターンファイル名を指定します。パターンファイルは、dataディレクトリにおいて下さい。
	 * マーカの解像度は、16x16である必要があります。
	 * <br/>EN:
	 * The file name of the marker pattern file of ARToolkit.
	 * Place the file to "data" directory at sketch.
	 * The marker resolution must be 16x16.
	 * @param i_patt_width
	 * マーカのサイズを指定します。単位はmmです。
	 * <br/>EN:
	 * The length of one side of a square marker in millimeter unit.
	 * @param i_projection_coord_system
	 * Projection Matrixの座標系を指定します。NyARBoard.CS_RIGHT か NyARBoard.Left(規定値)を指定します。
	 * <br/>EN:
	 * Coordinate system flag of projection Matrix. Should be NyARBoard.CS_RIGHT or NyARBoard.Left(default)
	 */
	public NyARBoard(PApplet parent, int i_width,int i_height,String i_cparam,String i_patt,int i_patt_width,int i_projection_coord_system)
	{
		super(parent,i_cparam,i_width,i_height,i_projection_coord_system);
		initInstance(i_width,i_height,i_patt,i_patt_width);
		return;
	}
	/**
	 * この関数はi_projection_coord_systemにCS_LEFTを設定するコンストラクタです。
	 * <br/>EN:
	 * This function is constructor same as i_projection_coord_system=CS_LEFT.
	 * @param parent
	 * processingのインスタンスを指定します。
	 * <br/>EN:
	 * Specify processing instance.
	 * @param i_width
	 * detect()に渡す入力画像の幅を指定します。
	 * <br/>EN:
	 * Width of source image size for "detect()".
	 * @param i_htight
	 * detectに渡す入力画像の高さを指定します。
	 * <br/>EN:
	 * Height of source image size for "detect()".
	 * @param i_cparam
	 * ARToolKitのパラメータファイル名を指定します。パラメータファイルはdataディレクトリにおいて下さい。
	 * <br/>EN:
	 * The file name of the camera parameter of ARToolKit format.
	 * Place the file to "data" directory at sketch.
	 * @param i_patt
	 * マーカのパターンファイル名を指定します。パターンファイルは、dataディレクトリにおいて下さい。
	 * マーカの解像度は、16x16である必要があります。
	 * <br/>EN:
	 * The file name of the marker pattern file of ARToolkit.
	 * Place the file to "data" directory at sketch.
	 * The marker resolution must be 16x16.
	 * @param i_patt_width
	 * マーカのサイズを指定します。単位はmmです。
	 * <br/>EN:
	 * The length of one side of a square marker in millimeter unit.
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
			this.angle=new PVector();
			this.trans=new PVector();
			this.pos2d=new int[4][2];
			this.transmat=new double[16];			
			
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
	 * i_imageから最も一致度の高いマーカを検出し、cfThreshold以上の一致度であれば、
	 * pos2d,angle,trans,confidence,transmatのプロパティを更新します。
	 * <br/>EN:
	 * This function detect a marker which is must higher confidence in i_image.
	 * When function detects marker, properties (pos2d,angle,trans,confidence,transmat) are updated.
	 * @param i_image
	 * 検出するイメージを設定します。
	 * <br/>EN:
	 * Specify source image.
	 * @return
	 * マーカが検出され、有効な値が得られればTRUEを返します。
	 * TRUEであれば、プロパティが更新されています。
	 * <br/>EN:
	 * TRUE if marker found;otherwise FALSE.
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
				this.confidence=this._nya.getConfidence();
				this.lostCount=0;
				//座標変換
				this._nya.getTransmationMatrix(this._result);
				updateTransmat(this._nya.refSquare(), this._result);

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



}
