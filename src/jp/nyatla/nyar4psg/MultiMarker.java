package jp.nyatla.nyar4psg;

import java.io.InputStream;
import java.util.ArrayList;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.analyzer.raster.threshold.NyARRasterThresholdAnalyzer_SlidePTile;
import jp.nyatla.nyartoolkit.core.match.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective_O2;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve192;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARPerspectiveRasterReader;
import jp.nyatla.nyartoolkit.core.squaredetect.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.nyidmarker.*;
import jp.nyatla.nyartoolkit.nyidmarker.data.*;
import processing.core.*;


/**
 * このクラスは、複数のマーカに対応したARToolKit管理クラスです。
 * 1映像中に異なる複数のマーカのあるユースケースで動作します。
 * 
 * 入力画像はPImage形式です。
 */
public class MultiMarker extends NyARPsgBaseClass
{
	/**
	 * マーカ情報を格納するためのクラスです。
	 */
	private class TMarkerData
	{
		public static final int MK_AR=0;
		public static final int MK_NyId=1;
		/** マーカタイプです。*/
		public final int mktype;
		/** MK_ARの情報。比較のための、ARToolKitマーカを格納します。*/
		public final NyARMatchPatt_Color_WITHOUT_PCA matchpatt;
		/** MK_ARの情報。パターンのサイズ。パターンの物理サイズをmm単位で格納します。*/
		public final double marker_size;
		/** MK_ARの情報。検出した矩形の格納変数。マーカの一致度を格納します。*/
		public double cf;
		/** MK_ARの情報。パターンの解像度。*/
		public final int patt_resolution;
		/** MK_ARの情報。パターンのエッジ割合。*/
		public final int patt_edge_percentage;
				
		/** MK_NyIdの情報。 反応するidの開始レンジ*/
		public final long nyid_range_s;
		/** MK_NyIdの情報。 反応するidの終了レンジ*/
		public final long nyid_range_e;
		/** MK_NyIdの情報。 実際のid値*/
		public long nyid;
		
		/** 検出した矩形の格納変数。理想形二次元座標を格納します。*/
		public final NyARSquare sq=new NyARSquare();
		/** 検出した矩形の格納変数。マーカの姿勢行列を格納します。*/
		public final NyARTransMatResult tmat=new NyARTransMatResult();
		/** 矩形の検出状態の格納変数。 連続して見失った回数を格納します。*/
		public int lost_count=Integer.MAX_VALUE;
		
		/**
		 * コンストラクタです。初期値からARマーカのインスタンスを生成します。
		 * @param i_patt
		 * @param i_patt_resolution
		 * @param i_patt_edge_percentage
		 * @param i_patt_size
		 * @throws NyARException
		 */
		public TMarkerData(InputStream i_patt,int i_patt_resolution,int i_patt_edge_percentage,double i_patt_size) throws NyARException
		{
			NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
			this.mktype=MK_AR;
			c.loadARPatt(i_patt);
			this.matchpatt=new NyARMatchPatt_Color_WITHOUT_PCA(c);
			this.marker_size=i_patt_size;
			this.patt_resolution=i_patt_resolution;
			this.patt_edge_percentage=i_patt_edge_percentage;
			//padding
			this.nyid_range_e=this.nyid_range_s=0;
			return;
		}
		/**
		 * コンストラクタです。初期値から、Idマーカのインスタンスを生成します。
		 * @param i_range_s
		 * @param i_range_e
		 * @param i_patt_size
		 * @throws NyARException
		 */
		public TMarkerData(int i_nyid_range_s,int i_nyid_range_e,double i_patt_size)
		{
			this.mktype=MK_NyId;
			this.marker_size=i_patt_size;
			this.nyid_range_s=i_nyid_range_s;
			this.nyid_range_e=i_nyid_range_e;
			//padding
			this.patt_resolution=0;
			this.patt_edge_percentage=0;
			this.matchpatt=null;
			return;
		}
	}
	
	/**
	 * この関数は、マーカパターン一致率の閾値を設定します。
	 * この値よりも一致率が低いマーカを認識しなくなります。
	 * デフォルト値は{@link #DEFAULT_CF_THRESHOLD}です。
	 * @param i_val
	 * 設定する値。0.0&lt;n&lt;1.0の値を設定します。
	 */
	public void setCfThreshold(double i_val)
	{
		this._rel_detector._cf_threshold=i_val;
	}
	/**
	 * この関数は、マーカ消失時の遅延数を設定します。
	 * ここに設定した回数以上、マーカが連続して認識できなかったときに、認識に失敗します。
	 * デフォルト値は、{@link #DEFAULT_LOST_DELAY}です。
	 * @param i_val
	 * 設定する値。1以上の数値が必要です。
	 */
	public void setLostDelay(int i_val)
	{
		this._rel_detector._max_lost_delay=i_val;
	}
	/**
	 * この関数は、画像2値化の敷居値を設定します。
	 * デフォルト値は{@link #THLESHOLD_AUTO}です。
	 * @param i_th
	 * 固定式位置を指定する場合は、0&lt;n&lt;256の値を指定します。
	 * 固定式位置以外に、次の自動敷居値を利用できます。
	 * <ul>
	 * <li>{@link #THLESHOLD_AUTO} - 敷居値決定に{@link #NyARRasterThresholdAnalyzer_SlidePTile}を使います。パラメータは15%、スキップ値は、入力画像/80です。
	 * </li>
	 * </ul>
	 * 
	 */
	public void setThreshold(int i_th)
	{
		this._threshold=i_th;
	}
	/**
	 * この関数は、現在の二値化敷居値を返します。
	 * 自動敷居値を選択している場合でも、その時の敷居値を取得できます。
	 * @return
	 */
	public int getCurrentThreshold()
	{
		//256スケールに直す。
		return this._current_th*256/192;
	}
	
	/** 初期値定数。マーカ一致度の最小敷居値を示します。*/
	public final static double DEFAULT_CF_THRESHOLD=0.4;
	/** 初期値定数。マーカ消失時の許容*/
	public final static int DEFAULT_LOST_DELAY=10;

	/** 敷居値の定数です。敷居値を自動決定します。*/
	public final static int THLESHOLD_AUTO=-1;
	
	/** 複数の解像度に対応したピックアップクラスです。必要に応じてピックアップインスタンスを生成します。
	 */
	private class MultiResolutionPattPickup
	{
		private class Item{
			private NyARColorPatt_Perspective_O2 _pickup;
			private NyARMatchPattDeviationColorData _patt_d;
			private int _patt_edge;
			public Item(int i_resolution,int i_edge_percentage)
			{
				int r=1;
				while(i_resolution*r<64){
					r*=2;
				}				
				this._patt_d=new NyARMatchPattDeviationColorData(i_resolution,i_resolution);
				this._pickup=new NyARColorPatt_Perspective_O2(i_resolution,i_resolution,r,i_edge_percentage,PImageRaster.BUFFER_TYPE);
				this._patt_edge=i_edge_percentage;
			}
		}
		private ArrayList<Item> items=new ArrayList<Item>();
		/**
		 * マーカにマッチした{@link NyARMatchPattDeviationColorData}インスタンスを得る。
		 */
		public NyARMatchPattDeviationColorData refDeviationColorData(TMarkerData i_marker)
		{
			int mk_resolution=i_marker.patt_resolution;
			int mk_edge=i_marker.patt_edge_percentage;
			Item item=null;
			for(int i=this.items.size()-1;i>=0;i--)
			{
				Item ptr=this.items.get(i);
				if(!ptr._pickup.getSize().isEqualSize(mk_resolution,mk_resolution) || ptr._patt_edge!=mk_edge)
				{
					//サイズとエッジサイズが合致しない物はスルー
					continue;
				}
				return this.items.get(i)._patt_d;
			}
			item=new Item(mk_resolution,mk_edge);
			this.items.add(item);
			return item._patt_d;
		}
		/**
		 * リストにあるパターン全ての差分パラメータを作成。
		 * @param i_image
		 * @param i_vertexs
		 * @return
		 * @throws NyARException
		 */
		public boolean makePattDeviationColorData(INyARRgbRaster i_image, NyARIntPoint2d[] i_vertexs) throws NyARException
		{
			for(int i=this.items.size()-1;i>=0;i--)
			{
				Item item=this.items.get(i);
				if(!item._pickup.pickFromRaster(i_image, i_vertexs)){
					return false;
				}
				item._patt_d.setRaster(item._pickup);
			}
			return true;
		}
		
	}
	/**
	 * {@link MultiMarker}向けの矩形検出器です。
	 */
	private class RleDetector extends NyARSquareContourDetector_Rle
	{
		private final NyIdMarkerPickup _id_pickup = new NyIdMarkerPickup();		
		private final MultiResolutionPattPickup _mpickup=new MultiResolutionPattPickup();
		private final NyARIntPoint2d[] _vertexs=new NyARIntPoint2d[4];
		private NyARCoord2Linear _coordline;
		private final NyARMatchPattResult _patt_result=new NyARMatchPattResult();;
		private INyARRgbRaster _ref_src_raster;
		private NyARRectOffset _offset=new NyARRectOffset();
		
		public final ArrayList<TMarkerData> marker_sl=new ArrayList<TMarkerData>();
		public double _cf_threshold=DEFAULT_CF_THRESHOLD;
		public int _max_lost_delay=DEFAULT_LOST_DELAY;
		public RleDetector(MultiMarker i_parent,NyARParam i_param) throws NyARException
		{
			super(i_param.getScreenSize());
			//インスタンスの生成
			this._coordline=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
		}
		
		//Idマーカのエンコーダなど
		private final NyIdMarkerPattern _id_patt=new NyIdMarkerPattern();
		private final NyIdMarkerParam _id_param=new NyIdMarkerParam();
		private final NyIdMarkerDataEncoder_RawBitId _id_encoder=new NyIdMarkerDataEncoder_RawBitId();
		private final NyIdMarkerData_RawBitId _id_data=new NyIdMarkerData_RawBitId();


	
		
		protected void onSquareDetect(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
		{
			//画像取得配列の生成
			for(int i2=0;i2<4;i2++){
				this._vertexs[i2]=i_coord.items[i_vertex_index[i2]];
			}
			//idピックアップを試行してみる。(itemとdirに矩形情報を保存してね。)
			if(this._id_pickup.pickFromRaster(this._ref_src_raster, this._vertexs, this._id_patt, this._id_param))
			{
			//NyIdの場合
				//エンコードしてみる
				if(!this._id_encoder.encode(this._id_patt,this._id_data)){
					return;
				}
				//対象NyIdがあるかチェック
				for(int i=marker_sl.size()-1;i>=0;i--)
				{
					TMarkerData item=marker_sl.get(i);
					if(item.mktype!=TMarkerData.MK_NyId){
						continue;
					}
					//レンジチェック
					long s=this._id_data.marker_id;
					if(item.nyid_range_s>s || s>item.nyid_range_e)
					{
						continue;
					}
					//一致したよー。
					item.nyid=s;
					item.lost_count=0;
					setItemSquare(this._id_param.direction,i_coord,i_vertex_index,item.sq);
					break;
				}
				//ここで終了
				return;
			}else{
			//ARマーカパターン
				//パターン作製
				if(!this._mpickup.makePattDeviationColorData(this._ref_src_raster,this._vertexs)){
					return;
				}
				//検出状態をリセットするよ。
				double best_cf=0;
				int best_id=-1;
				int best_dir=NyARMatchPattResult.DIRECTION_UNKNOWN;			
				//全てのマーカについて、一番一致したパターンIDを求める。
				for(int i=marker_sl.size()-1;i>=0;i--)
				{
					TMarkerData item=marker_sl.get(i);
					//マーカのパターン解像度に一致したサンプリング画像と比較する。
					if(!item.matchpatt.evaluate(this._mpickup.refDeviationColorData(item),this._patt_result)){
						continue;
					}
					//敷居値
					if(this._patt_result.confidence<this._cf_threshold)
					{
						continue;
					}				
					//一致率の比較
					if(best_cf>this._patt_result.confidence){
						continue;
					}
					//結果の保存
					best_cf=this._patt_result.confidence;
					best_dir=this._patt_result.direction;
					best_id=i;
				}
				if(best_id<0){
					return;
				}
				//一番評価の高いidで、過去のものより高評価なら差し替え
				TMarkerData item=this.marker_sl.get(best_id);
				if(item.cf>best_cf){
					return;
				}
				//結果をコピーする
				item.cf=best_cf;
				item.lost_count=0;
				setItemSquare(best_dir,i_coord,i_vertex_index,item.sq);
			}			
		}
		private void setItemSquare(int i_dir,NyARIntCoordinates i_coord,int[] i_vertex_index,NyARSquare i_sq) throws NyARException
		{
			for(int i=0;i<4;i++){
				int idx=(i+4 - i_dir) % 4;
				this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coord,i_sq.line[i]);
			}
			for (int i = 0; i < 4; i++) {
				//直線同士の交点計算
				if(!i_sq.line[i].crossPos(i_sq.line[(i + 3) % 4],i_sq.sqvertex[i])){
					throw new NyARException();//まずない。ありえない。
				}
			}			
		}
		/**
		 * 検出処理の準備
		 */
		public void prevDetection(INyARRgbRaster i_src)
		{
			this._ref_src_raster=i_src;
			//状態のリセット(ARマーカ)
			for(int i=this.marker_sl.size()-1;i>=0;i--){
				TMarkerData item=this.marker_sl.get(i);
				item.cf=0;
				if(item.lost_count<this._max_lost_delay){
					item.lost_count++;
				}
			}
			//状態のリセット(Idマーカ)
			
		}
		/**
		 * 検出の後処理
		 * @throws NyARException 
		 */
		public void finishDetection(NyARTransMat i_transmat) throws NyARException
		{
			//ARマーカ全てについて、変換行列を計算。
			for(int i=this.marker_sl.size()-1;i>=0;i--){
				TMarkerData item=this.marker_sl.get(i);
				this._offset.setSquare(item.marker_size);
				i_transmat.transMat(item.sq,this._offset, item.tmat);
			}
			//Idマーカについてはサイズが判らんので、
		}
	}
	/** 敷居値判定用 */
	private NyARRasterThresholdAnalyzer_SlidePTile _threshold_detect;
	
	private int _threshold=THLESHOLD_AUTO;
	/** 192スケールなので注意*/
	private int _current_th;
	private RleDetector _rel_detector;

	/** {@link PImage}をラップするラスタクラスを保持します。*/
	private PImageRaster _wrap_raster;
	/** 最後に{@link #detect}に入力した{@link PImage}を参照します。*/
	private PImage _ref_last_raster=null;
	private NyARGrayscaleRaster _gs_raster;
	private NyARRasterFilter_Rgb2Gs_RgbAve192 _tobin_filter;
	private NyARTransMat _transmat_inst;
	private final NyARIntRect _img_rect=new NyARIntRect();
	/**
	 * コンストラクタです。
	 * @param parent
	 * 親となるAppletオブジェクトを指定します。このOpenGLのレンダリングシステムを持つAppletである必要があります。
	 * @param i_cparam_file
	 * ARToolKitフォーマットのカメラパラメータファイルの名前を指定します。
	 * @param i_width
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_height
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_projection_coord_system
	 * ARToolKit座標系のタイプを指定します。{@link #CS_LEFT_HAND}か{@link #CS_RIGHT_HAND}を指定してください。
	 * @throws NyARException
	 */
	public MultiMarker(PApplet parent, int i_width,int i_height,String i_cparam_file,int i_projection_coord_system)
	{
		this.initInstance(parent, i_cparam_file, i_width, i_height, i_projection_coord_system);
	}
	/**
	 * コンストラクタです。
	 * 省略された入力画像サイズパラメータには、{@link PApplet#width}と{@link PApplet#height}を使います。
	 * @param parent
	 * {@link MultiMarker#MultiARTookitMarker(PApplet, int, int, String, int, int)}を参照。
	 * @param i_cparam_file
	 * {@link MultiMarker#MultiARTookitMarker(PApplet, int, int, String, int, int)}を参照。
	 * @param i_projection_coord_system
	 * {@link MultiMarker#MultiARTookitMarker(PApplet, int, int, String, int, int)}を参照。
	 * @throws NyARException
	 */
	public MultiMarker(PApplet parent,String i_cparam_file,int i_projection_coord_system)
	{
		this.initInstance(parent,i_cparam_file,parent.width,parent.height, i_projection_coord_system);
	}
	/**
	 * コンストラクタです。
	 * 省略された入力画像サイズパラメータには、{@link PApplet#width}と{@link PApplet#height}を使います。
	 * 座標系は、{@link #CS_RIGHT_HAND}が選択された物とします。
	 * @param parent
	 * {@link MultiMarker#MultiARTookitMarker(PApplet, int, int, String, int, int)}を参照。
	 * @param i_cparam_file
	 * {@link MultiMarker#MultiARTookitMarker(PApplet, int, int, String, int, int)}を参照。
	 * @throws NyARException
	 */
	public MultiMarker(PApplet parent,String i_cparam_file)
	{
		this.initInstance(parent, i_cparam_file, parent.width, parent.height, MultiMarker.CS_RIGHT_HAND);
	}
	private NyARPerspectiveRasterReader _preader;
	
	/**
	 * インスタンスを初期化します。
	 * @param parent
	 * @param i_width
	 * @param i_height
	 * @param i_cparam_file
	 * @param i_patt_resolution
	 * @param i_projection_coord_system
	 */
	protected void initInstance(PApplet parent,String i_cparam_file,int i_width,int i_height,int i_projection_coord_system)
	{
		super.initInstance(parent,i_cparam_file, i_width,i_height,i_projection_coord_system);
		try{
			this._rel_detector=new RleDetector(this,this._ar_param);
			this._wrap_raster=new PImageRaster(i_width,i_height);
			this._gs_raster=new NyARGrayscaleRaster(i_width,i_height,true);
			this._tobin_filter=new NyARRasterFilter_Rgb2Gs_RgbAve192(this._wrap_raster.getBufferType());
			this._transmat_inst=new NyARTransMat(this._ar_param);
			this._img_rect.setValue(0, 0, i_width, i_height);
			this._preader=new NyARPerspectiveRasterReader(this._wrap_raster.getBufferType());
			//
			int skip=i_height/120;
			this._threshold_detect=new NyARRasterThresholdAnalyzer_SlidePTile(15,this._gs_raster.getBufferType(),skip<1?1:skip);
		}catch(Exception e){
			e.printStackTrace();
			parent.die("Exception occurred at MultiARTookitMarker.initInstance");
		}
	}
	private PMatrix3D _old_matrix=null;
	/**
	 * この関数は、ProcessingのProjectionMatrixとModelview行列を、指定idのマーカ平面にセットします。
	 * 必ず{@link #endTransform}とペアで使います。
	 * 関数を実行すると、現在のModelView行列とProjection行列がインスタンスに保存され、新しい行列がセットされます。
	 * これらを復帰するには、{@link #endTransform}を使います。
	 * 復帰するまでの間は、再度{@link #beginTransform}を使うことはできません。
	 * <div>
	 * <div>この関数は、次のコードと等価です。</div>
	 * <hr/>
	 * :<br/>
	 * PMatrix3D prev_mat=setARPerspective();//prev_matは現在の行列退避用。<br/>
	 * pushMatrix();<br/>
	 * setMatrix(ar.getMarkerMatrix(i_id));<br/>
	 * :<br/>
	 * <hr/>
	 * </div>
	 * @param i_id
	 * マーカidを指定します。
	 */
	public void beginARMarkerTransform(int i_id)
	{
		if(this._old_matrix!=null){
			this._ref_papplet.die("The function beginTransform is already called.", null);			
		}
		//projectionの切り替え
		this._old_matrix=this.setARPerspective();
		//ModelViewの設定
		this._ref_papplet.pushMatrix();
		this._ref_papplet.setMatrix(this.getMarkerMatrix(i_id));
		return;	
	}
	/**
	 * この関数は、{@link #beginTransform}でセットしたProjectionとModelViewを元に戻します。
	 * この関数は、必ず{@link #beginTransform}とペアで使います。
	 * <div>この関数は、次のコードと等価です。</div>
	 * <hr/>
	 * :<br/>
	 * setPerspective(prev_mat);//prev_matはsetARPerspectiveで退避した行列。<br/>
	 * pushMatrix();<br/>
	 * setMatrix(ar.getMarkerMatrix());<br/>
	 * :<br/>
	 * <hr/>
	 * </div>
	 */
	public void endTransform()
	{
		if(this._old_matrix==null){
			this._ref_papplet.die("The function beginTransform is never called.", null);			
		}
		//ModelViewの復帰
		this._ref_papplet.popMatrix();
		//Projectionの復帰
		this.setPerspective(this._old_matrix);
		this._old_matrix=null;
		return;
	}
	/**
	 * この関数は、画像からマーカーの検出処理を実行します。
	 * @param i_image
	 * 検出処理を行う画像を指定します。
	 */
	public void detect(PImage i_image)
	{
		try{
			//RGBラスタをラップ
			this._ref_last_raster=i_image;
			this._wrap_raster.wrapBuffer(i_image);
			//GS変換
			this._tobin_filter.doFilter(this._wrap_raster,this._gs_raster);

			//敷居値決定
			if(this._threshold==THLESHOLD_AUTO){
				//GS画像から敷居値を計算(192スケールのGS画像から計算したthを256スケールに修正する。で、平均を計算)
				this._current_th=(this._threshold_detect.analyzeRaster(this._gs_raster)+this._current_th)/2;
			}else{
				//256スケールを192スケールに変換
				this._current_th=this._threshold*192/256;
			}

			//検出準備
			this._rel_detector.prevDetection(this._wrap_raster);

			//GS画像から検出
			this._rel_detector.detectMarker(this._gs_raster,this._img_rect,this._current_th);
			this._rel_detector.finishDetection(this._transmat_inst);
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Exception occurred at MultiARTookitMarker.detect");
		}
	}
	
	
	
	/**
	 * この関数は、ARToolKitスタイルのマーカーをファイルから読みだして、登録します。
	 * 同じパターンを複数回登録した場合には、最後に登録したものを優先して認識します。
	 * @param i_file_name
	 * マーカパターンファイル名を指定します。
	 * @param i_patt_resolution
	 * マーカパターンの解像度を指定します。
	 * @param i_edge_percentage
	 * マーカのエッジ幅を割合で指定します。
	 * 0&lt;n&lt;50の数値です。
	 * @param i_width
	 * マーカの物理サイズをmm単位で指定します。
	 * @return
	 * 0から始まるマーカーIDを返します。
	 * この数値は、マーカを区別するためのId値です。0から始まり、{@link #addARMarker}と{@link #addNyIdMarker}関数を呼ぶたびにインクリメントされます。
	 * {@link #getMarkerMatrix},{@link #getConfidence},{@link #isExistMarker},{@link #addARMarker},
	 * {@link #screen2MarkerCoordSystem},{@link #pickupMarkerImage},{@link #pickupRectMarkerImage}
	 * のid値に使います。
	 */
	public int addARMarker(String i_file_name,int i_patt_resolution,int i_edge_percentage,float i_width)
	{
		//初期化済みのアイテムを生成
		try{
			TMarkerData item=new TMarkerData(this._ref_papplet.createInput(i_file_name),i_patt_resolution,i_edge_percentage,i_width);
			this._rel_detector.marker_sl.add(item);
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Exception occurred at MultiARTookitMarker.addMarker");
		}
		return this._rel_detector.marker_sl.size()-1;
	}
	/**
	 * この関数は、ARToolKitスタイルのマーカーをファイルから読みだして、登録します。
	 * エッジ割合はARToolKitの標準マーカと同じ25%です。
	 * 重複するidを登録した場合には、最後に登録したidを優先して認識します。
	 * @param i_file_name
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 * @param i_patt_resolution
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 * @param i_width
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 * @return
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 */
	public int addARMarker(String i_file_name,int i_patt_resolution,float i_width)
	{
		return this.addARMarker(i_file_name, i_patt_resolution,25, i_width);
	}
	/**
	 * この関数は、ARToolKitスタイルのマーカーをファイルから読みだして、登録します。
	 * エッジ割合とパターン解像度は、ARToolKitの標準マーカと同じ25%、16x16です。
	 * 重複するidを登録した場合には、最後に登録したidを優先して認識します。
	 * @param i_file_name
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 * @param i_width
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 * @return
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 */
	public int addARMarker(String i_file_name,float i_width)
	{
		return this.addARMarker(i_file_name,16,25, i_width);
	}
	/**
	 * この関数は、NyIdマーカを追加します。
	 * 重複するidを登録した場合には、最後に登録したidを優先して認識します。
	 * @param i_nyid
	 * NyIdを指定します。範囲は、0から33554431です。512以上の数値はmodel3のマーカが必要になるので、特に大量のマーカが必要でなければ512までの値にしてください。
	 * @param i_width
	 * マーカの物理サイズをmm単位で指定します。
	 * @return
	 * 0から始まるマーカーIDを返します。
	 * この数値は、マーカを区別するためのId値です。0から始まり、{@link #addARMarker}と{@link #addNyIdMarker}関数を呼ぶたびにインクリメントされます。
	 */
	public int addNyIdMarker(int i_nyid,int i_width)
	{
		return addNyIdMarker(i_nyid,i_nyid,i_width);
	}
	/**
	 * この関数は、NyIdマーカを範囲指定で追加します。
	 * 範囲指定を行うと、例えば1~10番までのマーカ全てを同じマーカとして扱うようになります。
	 * 範囲中のどのマーカidを認識したかは、{@link #getNyId}で知ることができます。
	 * 範囲が重なるidを登録した場合には、最後に登録したidを優先して認識します。
	 * @param i_nyid_s
	 * NyIdの範囲開始値を指定します。範囲は、{@link #addNyIdMarker(int, int)}を参照してください。
	 * @param i_nyid_e
	 * NyIdの範囲終了値を指定します。
	 * i_nyid_s<=i_nyid_eの関係を満たす値を設定します。
	 * @param i_width
	 * マーカの物理サイズをmm単位で指定します。
	 * @return
	 * 0から始まるマーカーIDを返します。
	 * この数値は、マーカを区別するためのId値です。0から始まり、{@link #addARMarker}と{@link #addNyIdMarker}関数を呼ぶたびにインクリメントされます。
	 */
	public int addNyIdMarker(int i_nyid_range_s,int i_nyid_range_e,int i_width)
	{
		//初期化済みのアイテムを生成
		try{
			TMarkerData item=new TMarkerData(i_nyid_range_s,i_nyid_range_e,i_width);
			this._rel_detector.marker_sl.add(item);
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Exception occurred at MultiARTookitMarker.addNyIdMarker");
		}
		return this._rel_detector.marker_sl.size()-1;		
	}

	
	/**
	 * この関数は、マーカの姿勢行列を返します。
	 * 返却した行列は{@link PApplet#setMatrix}でProcessingにセットできます。
	 * @param i_armk_id
	 * マーカidを指定します。
	 * @return
	 * マーカの姿勢行列を返します。
	 */
	public PMatrix3D getMarkerMatrix(int i_armk_id)
	{
		PMatrix3D p=new PMatrix3D();
		//存在チェック
		if(!this.isExistMarker(i_armk_id)){
			this._ref_papplet.die("Marker id " +i_armk_id + " is not exist on image.", null);
		}
		TMarkerData item=this._rel_detector.marker_sl.get(i_armk_id);
		matResult2PMatrix3D(item.tmat,this._coord_system,p);
		return p;
	}
	/**
	 * この関数は、指定idのARマーカパターンの一致率を返します。
	 * {@list #isExistMarker}がtrueを返すときだけ有効です。
	 * @param i_id
	 * マーカidを指定します。{@link #addARMarker}で登録したidである必要があります。
	 * @return
	 * マーカの一致率を返します。0から1.0までの数値です。
	 * この値は、delayが{@link #getLostCount}の時だけ正しい値を返します。それ以外の時には、0を返します。
	 */
	public double getConfidence(int i_id)
	{
		TMarkerData item=this._rel_detector.marker_sl.get(i_id);
		if(item.mktype!=TMarkerData.MK_AR){
			this._ref_papplet.die("Marker id " +i_id + " is not AR Marker.", null);
		}
		if(!this.isExistMarker(i_id)){
			this._ref_papplet.die("Marker id " +i_id + " is not on image.", null);
		}
		return item.cf;
	}
	/**
	 * この関数は、指定idのNyIdマーカから、現在のマーカIdを取得します。
	 * 値範囲を持つNyIdの場合は、この関数で現在のId値を得ることができます。
	 * @param i_id
	 * マーカidを指定します。{@link #addNyIdMarker}で登録したidである必要があります。
	 * @return
	 * 現在のNyIdマーカを返します。
	 */
	public long getNyId(int i_id)
	{
		TMarkerData item=this._rel_detector.marker_sl.get(i_id);
		if(item.mktype!=TMarkerData.MK_NyId){
			this._ref_papplet.die("Marker id " +i_id + " is not NyId Marker.", null);
		}
		if(!this.isExistMarker(i_id)){
			this._ref_papplet.die("Marker id " +i_id + " is not on image.", null);
		}
		return item.nyid;
	}
	
	/**
	 * この関数は、指定idのマーカが有効かを返します。
	 * {@list #isExistMarker}がtrueを返すときだけ有効です。
	 * @param i_armk_id
	 * ARマーカidを指定します。
	 * @return
	 * マーカが有効ならばtrueです。無効ならfalseです。
	 */
	public boolean isExistMarker(int i_armk_id)
	{
		TMarkerData item=this._rel_detector.marker_sl.get(i_armk_id);
		return (item.lost_count<this._rel_detector._max_lost_delay);
	}
	
	/**
	 * この関数は、指定idのマーカの認識状態を返します。
	 * 数値は、マーカが連続して認識に失敗した回数です。
	 * @param i_armk_id
	 * マーカidを指定します。
	 * @return
	 * 0から{@link #setLostDelay(int)}で設定した範囲の値を返します。
	 */
	public int getLostCount(int i_armk_id)
	{
		if(!this.isExistMarker(i_armk_id)){
			this._ref_papplet.die("Marker id " +i_armk_id + " is not on image.", null);
		}
		TMarkerData item=this._rel_detector.marker_sl.get(i_armk_id);
		return item.lost_count;
	}
//ちょっとわからんからパス
//	/**
//	 * この関数は、指定idのマーカの姿勢変換行列を、ZXY系の角度に変換します。
//	 * 数値は、0から10の範囲です。数値が大きいほど、信頼性が上がります。
//	 * @param i_id
//	 * @return
//	 */
//	public PVector getZXYangle(int i_id)
//	{
//		TItem item=this._rel_detector.marker_sl.get(i_id);
//		NyARDoublePoint3d np=new NyARDoublePoint3d();
//		item.tmat.getZXYAngle(np);
//		return new PVector((float)np.x,(float)np.y,(float)np.z);
//	}
	/**
	 * この関数は、スクリーン座標を、idで指定したマーカ平面座標へ変換して返します。
	 * @param i_id
	 * マーカidを指定します。
	 * @param i_x
	 * スクリーン座標を指定します。
	 * @param i_y
	 * スクリーン座標を指定します。
	 * @return
	 * マーカ平面上の座標点です。
	 */
	public PVector screen2MarkerCoordSystem(int i_id,int i_x,int i_y)
	{
		return this.screen2MarkerCoordSystem(this._rel_detector.marker_sl.get(i_id).tmat, i_x, i_y);
	}
	/**
	 * この関数は、idで指定したマーカの画像のXY平面上の4頂点でかこまれた領域から、画像を取得します。
	 * 取得元画像には、最後に{@link #detect}関数に入力した画像を使います。
	 * 座標点は、[mm]単位です。出力解像度はo_outの解像度に伸縮します。
	 * 座標点の指定順序は、右手系{@link #CS_RIGHT_HAND}なら右上から時計回りです。
	 * 座標点の指定順序は、左手系{@link #CS_LEFT_HAND}なら左上から時計回りです。
	 * @param i_image
	 * 画像を指定します。
	 * @param i_id
	 * マーカIdを指定します。
	 * @param i_x1
	 * 頂点座標1です。
	 * @param i_y1
	 * 頂点座標1です。
	 * @param i_x2
	 * 頂点座標2です。
	 * @param i_y2
	 * 頂点座標2です。
	 * @param i_x3
	 * 頂点座標3です。
	 * @param i_y3
	 * 頂点座標3です。
	 * @param i_x4
	 * 頂点座標4です。
	 * @param i_y4
	 * 頂点座標4です。
	 * @param i_out_w_pix
	 * 出力画像のピクセル幅です。
	 * @param i_out_h_pix
	 * 出力画像のピクセル高さです。
	 * @return
	 * 取得したパターンを返します。
	 */
	public PImage pickupMarkerImage(int i_id,int i_x1,int i_y1,int i_x2,int i_y2,int i_x3,int i_y3,int i_x4,int i_y4,int i_out_w_pix,int i_out_h_pix)
	{
		if(this._ref_last_raster==null){
			this._ref_papplet.die("_rel_detector is null.(Function detect() was never called. )");
		}
		PImage img=new PImage(i_out_w_pix,i_out_h_pix);
		img.parent=this._ref_papplet;
		try{
			NyARDoublePoint3d[] pos=NyARDoublePoint3d.createArray(4);
			TMarkerData item=this._rel_detector.marker_sl.get(i_id);
			item.tmat.transform3d(i_x1, i_y1,0,	pos[1]);
			item.tmat.transform3d(i_x2, i_y2,0,	pos[0]);
			item.tmat.transform3d(i_x3, i_y3,0,	pos[3]);
			item.tmat.transform3d(i_x4, i_y4,0,	pos[2]);
			//4頂点を作る。
			NyARDoublePoint2d[] pos2=NyARDoublePoint2d.createArray(4);
			for(int i=3;i>=0;i--){
				this._frustum.project(pos[i],pos2[i]);
			}
			this._wrap_raster.wrapBuffer(this._ref_last_raster);
			PImageRaster out_raster=new PImageRaster(i_out_w_pix,i_out_h_pix);
			out_raster.wrapBuffer(img);
			if(!this._preader.read4Point(this._wrap_raster,pos2,0,0,1,out_raster))
			{
				throw new Exception("this._preader.read4Point failed.");
			}
			return img;
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Exception occurred at MultiARTookitMarker.pickupImage");
			return null;
		}
	}
	/**
	 * この関数は、idで指定したマーカのXY平面上の矩形領域から、画像を取得します。
	 * 座標点は、[mm]単位です。出力は、o_outの解像度に伸縮します。
	 * @param i_id
	 * 画像を指定します。
	 * @param i_l
	 * 左上の点を指定します。
	 * @param i_t
	 * 左上の点を指定します。
	 * @param i_w
	 * 矩形の幅を指定します。
	 * @param i_h
	 * 矩形の高さを指定します。
	 * @param i_out_w_pix
	 * 出力画像のピクセル幅です。
	 * @param i_out_h_pix
	 * 出力画像のピクセル高さです。
	 * @return
	 * 取得したパターンを返します。
	 */
	public PImage pickupRectMarkerImage(int i_id,int i_l,int i_t,int i_w,int i_h,int i_out_w_pix,int i_out_h_pix)
	{
		return pickupMarkerImage(
			i_id,
			i_l+i_w-1,i_t+i_h-1,
			i_l,i_t+i_h-1,
			i_l,i_t,
			i_l+i_w-1,i_t,
			i_out_w_pix,i_out_h_pix);
	}
}
