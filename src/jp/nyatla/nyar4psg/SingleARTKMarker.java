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
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.processor.SingleARMarkerProcesser;



/**
 * このクラスは、１個のARToolKitマーカを認識することができます。
 * 入力画像から、事前に登録したパターンに最も一致するマーカ1個を認識して、
 * その座標と認識したマーカの番号を出力します。
 * <br/>EN:
 * -
 * @author nyatla
 *
 */
public class SingleARTKMarker extends SingleMarkerBaseClass
{
	/**
	 * detectの返すステータス値です。
	 * <br/>EN:
	 * -
	 */
	public static final int ST_NOMARKER    =0;
	/**
	 * detectの返すステータス値です。
	 * <br/>EN:
	 * -
	 */
	public static final int ST_NEWMARKER   =1;
	/**
	 * detectの返すステータス値です。
	 * <br/>EN:
	 * -
	 */
	public static final int ST_UPDATEMARKER=2;
	/**
	 * detectの返すステータス値です。
	 * <br/>EN:
	 * -
	 */
	public static final int ST_REMOVEMARKER=3;

	/**
	 * 検出したマーカのIDを示します。この値は、setARCodesで登録したマーカの0からはじまる
	 * 順番を返します。たとえば、マーカを1個しか登録しなかった場合、常に0となります。
	 * <br/>EN:
	 * -
	 */
	public int markerid;

	
	private PImageRaster _raster;
	private MarkerProcessor _marker_proc;
	private boolean _registerd_marker=false;

	/**
	 * インスタンスを作成します。
	 * <br/>EN:
	 * -
	 * @param parent
	 * <br/>EN:
	 * -
	 * @param i_width
	 * <br/>EN:
	 * -
	 * @param i_height
	 * <br/>EN:
	 * -
	 * @param i_cparam
	 * <br/>EN:
	 * -
	 * @param i_projection_coord_system
	 * <br/>EN:
	 * -
	 */
	public SingleARTKMarker(PApplet parent, int i_width,int i_height,String i_cparam,int i_projection_coord_system)
	{
		super(parent,i_cparam,i_width,i_height,i_projection_coord_system);
		try{
			this._raster=new PImageRaster(i_width,i_height);
			this._marker_proc=new MarkerProcessor(this._ar_param,this._raster.getBufferType());
		}catch(NyARException e){
			this._pa.die("Error on SingleARTKMarker",e);
		}
		return;
	}
	/**
	 * 1個のマーカを登録します。detect関数を呼び出す前に、1度だけ呼び出してください。
	 * <br/>EN:
	 * -
	 * @param i_patt_name
	 * マーカパターンの名前を指定します。マーカーのIDは、0になります。
	 * <br/>EN:
	 * -
	 * @param i_patt_size
	 * マーカのサイズをmm単位で指定します。
	 * <br/>EN:
	 * -
	 */
	public void setARCodes(String i_patt_name,int i_patt_size)
	{
		if(this._registerd_marker)
		{
			this._pa.die("Error already called setARCodes.", new NyARException());
		}
		try{
	        NyARCode[] codes=new NyARCode[1];
	        codes[0]=new NyARCode(16,16);
	        codes[0].loadARPatt(this._pa.createInput(i_patt_name));
	        this._marker_proc.setARCodeTable(codes,16,i_patt_size);
	        this._registerd_marker=true;
		}catch(NyARException e){
			this._pa.die("Error on setARCodes",e);
		}	        
		return;	
	}
	/**
	 * 複数のマーカを登録します。detect関数を呼び出す前に、1度だけ呼び出してください。
	 * <br/>EN:
	 * -
	 * @param i_patt_names
	 * マーカパターンの名前配列を指定します。配列の先頭から、マーカーのIDは、0,1,2...の順になります。
	 * <br/>EN:
	 * -
	 * @param i_patt_size
	 * マーカのサイズをmm単位で指定します。
	 * <br/>EN:
	 * -
	 */
	public void setARCodes(String[] i_patt_names,int i_patt_size)
	{
		if(this._registerd_marker)
		{
			this._pa.die("Error already called setARCodes.", new NyARException());
		}
		try{
	        NyARCode[] codes=new NyARCode[i_patt_names.length];
	        for(int i=0;i<i_patt_names.length;i++){
	            codes[i]=new NyARCode(16,16);
	            codes[i].loadARPatt(this._pa.createInput(i_patt_names[i]));        	
	        }
	        this._marker_proc.setARCodeTable(codes,16,i_patt_size);
	        this._registerd_marker=true;
		}catch(NyARException e){
			this._pa.die("Error on setARCodes",e);
		}	        
		return;	
	}	
	/**
	 * 認識処理を行うマーカの一致度を指定します。
	 * <br/>EN:
	 * -
	 * @param i_new_cf
	 * 初めてマーカを認識するときの敷居値を指定します。値範囲は、0&lt;&lt;n&lt;100です。
	 * <br/>EN:
	 * -
	 * @param i_exist_cf
	 * 連続してマーカを認識するときの敷居値を指定します。値範囲は、0&lt;&lt;n&lt;100です。
	 * i_new_cfより低い値を指定してください。
	 * <br/>EN:
	 * -
	 */
	public void setConfidenceThreshold(double i_new_cf,double i_exist_cf)
	{
		this._marker_proc.setConfidenceThreshold(i_new_cf, i_exist_cf);
		return;
	}
	/**
	 * 画像から、マーカの認識処理を行い、プロパティを更新します。
	 * 認識処理の結果は、戻り値にステータスコードで格納します。
	 * <br/>EN:
	 * -
	 * @param i_image
	 * <br/>EN:
	 * -
	 * @return
	 * ステータスコードを返します。
	 * <pre>
	 * ST_NOMARKER:
	 * マーカが認識されていない事を示します。
	 * マーカパラメータのメンバ変数は使用不可能です。
	 * </pre>
	 * <pre>
	 * ST_NEWMARKER:
	 * マーカが発見された事を示します。
	 * transmat,angle,trans,markeridメンバ変数が利用可能です。
	 * </pre>
	 * <pre>
	 * ST_UPDATEMARKER:
	 * マーカ座標が更新されたことを示します。
	 * transmat,angle,trans,markeridメンバ変数が利用可能です。
	 * </pre>
	 * <pre>
	 * ST_REMOVEMARKER:
	 * マーカが消失したことを示します。
	 * マーカパラメータのメンバ変数は使用不可能です。
	 * </pre>
	 * <br/>EN:
	 * -
	 */
	public int detect(PImage i_image)
	{
		if(!this._registerd_marker){
			this._pa.die("Must call setARCodes function in the first.");
		}
		try{
			this._raster.wrapBuffer(i_image);
			this._marker_proc.initSequence();
			this._marker_proc.detectMarker(this._raster);
			//ステータスチェック
			switch(this._marker_proc.status){
			case ST_NOMARKER:
			case ST_REMOVEMARKER:
				this.transmat=null;
				this.angle=null;
				this.pos2d=null;
				this.trans=null;
				this.markerid=-1;
				break;
			case ST_NEWMARKER:
				this.transmat=this._marker_proc.gltransmat;
				this.angle=this._marker_proc.angle;
				this.pos2d=this._marker_proc.pos2d;
				this.trans=this._marker_proc.trans;
				this.markerid=this._marker_proc.current_code;
				break;
			case ST_UPDATEMARKER:
				break;
			default:
				throw new NyARException();
			}
		}catch(NyARException e){
			this._pa.die("Error while marker detecting up NyARToolkit for java", e);
		}
		return this._marker_proc.status;
	}

	private class MarkerProcessor extends SingleARMarkerProcesser
	{	
		public double[] gltransmat=new double[16];
		public PVector angle=new PVector();
		public int[][] pos2d=new int[4][2];
		public PVector trans=new PVector();
		public int current_code=-1;
		public int status;
		
		private final NyARDoublePoint3d _tmp_d3p=new NyARDoublePoint3d();


		private boolean _is_prev_onenter;

		public MarkerProcessor(NyARParam i_cparam,int i_raster_format) throws NyARException
		{
			//アプリケーションフレームワークの初期化
			super();
			initInstance(i_cparam,i_raster_format);
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

		protected void onUpdateHandler(NyARSquare i_square, NyARTransMatResult result)
		{
			matResult2GLArray(result, gltransmat);
			//angle
			result.getZXYAngle(this._tmp_d3p);
			
			this.angle.x=(float)this._tmp_d3p.x;
			this.angle.y=(float)this._tmp_d3p.y;
			this.angle.z=(float)this._tmp_d3p.z;
			//trans
			this.trans.x=(float)result.m03;
			this.trans.y=(float)result.m13;
			this.trans.z=(float)result.m23;
			
			for(int i=0;i<4;i++){
				this.pos2d[i][0]=(int)i_square.sqvertex[i].x;
				this.pos2d[i][1]=(int)i_square.sqvertex[i].y;
			}

			this.status=this._is_prev_onenter?SingleARTKMarker.ST_NEWMARKER:SingleARTKMarker.ST_UPDATEMARKER;
			this._is_prev_onenter=false;
		}
	}	
}

