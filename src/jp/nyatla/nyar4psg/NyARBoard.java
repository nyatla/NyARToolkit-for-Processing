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
 * このクラスは、マーカを１枚の板に見立てて、そこを中心にした座標系を計算します。
 * PImageから1個のARマーカを検出します。
 *  * 同時検出可能なマーカは１種類、１個です。
 * <p>
 * NyARBoardは、２つの外部設定ファイル"マーカファイル"と"カメラパラメータ"ファイルを必要とします。
 * これらはARToolKitのものと互換性があります。
 * これらはスケッチのdataディレクトリ以下に配置をして下さい。（exampleを見てください。）
 * </p>
 */
public class NyARBoard extends SingleMarkerBaseClass
{
	/**
	 * この値は、マーカ消失時の遅延数を設定します。
	 * 値は0以上を指定します。
	 */
	public int lostDelay =10;
	/**
	 * [read only]連続でマーカを見失った回数。この値は0&lt;n&lt;lostDelayをとります。
	 */
	public int lostCount = 0;
	/**
	 * マーカの座標変換を行う閾値です。
	 * 0.0&lt;n&lt;1.0の値をとります。
	 * この数値より一致度が大きい場合のみ、マーカを検出したと判定され、座標計算が行われます。
	 */
	public double cfThreshold=0.4;
	/**
	 * [read only]検出したマーカの一致度です。
	 * 0&lt;n&lt;1.0の値を取ります。
	 */
	public double confidence=0.0;
	/**
	 * マーカ検出時のグレースケール閾値を指定します。この値は、0&lt;=n&lt;=255をります。
	 */
	public int gsThreshold=110;
	
	
	
	private final NyARTransMatResult _result=new NyARTransMatResult();
	private NyARSingleDetectMarker _nya;
	private PImageRaster _raster;
	
	/**
	 * この関数はコンストラクタです。
	 * @param parent
	 * processingの{@link PApplet}インスタンスをしていします。
	 * @param i_width
	 * detect()に渡す入力画像の幅を指定します。
	 * @param i_htight
	 * detectに渡す入力画像の高さを指定します。
	 * @param i_cparam
	 * ARToolKitのパラメータファイル名を指定します。パラメータファイルはdataディレクトリにおいて下さい。
	 * @param i_patt
	 * マーカのパターンファイル名を指定します。パターンファイルは、dataディレクトリにおいて下さい。
	 * マーカの解像度は、16x16である必要があります。
	 * @param i_patt_width
	 * マーカのサイズを指定します。単位はmmです。
	 * @param i_coord_system
	 * 座標系を指定します。{@link NyARBoard#CS_RIGHT_HAND} か {@link NyARBoard.CS_LEFT_HAND}(規定値)を指定します。
	 * nyar4psg/0.2.xのCS_LEFTと互換性のある値は、{@link NyARBoard#CS_RIGHT_HAND}です。nyar4psg/0.2.xのCS_RIGHTと互換性のある値はありません。
	 */
	public NyARBoard(PApplet parent, int i_width,int i_height,String i_cparam,String i_patt,int i_patt_width,int i_coord_system)
	{
		super(parent,i_cparam,i_width,i_height,i_coord_system);
		initInstance(i_width,i_height,i_patt,i_patt_width);
		return;
	}
	/**
	 * この関数はコンストラクタです。
	 * i_projection_coord_system引数にCS_LEFTを設定するコンストラクタと同一です。
	 * @param parent
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, int, int)}を参照してください。
	 * @param i_width
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, int, int)}を参照してください。
	 * @param i_height
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, int, int)}を参照してください。
	 * @param i_cparam
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, int, int)}を参照してください。
	 * @param i_patt
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, int, int)}を参照してください。
	 * @param i_patt_width
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, int, int)}を参照してください。
	 */	
	public NyARBoard(PApplet parent, int i_width,int i_height,String i_cparam,String i_patt,int i_patt_width)
	{
		super(parent,i_cparam,i_width,i_height,NyARBoard.CS_RIGHT_HAND);
		initInstance(i_width,i_height,i_patt,i_patt_width);
		return;
	}
	
	private void initInstance(int i_width,int i_height,String i_patt,int i_marker_width)
	{
		try{
			this._raster=new PImageRaster(i_width, i_height);
			NyARCode code=new NyARCode(16,16);
			code.loadARPatt(this._ref_papplet.createInput(i_patt));
			this._nya=new NyARSingleDetectMarker(this._ar_param,code,i_marker_width,this._raster.getBufferType());
		}catch(NyARException e){
			this._ref_papplet.die("Error while setting up NyARToolkit for java", e);
		}
		return;
	}
	
	/**
	 * i_imageから最も一致度の高いマーカを検出し、cfThreshold以上の一致度であれば、
	 * pos2d,angle,trans,confidence,transmatのプロパティを更新します。
	 * @param i_image
	 * 検出する画像を指定します。この画像は、入力画像に設定した値と同じでなければなりません。
	 * @return
	 * マーカが検出され、有効な値が得られればTRUEを返します。
	 * TRUEの時には、新しいマーカの位置情報が更新されます。
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
			this._ref_papplet.die("Error while marker detecting up NyARToolkit for java", e);
		}
		return is_marker_exist;
	}



}
