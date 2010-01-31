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
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.nyidmarker.data.INyIdMarkerData;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerDataEncoder_RawBit;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerData_RawBit;
import jp.nyatla.nyartoolkit.processor.*;



/**
 * このクラスは、１個のNyIdマーカを認識することができます。
 * 映像からマーカを１個検出し、そのID番号と行列を返します。
 * <pre>
 * </pre>
 * <br/>EN:
 * -
 * @author nyatla
 *
 */
public class SingleNyIdMarker extends SingleMarkerBaseClass
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
	 * 検知しているNyIdマーカのIDです。
	 * <br/>EN:
	 * -
	 */
	public int markerid;

	
	
	private PImageRaster _raster;
	private MarkerProcessor _marker_proc;
	private boolean _registerd_marker=false;

	/**
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
	public SingleNyIdMarker(PApplet parent, int i_width,int i_height,String i_cparam,int i_projection_coord_system)
	{
		super(parent,i_cparam,i_width,i_height,i_projection_coord_system);
		try{
			this._raster=new PImageRaster(i_width,i_height);		
		}catch(NyARException e){
			this._pa.die("Error on SingleNyIdMarker",e);
		}
	}
	/**
	 * Idマーカノサイズを設定します。
	 * <br/>EN:
	 * This function sets size of marker.
	 * @param i_width
	 * [mm]単位でのマーカサイズ
	 * <br/>EN:
	 * size of marker in [mm] unit.
	 */
	public void setIdMarkerSize(double i_width)
	{
		if(this._registerd_marker)
		{
			this._pa.die("Error already called setIdMarkerSize.", new NyARException());
		}
		try{
			this._marker_proc=new MarkerProcessor(this._ar_param,this._raster.getBufferType());
		}catch(NyARException e){
			this._pa.die("Error on setIdMarkerSize",e);
		}
		this._registerd_marker=true;
		return;
			
	}
	/**
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
			this._pa.die("Must call setIdMarkerSize function in the first.");
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
				this.markerid=this._marker_proc.current_id;
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


	private class MarkerProcessor extends SingleNyIdMarkerProcesser
	{	
		public double[] gltransmat=new double[16];
		public PVector angle=new PVector();
		public int[][] pos2d=new int[4][2];
		public PVector trans=new PVector();
		public int current_id=-1;
		public int status;
		
		private final NyARDoublePoint3d _tmp_d3p=new NyARDoublePoint3d();


		private boolean _is_prev_onenter;

		public void initSequence()
		{
			this.status=this.status==SingleNyIdMarker.ST_REMOVEMARKER?SingleNyIdMarker.ST_NOMARKER:this.status;
		}
		public MarkerProcessor(NyARParam i_cparam,int i_raster_format) throws NyARException
		{
			//アプリケーションフレームワークの初期化
			super();
			initInstance(i_cparam, new NyIdMarkerDataEncoder_RawBit(),100, i_raster_format);
			return;
		}
		protected void onLeaveHandler()
		{
			this.current_id=-1;
			this.status=SingleNyIdMarker.ST_REMOVEMARKER;
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
