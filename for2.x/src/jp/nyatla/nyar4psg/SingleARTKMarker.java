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
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.processor.SingleARMarkerProcesser;



/**
 * このクラスは、登録した複数のマーカのうち、同時に1個を認識するクラスです。
 * NyARToolKitの{@link SingleARMarkerProcesser}を使用したクラスです。
 * 入力画像から、事前に登録したパターンに最も一致するマーカ1個を認識して、その座標と認識したマーカの番号を出力します。
 * 
 */
public class SingleARTKMarker extends SingleMarkerBaseClass
{
	/**　detectの返すステータス値です。*/
	public static final int ST_NOMARKER    =0;
	/**　detectの返すステータス値です。*/
	public static final int ST_NEWMARKER   =1;
	/**　detectの返すステータス値です。*/
	public static final int ST_UPDATEMARKER=2;
	/** detectの返すステータス値です。*/
	public static final int ST_REMOVEMARKER=3;

	/**
	 * [readonly]
	 * この変数は互換性の為に残されています。{@link #getMarkerId}を使用してください。
	 * @deprecated
	 */
	public int markerid;

	/**
	 * 検出したマーカのIDを示します。この値は、setARCodesで登録したマーカの0からはじまる
	 * 順番を返します。たとえば、マーカを1個しか登録しなかった場合、常に0となります。
	 */
	public int getMarkerId()
	{
		return this.markerid;
	}

	
	private MarkerProcessor _marker_proc;
	private boolean _registerd_marker=false;

	/**
	 * コンストラクタです。
	 * @param parent
	 * @param i_width
	 * @param i_height
	 * @param i_cparam
	 * @param i_config
	 * コンフィギュレーションオブジェクトを指定します。
	 * このクラスは、{@link NyAR4PsgConfig#env_transmat_mode}の値を無視します。（常に{@link NyAR4PsgConfig#TM_NYARTK}を使います。 ）
	 */
	public SingleARTKMarker(PApplet parent, int i_width,int i_height,String i_cparam,NyAR4PsgConfig i_config)
	{
		super();
		try{
			this.initInstance(parent, i_cparam, i_width, i_height, i_config);
			this._marker_proc=new MarkerProcessor(this,this._ar_param,this._src_raster.getBufferType());
		}catch(NyARException e){
			this._ref_papplet.die("Error on SingleARTKMarker",e);
		}
		return;
	}
	public SingleARTKMarker(PApplet parent, int i_width,int i_height,String i_cparam)
	{
		super();
		try{
			this.initInstance(parent, i_cparam, i_width, i_height,NyAR4PsgConfig.CONFIG_DEFAULT);
			this._marker_proc=new MarkerProcessor(this,this._ar_param,this._src_raster.getBufferType());
		}catch(NyARException e){
			this._ref_papplet.die("Error on SingleARTKMarker",e);
		}
		return;
	}	
	/**
	 * この関数は、1個のARマーカをテーブルに登録します。
	 * コンストラクタの後で1度だけ呼び出してください。
	 * @param i_patt_name
	 * マーカパターンの名前配列を指定します。配列の先頭から、マーカーのIDは、0,1,2...の順になります。
	 * 登録できるマーカの種類は、エッジサイズ25%、解像度16x16のみです。
	 * @param i_patt_size
	 * マーカの物理サイズを[mm]で指定します。
	 */
	public void setARCodes(String i_patt_name,int i_patt_size)
	{
		if(this._registerd_marker)
		{
			this._ref_papplet.die("Error already called setARCodes.", new NyARException());
		}
		try{
	        NyARCode[] codes=new NyARCode[1];
	        codes[0]=NyARCode.createFromARPattFile(this._ref_papplet.createInput(i_patt_name),16,16);
	        this._marker_proc.setARCodeTable(codes,16,i_patt_size);
	        this._registerd_marker=true;
		}catch(NyARException e){
			this._ref_papplet.die("Error on setARCodes",e);
		}	        
		return;	
	}
	/**
	 * この関数は、マーカリストをテーブルに登録します。
	 * コンストラクタの後で1度だけ呼び出してください。
	 * @param i_patt_names
	 * マーカパターンの名前配列を指定します。配列の先頭から、マーカーのIDは、0,1,2...の順になります。
	 * 登録できるマーカの種類は、エッジサイズ25%、解像度16x16のみです。
	 * @param i_marker_size
	 * マーカの物理サイズを[mm]で指定します。このサイズは、全てのマーカで共通です。
	 */
	public void setARCodes(String[] i_patt_names,int i_marker_size)
	{
		if(this._registerd_marker)
		{
			this._ref_papplet.die("Error already called setARCodes.", new NyARException());
		}
		try{
	        NyARCode[] codes=new NyARCode[i_patt_names.length];
	        for(int i=0;i<i_patt_names.length;i++){
	            codes[i]=NyARCode.createFromARPattFile(this._ref_papplet.createInput(i_patt_names[i]),16,16);
	        }
	        this._marker_proc.setARCodeTable(codes,16,i_marker_size);
	        this._registerd_marker=true;
		}catch(NyARException e){
			this._ref_papplet.die("Error on setARCodes",e);
		}	        
		return;	
	}
	/**
	 * この関数は、マーカパターンの一致度敷居値を指定します。
	 * この敷居値よりも低いマーカは、認識しません。
	 * @param i_new_cf
	 * 新しくマーカを認識するために必要なマーカノ一致度です。値範囲は、0&lt;&lt;n&lt;100です。
	 * @param i_exist_cf
	 * 継続してマーカを認識するために必要なマーカノ一致度です。値範囲は、0&lt;&lt;n&lt;100です。
	 * i_new_cfより低い値を指定してください。
	 */
	public void setConfidenceThreshold(double i_new_cf,double i_exist_cf)
	{
		this._marker_proc.setConfidenceThreshold(i_new_cf, i_exist_cf);
		return;
	}
	/**
	 * 画像から、マーカの認識処理を行い、プロパティを更新します。
	 * 認識処理の結果は、戻り値にステータスコードで格納します。
	 * 二値化敷居値はPTail法により、自動的に決定します。
	 * 関数は、i_imageに対して1度だけ{@link PImage#loadPixels()}を実行します。
	 * {@link PImage#loadPixels()}のタイミングをコントロールしたい場合は、{@link #detectWithoutLoadPixels}を使用してください。
	 * @param i_image
	 * 検出処理を行う画像を指定します。
	 * @return
	 * ステータスコードを返します。
	 * <ul>
	 * <li>{@link #ST_NOMARKER} -
	 * マーカが認識されていない事を示します。
	 * マーカパラメータのメンバ変数は使用不可能です。</li>
	 * <li>{@link #ST_NEWMARKER} -
	 * マーカが発見された事を示します。
	 * transmat,angle,trans,markeridメンバ変数が利用可能です。</li>
	 * <li>{@link #ST_UPDATEMARKER} -
	 * マーカ座標が更新されたことを示します。
	 * transmat,angle,trans,markeridメンバ変数が利用可能です。
	 * </li>
	 * <li>{@link #ST_REMOVEMARKER} -
	 * マーカが消失したことを示します。
	 * マーカパラメータのメンバ変数は使用不可能です。
	 * </li>
	 */
	public int detect(PImage i_image)
	{
		i_image.loadPixels();
		return this.detectWithoutLoadPixels(i_image);
	}
	/**
	 * {@link PImage#loadPixels()}を伴わない{@link detect()}です。
	 * 引数と戻り値の詳細は、{@link #detect(PImage)}を参照してください。
	 * @param i_image
	 * @see #detect(PImage)
	 */
	public int detectWithoutLoadPixels(PImage i_image)	{
		if(!this._registerd_marker){
			this._ref_papplet.die("Must call setARCodes function in the first.");
		}
		try{
			this._src_raster.wrapBuffer(i_image);
			this._marker_proc.initSequence();
			this._marker_proc.detectMarker(this._src_raster);
			//ステータスチェック
			switch(this._marker_proc.status){
			case ST_NOMARKER:
			case ST_REMOVEMARKER:
				this.markerid=-1;
				break;
			case ST_NEWMARKER:
				this.markerid=this._marker_proc.current_code;
				break;
			case ST_UPDATEMARKER:
				break;
			default:
				throw new NyARException();
			}
		}catch(NyARException e){
			this._ref_papplet.die("Error while marker detecting up NyARToolkit for java", e);
		}
		return this._marker_proc.status;
	}
	/**
	 * {@link SingleARTKMarker}用にカスタマイズした{@link SingleARMarkerProcesser}です。
	 */
	private class MarkerProcessor extends SingleARMarkerProcesser
	{	
		private SingleMarkerBaseClass _parent;
		public int current_code=-1;
		public int status;
		
		private boolean _is_prev_onenter;

		public MarkerProcessor(SingleMarkerBaseClass i_parent,NyARParam i_cparam,int i_raster_format) throws NyARException
		{
			//アプリケーションフレームワークの初期化
			super();
			this._parent=i_parent;
			initInstance(i_cparam);
			this.status=SingleARTKMarker.ST_NOMARKER;
			return;
		}
		public void initSequence()
		{
			this.status=this.status==SingleARTKMarker.ST_REMOVEMARKER?SingleARTKMarker.ST_NOMARKER:this.status;
		}
		protected void onEnterHandler(int i_code)
		{
			current_code=i_code;
			this.status=SingleARTKMarker.ST_NEWMARKER;
			this._is_prev_onenter=true;
		}
		protected void onLeaveHandler()
		{
			current_code=-1;
			this.status=SingleARTKMarker.ST_REMOVEMARKER;
			return;			
		}

		protected void onUpdateHandler(NyARSquare i_square, NyARDoubleMatrix44 result)
		{
			this._parent.updateTransmat(i_square, result);
			this.status=this._is_prev_onenter?SingleARTKMarker.ST_NEWMARKER:SingleARTKMarker.ST_UPDATEMARKER;
			this._is_prev_onenter=false;
		}

	}	
}

