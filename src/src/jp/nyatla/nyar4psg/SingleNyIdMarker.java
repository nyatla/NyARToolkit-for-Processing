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
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.nyidmarker.data.INyIdMarkerData;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerDataEncoder_RawBit;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerData_RawBit;
import jp.nyatla.nyartoolkit.processor.*;


/**
 * このクラスは、NyIdマーカを同時に1個を認識するクラスです。
 * NyARToolKitの{@link SingleNyIdMarkerProcesser}を使用したクラスです。
 * 入力画像から、NyIdマーカを1個検出し、そのID番号と行列を返します。
 */
public class SingleNyIdMarker extends SingleMarkerBaseClass
{
	/** detectの返すステータス値です。*/
	public static final int ST_NOMARKER    =0;
	/** detectの返すステータス値です。*/
	public static final int ST_NEWMARKER   =1;
	/** detectの返すステータス値です。*/
	public static final int ST_UPDATEMARKER=2;
	/** detectの返すステータス値です。*/
	public static final int ST_REMOVEMARKER=3;

	/**
	 * [readonly]
	 * この変数は互換性の為に残されています。{@link #getNyId}を使用してください。
	 * @deprecated
	 */	
	public int markerid;
	/**
	 * 検出しているNyIdマーカの番号を返します。
	 * @return
	 * NyId値
	 */
	public int getNyId()
	{
		return this.markerid;
	}

	
	
	private MarkerProcessor _marker_proc;
	private boolean _registerd_marker=false;

	/**
	 * コンストラクタです。
	 * @param parent
	 * {@link PApplet}を指定します。
	 * @param i_width
	 * 入力する映像サイズを指定します。
	 * @param i_height
	 * 入力する映像サイズを指定します。
	 * @param i_cparam
	 *　カメラパラメータファイル名を指定します。
	 * @param i_config
	 * コンフィギュレーションオブジェクトを指定します。
	 * このクラスは、{@link NyAR4PsgConfig#env_transmat_mode}の値を無視します。（常に{@link NyAR4PsgConfig#TM_NYARTK}を使います。 ）
	 */
	public SingleNyIdMarker(PApplet parent, int i_width,int i_height,String i_cparam,NyAR4PsgConfig i_config)
	{
		super();
		try{
			this.initInstance(parent,i_cparam,i_width,i_height,i_config);
		}catch(NyARException e){
			this._ref_papplet.die("Error on SingleNyIdMarker",e);
		}
	}
	/**
	 * コンストラクタです。
	 * {@link SingleNyIdMarker#SingleNyIdMarker(PApplet, int, int, String, NyAR4PsgConfig)}のコンフィギュレーションに、{@link NyAR4PsgConfig#CONFIG_DEFAULT}を指定した物と同じです。
	 * @param parent
	 * {@link SingleNyIdMarker#SingleNyIdMarker(PApplet, int, int, String, NyAR4PsgConfig)}を参照してください。
	 * @param i_width
	 * {@link SingleNyIdMarker#SingleNyIdMarker(PApplet, int, int, String, NyAR4PsgConfig)}を参照してください。
	 * @param i_height
	 * {@link SingleNyIdMarker#SingleNyIdMarker(PApplet, int, int, String, NyAR4PsgConfig)}を参照してください。
	 * @param i_cparam
	 * {@link SingleNyIdMarker#SingleNyIdMarker(PApplet, int, int, String, NyAR4PsgConfig)}を参照してください。
	 */
	public SingleNyIdMarker(PApplet parent, int i_width,int i_height,String i_cparam)
	{
		super();
		try{
			this.initInstance(parent,i_cparam,i_width,i_height,NyAR4PsgConfig.CONFIG_DEFAULT);
		}catch(NyARException e){
			this._ref_papplet.die("Error on SingleNyIdMarker",e);
		}
	}	

	/**
	 * この関数は、Idマーカのサイズを設定します。
	 * @param i_width
	 * Idマーカの物理サイズをmm単位で指定します。
	 */
	public void setIdMarkerSize(double i_width)
	{
		if(this._registerd_marker)
		{
			this._ref_papplet.die("Error already called setIdMarkerSize.", new NyARException());
		}
		try{
			this._marker_proc=new MarkerProcessor(this,this._ar_param,this._src_raster.getBufferType());
		}catch(NyARException e){
			this._ref_papplet.die("Error on setIdMarkerSize",e);
		}
		this._registerd_marker=true;
		return;
			
	}
	/**
	 * 画像から、マーカの認識処理を行い、プロパティを更新します。
	 * 認識処理の結果は、戻り値にステータスコードで格納します。
	 * 関数は、i_imageに対して1度だけ{@link PImage#loadPixels()}を実行します。
	 * {@link PImage#loadPixels()}のタイミングをコントロールしたい場合は、{@link #detectWithoutLoadPixels}を使用してください。
	 * @param i_image
	 * 検出処理を行う画像を指定します。
	 * @return
	 * ステータスコードを返します。
	 * <ul>
	 * <li>{@link #ST_NOMARKER} -
	 * マーカが認識されていない事を示します。
	 * マーカパラメータのメンバ変数は使用不可能です。
	 * </li>
	 * <li>{@link #ST_NEWMARKER} -
	 * マーカが発見された事を示します。
	 * transmat,angle,trans,markeridメンバ変数が利用可能です。
	 * </li>
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
	public int detectWithoutLoadPixels(PImage i_image)
	{
		if(!this._registerd_marker){
			this._ref_papplet.die("Must call setIdMarkerSize function in the first.");
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
				this.markerid=this._marker_proc.current_id;
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
	 * カスタマイズしたIdマーカです。
	 */
	private class MarkerProcessor extends SingleNyIdMarkerProcesser
	{	
		public int current_id=-1;
		public int status;
		private SingleMarkerBaseClass _parent;
		
		private boolean _is_prev_onenter;

		public void initSequence()
		{
			this.status=this.status==SingleNyIdMarker.ST_REMOVEMARKER?SingleNyIdMarker.ST_NOMARKER:this.status;
		}
		public MarkerProcessor(SingleMarkerBaseClass i_parent,NyARParam i_cparam,int i_raster_format) throws NyARException
		{
			//アプリケーションフレームワークの初期化
			super();
			this._parent=i_parent;
			initInstance(i_cparam, new NyIdMarkerDataEncoder_RawBit(),100);
			return;
		}
		protected void onLeaveHandler()
		{
			this.current_id=-1;
			this.status=SingleNyIdMarker.ST_REMOVEMARKER;
			return;			
		}

		protected void onUpdateHandler(NyARSquare i_square, NyARDoubleMatrix44 result)
		{
			this._parent.updateTransmat(i_square, result);
			this.status=this._is_prev_onenter?SingleNyIdMarker.ST_NEWMARKER:SingleNyIdMarker.ST_UPDATEMARKER;
			this._is_prev_onenter=false;
		}
		
        protected void onEnterHandler(INyIdMarkerData i_code)
        {
            NyIdMarkerData_RawBit code = (NyIdMarkerData_RawBit)i_code;
            if (code.length > 4)
            {
                //4バイト以上の時はint変換しない。
                this.current_id = -1;//undefined_id
            }
            else
            {
                this.current_id = 0;
                //最大4バイト繋げて１個のint値に変換
                for (int i = 0; i < code.length; i++)
                {
                    this.current_id = (this.current_id << 8) | code.packet[i];
                }
            }
			this.status=SingleNyIdMarker.ST_NEWMARKER;
			this._is_prev_onenter=true;
        }		

	}	
}
