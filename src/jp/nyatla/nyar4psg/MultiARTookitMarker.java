package jp.nyatla.nyar4psg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattResult;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective_O2;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.INyARRasterFilter_Rgb2Bin;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilter_ARToolkitThreshold;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.*;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

class TItem
{
	/** パターン情報。ARToolKitコードの比較パターンを格納。 */
	public NyARMatchPatt_Color_WITHOUT_PCA matchpatt;
	/** パターンのサイズ*/
	public double patt_size;
	/**
	 * マーカノステータス値
	 */
	NyARSquare sq=new NyARSquare();
	NyARTransMatResult tmat;
	double cf;
	int dir;
	double matrix;
	int lost_count=0;
	public TItem(InputStream i_patt,int i_patt_resolution,double i_patt_size) throws NyARException
	{
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		c.loadARPatt(i_patt);
		this.matchpatt=new NyARMatchPatt_Color_WITHOUT_PCA(c);
		this.patt_size=i_patt_size;
		this.tmat=new NyARTransMatResult();
		return;
	}
}
/**
 * このクラスは、PImageからARToolKit準拠の変換行列を求めるクラスです。
 * 1映像中に異なる複数のマーカのあるユースケースで動作します。
 */
public class MultiARTookitMarker extends NyARPsgBaseClass
{
	
	/**
	 * この関数は、マーカパターン一致率の閾値を設定します。
	 * デフォルト値は0.4です。
	 * この値よりも一致率が低い場合は、マーカを認識できません。
	 * @param i_val
	 * 設定する値。0.0&lt;n&lt;1.0の値をとります。
	 */
	public void setCfThreshold(double i_val)
	{
		this._rel_detector._cf_threshold=i_val;
	}
	/**
	 * この関数は、マーカ消失時の遅延許容数を指定します。
	 * マーカが消失した後に、何フレーム消失を無視するかを指定します。
	 * デフォルトは10です。
	 * @param i_val
	 * 設定する値。0以上の数値が必要です。
	 */
	public void setLostDelay(int i_val)
	{
		this._rel_detector._lost_delay=i_val;
	}
	/**
	 * この関数は、画像2値化の敷居値を設定します。
	 * @param i_th
	 */
	public void setBinThreshold(int i_th)
	{
		this._threshold=i_th;
	}
	/**
	 * この関数は、マーカのエッジの割合を変更します。
	 * デフォルト値は、25です。
	 * @param i_percentage
	 * エッジの割合は、1以上、50未満です。
	 */
	public void setEdgePercentage(int i_percentage)
	{
		this._rel_detector._pickup.setEdgeSizeByPercent(i_percentage, i_percentage,this._rel_detector._sample_per_pixel);
	}
	private final static int DEFAULT_THRESHOLD=100;
	private final static double DEFAULT_CF_THRESHOLD=0.4;
	private final static int DEFAULT_LOST_DELAY=10;
	private final static int DEFAULT_EDGE=25;
	private int _threshold=DEFAULT_THRESHOLD;
	
	class RleDetector extends NyARSquareContourDetector_Rle
	{
		private NyARIntPoint2d[] _vertexs=new NyARIntPoint2d[4];
		private NyARMatchPattDeviationColorData _patt_d;
		private NyARCoord2Linear _coordline;
		private NyARColorPatt_Perspective_O2 _pickup;
		private NyARMatchPattResult _patt_result;
		private INyARRgbRaster _ref_src_raster;
		public ArrayList<TItem> marker_sl;
		private NyARRectOffset _offset=new NyARRectOffset();
		MultiARTookitMarker _ref_parent;
		public double _cf_threshold=DEFAULT_CF_THRESHOLD;
		public int _lost_delay=DEFAULT_LOST_DELAY;
		private int _sample_per_pixel;
		public RleDetector(MultiARTookitMarker i_parent,NyARParam i_param,int i_patt_resolution) throws NyARException
		{
			super(i_param.getScreenSize());
			this._ref_parent=i_parent;
			//サンプリングピッチの計算
			int r=1;
			while(i_patt_resolution*r<64){
				r*=2;
			}
			this._sample_per_pixel=r;
			//インスタンスの生成
			this._patt_d=new NyARMatchPattDeviationColorData(i_patt_resolution, i_patt_resolution);
			this._pickup=new NyARColorPatt_Perspective_O2(i_patt_resolution,i_patt_resolution,this._sample_per_pixel,DEFAULT_EDGE,PImageRaster.BUFFER_TYPE);
			this._coordline=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
			this._patt_result=new NyARMatchPattResult();
			//リストのリセット
			this.marker_sl=new ArrayList<TItem>();
		}
		protected void onSquareDetect(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
		{
			//画像取得配列の生成
			for(int i2=0;i2<4;i2++){
				this._vertexs[i2]=i_coord.items[i_vertex_index[i2]];
			}
			//パターンの取得
			if(!this._pickup.pickFromRaster(this._ref_src_raster,this._vertexs)){
				return;
			}
			//比較パターンの生成
			this._patt_d.setRaster(this._pickup);
			//検出状態をリセットするよ。
			//全てのマーカについて、一番一致したパターンIDを求める。
			double best_cf=0;
			int best_id=-1;
			int best_dir=NyARMatchPattResult.DIRECTION_UNKNOWN;
			for(int i=marker_sl.size()-1;i>=0;i--)
			{
				TItem item=marker_sl.get(i);
				//評価
				if(!item.matchpatt.evaluate(this._patt_d,this._patt_result)){
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
			TItem item=this.marker_sl.get(best_id);
			if(item.cf>best_cf){
				return;
			}
			//結果をコピーする
			item.lost_count=0;
			item.cf=best_cf;
			for(int i=0;i<4;i++){
				int idx=(i+4 - best_dir) % 4;
				this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coord,item.sq.line[i]);
			}
			for (int i = 0; i < 4; i++) {
				//直線同士の交点計算
				if(!item.sq.line[i].crossPos(item.sq.line[(i + 3) % 4],item.sq.sqvertex[i])){
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
			//状態のリセット
			for(int i=this.marker_sl.size()-1;i>=0;i--){
				TItem item=this.marker_sl.get(i);
				item.cf=0;
				if(item.lost_count<this._lost_delay){
					item.lost_count++;
				}
			}
		}
		/**
		 * 検出の後処理
		 * @throws NyARException 
		 */
		public void finishDetection(NyARTransMat i_transmat) throws NyARException
		{
			//全部のマーカについて、変換行列を計算しておきましょうか。
			for(int i=this.marker_sl.size()-1;i>=0;i--){
				TItem item=this.marker_sl.get(i);
				this._offset.setSquare(item.patt_size);
				i_transmat.transMat(item.sq,this._offset, item.tmat);
			}	
		}
	}
	private RleDetector _rel_detector;
	private PGraphicsOpenGL _ref_pgl;
	private PMatrix3D _old_pp_matrix;
	int _patt_resolution;
	PImageRaster _raster;
	
	private NyARBinRaster _bin_raster;
	private INyARRasterFilter_Rgb2Bin _tobin_filter;
	private NyARTransMat _transmat_inst;
	/**
	 * コンストラクタです。
	 * @param parent
	 * 親となるAppletオブジェクトを指定します。
	 * @param i_cparam_file
	 * カメラパラメータファイルの名前を指定します。
	 * @param i_width
	 * 入力画像の横解像度を指定します。
	 * @param i_height
	 * 入力画像の縦解像度を指定します。
	 * @param i_patt_resolution
	 * 使用するマーカパターンの解像度を指定します。{@link #addMarker(String, double)}でセットするマーカパターンは、ここで設定したサイズであるとみなされます。
	 * @param i_projection_coord_system
	 * ARToolKit座標系のタイプを指定します。
	 * @throws NyARException
	 */
	public MultiARTookitMarker(PApplet parent, int i_width,int i_height,String i_cparam_file,int i_patt_resolution,int i_projection_coord_system)
	{
		super(parent,i_cparam_file, i_width,i_height,i_projection_coord_system);
		try{
			this._rel_detector=new RleDetector(this,this._ar_param,i_patt_resolution);
			this._patt_resolution=i_patt_resolution;
			this._raster=new PImageRaster(i_width,i_height);
			this._bin_raster=new NyARBinRaster(i_width,i_height,true);
			this._tobin_filter=new NyARRasterFilter_ARToolkitThreshold(this._threshold,this._raster.getBufferType());
			this._transmat_inst=new NyARTransMat(this._ar_param);
		}catch(Exception e){
			e.printStackTrace();
			parent.die("Exception occurred at MultiARTookitMarker.MultiARTookitMarker");
		}
	}
	/**
	 * マーカーを登録する。
	 * 登録するマーカの解像度は、コンストラクタで設定したパターン解像度と同じである必要があります。
	 * @param i_file
	 * マーカパターンファイル名を指定します。
	 * @param i_width
	 * マーカの物理サイズをmm単位で指定します。
	 * @return
	 * 0から始まるマーカーIDを返します。
	 */
	public int addMarker(String i_file,double i_width)
	{
		//初期化済みのアイテムを生成
		try{
			TItem item=new TItem(this._ref_papplet.createInput(i_file),this._patt_resolution,i_width);
			this._rel_detector.marker_sl.add(item);
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Exception occurred at MultiARTookitMarker.addMarker");
		}
		return this._rel_detector.marker_sl.size()-1;
	}
	/**
	 * この関数は、画像からマーカーの検出処理を実行します。
	 * @param i_image
	 * 検出処理を行う画像
	 */
	public void detect(PImage i_image)
	{
		try{

			//RGBラスタをラップ
			this._raster.wrapBuffer(i_image);
			//BIN変換
			this._tobin_filter.doFilter(this._raster,this._bin_raster);
			//検出準備
			this._rel_detector.prevDetection(this._raster);
			//検出
			this._rel_detector.detectMarker(this._bin_raster);
			this._rel_detector.finishDetection(this._transmat_inst);
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Exception occurred at MultiARTookitMarker.detect");
		}
		
	}
	/**
	 * この関数は、ARToolKitの座標系を開始します。
	 * 関数はProcessingの描画システムのProjectionMatrixにカメラパラメータをセットして、カメラ画像にマッチした
	 * 3Dモデル描画を出来るようにします。
	 */
	public void beginARTKProjection(PGraphicsOpenGL i_pgl)
	{
		if(this._ref_pgl!=null)
		{
			this._ref_papplet.die("The function beginARTKProjection is already called.", null);			
		}
		
		if(this._ref_pgl!=null){
			this._ref_papplet.die("The function beginTransform is already called.", null);			
		}
		this._ref_pgl=i_pgl;
		GL gl=i_pgl.gl;
		{	//projectionの切り替え
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushMatrix();
			gl.glLoadMatrixd(this.projection,0);
			this._old_pp_matrix=i_pgl.projection;
			i_pgl.projection=this._ps_projection;
		}
		//行列モードを戻す。
		gl.glMatrixMode(GL.GL_MODELVIEW);
		return;	
	}
	/**
	 * この関数は、ARToolKit座標系を終了します。
	 * @return
	 */
	public void endARTKProjection()
	{
		if(this._ref_pgl==null){
			this._ref_papplet.die("The function beginARTKProjection is never called.", null);			
		}
		GL gl=this._ref_pgl.gl;

		{	//projectionの復帰
			this._ref_pgl.projection=this._old_pp_matrix;
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPopMatrix();
			gl.glMatrixMode(GL.GL_MODELVIEW);
		}
		this._ref_pgl=null;
		return;
	}
	/**
	 * この関数は、マーカの座標変換行列を返します。
	 * 返却した行列は{@link PApplet#setMatrix}でProcessingにセットできます。
	 * @param i_id
	 * マーカidを指定します。
	 * @param o_matrix
	 * 座標変換行列を受け取る
	 */
	public PMatrix3D allocMarkerMatrix(int i_id)
	{
		if(this._ref_pgl==null){
			this._ref_papplet.die("The function beginARTKProjection is never called.", null);			
		}
		PMatrix3D p=new PMatrix3D();
		//存在チェック
		if(!this.isExistMarker(i_id)){
			this._ref_papplet.die("Marker id " +i_id + " is not on image.", null);
		}
		TItem item=this._rel_detector.marker_sl.get(i_id);
		matResult2PMatrix3D(item.tmat,this._coord_system,p);
		return p;
	}

	/**
	 * 指定idのマーカパターンの一致率を返します。
	 * @param i_id
	 * マーカidを指定します。
	 * @return
	 */
	public double getConfidence(int i_id)
	{
		TItem item=this._rel_detector.marker_sl.get(i_id);
		if(!this.isExistMarker(i_id)){
			this._ref_papplet.die("Marker id " +i_id + " is not on image.", null);
		}
		return item.cf;
	}
	/**
	 * 指定idのマーカがあるか調べる。
	 * @param i_id
	 * マーカidを指定します。
	 * @return
	 */
	public boolean isExistMarker(int i_id)
	{
		TItem item=this._rel_detector.marker_sl.get(i_id);
		return !(item.lost_count>=this._rel_detector._lost_delay);
	}
	
	/**
	 * この関数は、指定idのマーカの認識状態を返します。
	 * 数値は、マーカが連続して認識に失敗した回数です。
	 * @param i_id
	 * マーカidを指定します。
	 * @return
	 * 0から{@link #setLostDelay(int)}で設定した範囲の値を返します。
	 */
	public double getLostCount(int i_id)
	{
		if(!this.isExistMarker(i_id)){
			this._ref_papplet.die("Marker id " +i_id + " is not on image.", null);
		}
		TItem item=this._rel_detector.marker_sl.get(i_id);
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
	 * スクリーン座標を、idで指定したマーカ平面座標へ変換して返します。
	 * @param i_id
	 * @param i_screen_pos
	 */
	public PVector screen2MarkerCoordSystem(int i_id,int i_x,int i_y)
	{
		TItem item=this._rel_detector.marker_sl.get(i_id);
		PVector ret=new PVector();
		NyARDoublePoint3d tmp=new NyARDoublePoint3d();
		this._frustum.unProjectOnMatrix(i_x, i_y, item.tmat,tmp);
		ret.x=(float)tmp.x;
		ret.y=(float)tmp.y;
		ret.z=(float)tmp.z;
		if(this._coord_system==MultiARTookitMarker.CS_LEFT_HAND){
			ret.x*=-1;
		}
		return ret;
	}
}
