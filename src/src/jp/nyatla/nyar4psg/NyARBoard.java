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
	
	
	
	private NyARSingleDetectMarker _nya;
	
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
	 * @param i_config
	 * コンフィギュレーションオブジェクトを指定します。
	 */
	public NyARBoard(PApplet parent, int i_width,int i_height,String i_cparam,String i_patt,int i_patt_width,NyAR4PsgConfig i_config)
	{
		super();
		try{
			this.initInstance(parent, i_cparam,i_patt, i_width, i_height, i_patt_width, i_config);
		}catch(Exception e){
			this._ref_papplet.die("Error at NyARBoard", e);
		}
		return;
	}
	/**
	 * この関数はコンストラクタです。
	 * i_config引数に{@link NyAR4PsgConfig#CONFIG_DEFAULT}を設定するコンストラクタと同一です。
	 * @param parent
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, NyAR4PsgConfig)}を参照してください。
	 * @param i_width
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, NyAR4PsgConfig)}を参照してください。
	 * @param i_height
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, NyAR4PsgConfig)}を参照してください。
	 * @param i_cparam
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, NyAR4PsgConfig)}を参照してください。
	 * @param i_patt
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, NyAR4PsgConfig)}を参照してください。
	 * @param i_patt_width
	 * {@link NyARBoard#NyARBoard(PApplet, int, int, String, String, NyAR4PsgConfig)}を参照してください。
	 */	
	public NyARBoard(PApplet parent, int i_width,int i_height,String i_cparam,String i_patt,int i_patt_width)
	{
		super();
		try{
			this.initInstance(parent, i_cparam,i_patt,i_width, i_height, i_patt_width,NyAR4PsgConfig.CONFIG_DEFAULT);
		}catch(Exception e){
			this._ref_papplet.die("Error at NyARBoard", e);
		}
		return;		
	}
	
	private void initInstance(PApplet parent,String i_cparam,String i_patt,int i_width,int i_height,int i_marker_width,NyAR4PsgConfig i_config) throws NyARException
	{
		super.initInstance(parent, i_cparam, i_width, i_height, i_config);
		NyARCode code=NyARCode.createFromARPattFile(this._ref_papplet.createInput(i_patt),16,16);	
		//モード選択
		int tm_type=(i_config.env_transmat_mode==NyAR4PsgConfig.TM_ARTK)?NyARSingleDetectMarker.PF_ARTOOLKIT_COMPATIBLE:NyARSingleDetectMarker.PF_NYARTOOLKIT;
		this._nya=NyARSingleDetectMarker.createInstance(this._ar_param,code,i_marker_width,tm_type);
		this._nya.setContinueMode(true);
		return;
	}
	private final NyARTransMatResult _rettmp=new NyARTransMatResult();
	/**
	 * i_imageから最も一致度の高いマーカを検出し、cfThreshold以上の一致度であれば、
	 * pos2d,angle,trans,confidence,transmatのプロパティを更新します。
	 * @param i_image
	 * 検出する画像を指定します。この画像は、入力画像に設定した値と同じでなければなりません。
	 * 関数を実行する前に、i_imageの{@link PImage#loadPixels()}を実行してください。
	 * {@link PImage#loadPixels()}のタイミングをコントロールしたい場合は、{@link #detectWithoutLoadPixels}を使用してください。
	 * @return
	 * マーカが検出され、有効な値が得られればTRUEを返します。
	 * TRUEの時には、新しいマーカの位置情報が更新されます。
	 */
	public boolean detect(PImage i_image)
	{
		i_image.loadPixels();
		return this.detectWithoutLoadPixels(i_image);
	}
	/**
	 * {@link PImage#loadPixels()}を伴わない{@link detect()}です。
	 * 引数と戻り値の詳細は、{@link #detect(PImage)}を参照してください。
	 * @param i_image
	 * @return
	 * @see #detect(PImage)
	 */
	public boolean detectWithoutLoadPixels(PImage i_image)
	{
		boolean is_marker_exist=false;
		try{
			this._src_raster.wrapBuffer(i_image);
			//マーカの検出をするよ。
			is_marker_exist = this._nya.detectMarkerLite(this._src_raster,this.gsThreshold);
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
				this._nya.getTransmationMatrix(this._rettmp);
				updateTransmat(this._nya.refSquare(), this._rettmp);

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
