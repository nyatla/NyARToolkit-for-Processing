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

import java.util.ArrayList;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import processing.core.*;
import processing.opengl.*;
import jp.nyatla.nyartoolkit.markersystem.*;


/**
 * このクラスは、複数のマーカに対応したARToolKit管理クラスです。
 * 1映像中に異なる複数のマーカのあるユースケースで動作します。
 * 
 * 入力画像はPImage形式です。
 */
public class MultiMarker extends NyARPsgBaseClass
{
	class PsgMsCfg extends NyARMarkerSystemConfig
	{
		private int _tmat_type;
		public PsgMsCfg(NyARParam i_param,int i_tmat_type)throws NyARException
		{
			super(i_param);
			this._tmat_type=i_tmat_type;
		}
		public INyARTransMat createTransmatAlgorism() throws NyARException
		{
			switch(this._tmat_type)
			{
			case NyAR4PsgConfig.TM_ARTK:
				return new NyARTransMat_ARToolKit(this._param);
			case NyAR4PsgConfig.TM_NYARTK:
				return new NyARTransMat(this._param);
			default:
				throw new NyARException("Invalid Transmat algolism type.");
			}
		}		
	}
	class PImageSensor extends NyARSensor
	{
		private PImageRaster _src;
		public PImageSensor(NyARIntSize i_size) throws NyARException
		{
			super(i_size);
			this._src=new PImageRaster(i_size.w,i_size.h);
		}
		public void update(PImage i_img) throws NyARException
		{
			this._src.wrapBuffer(i_img);
			super.update(this._src);
		}
		
	}
	
	protected PImageSensor _ss;
	protected NyARMarkerSystem _ms;


	
	/**
	 * この関数は、ARマーカパターン一致率の閾値を設定します。
	 * この値よりも一致率が低いマーカを認識しなくなります。
	 * デフォルト値は{@link #DEFAULT_CF_THRESHOLD}です。
	 * @param i_val
	 * 設定する値。0.0&lt;n&lt;1.0の値を設定します。
	 */
	public void setConfidenceThreshold(double i_val)
	{
		this._ms.setConfidenceThreshold(i_val);
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
		this._ms.setLostDelay(i_val);
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
		int th=(i_th==THLESHOLD_AUTO)?NyARMarkerSystem.THLESHOLD_AUTO:i_th;
		this._ms.setBinThreshold(th);
	}
	/**
	 * この関数は、現在の二値化敷居値を返します。
	 * 自動敷居値を選択している場合は、直近の敷居値を返します。
	 * @return
	 */
	public int getCurrentThreshold()
	{
		//256スケールに直す。
		return this.getCurrentThreshold();
	}
	
	/** 初期値定数。マーカ一致度の最小敷居値を示します。*/
	public final static double DEFAULT_CF_THRESHOLD=0.51;
	/** 初期値定数。マーカ消失時の許容*/
	public final static int DEFAULT_LOST_DELAY=10;
	/** 敷居値の定数です。敷居値を自動決定します。*/
	public final static int THLESHOLD_AUTO=-1;
	/** PSGのIDとMarkerSystemのマッピング*/
	private ArrayList<Integer> _id_map=new ArrayList<Integer>();

	private final PMatrix3D _ps_projection=new PMatrix3D();
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
	 * @param i_config
	 * コンフィギュレーションオブジェクトを指定します。
	 * @throws NyARException
	 */
	public MultiMarker(PApplet parent, int i_width,int i_height,String i_cparam_file,NyAR4PsgConfig i_config)
	{
		try{
			NyARParam cp=NyARParam.createFromARParamFile(parent.createInput(i_cparam_file));
			cp.changeScreenSize(i_width,i_height);
			this.initInstance(parent,cp,i_config);
		}catch(Exception e){
			e.printStackTrace();
			parent.die("Catch an exception!");
		}			
		return;
	}
	/**
	 * コンストラクタです。
	 * {@link MultiMarker#MultiMarker(PApplet, int, int, String, NyAR4PsgConfig)}のコンフィギュレーションに、{@link NyAR4PsgConfig#CONFIG_DEFAULT}を指定した物と同じです。
	 * @param parent
	 * {@link MultiMarker#MultiMarker(PApplet, int, int, String, NyAR4PsgConfig)}を参照。
	 * @param i_width
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_height
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_cparam_file
	 * {@link MultiMarker#MultiMarker(PApplet, int, int, String, NyAR4PsgConfig)}を参照。
	 * @throws NyARException
	 */
	public MultiMarker(PApplet parent,int i_width,int i_height,String i_cparam_file)
	{
		try{
			NyARParam cp=NyARParam.createFromARParamFile(parent.createInput(i_cparam_file));
			cp.changeScreenSize(i_width,i_height);
			this.initInstance(parent,cp, NyAR4PsgConfig.CONFIG_DEFAULT);
		}catch(Exception e){
			e.printStackTrace();
			parent.die("Catch an exception!");
		}
		return;
	}
	/**
	 * コンストラクタです。
	 * {@link MultiMarker#MultiMarker(PApplet, int, int, String, NyAR4PsgConfig)}のコンフィギュレーションに、{@link NyAR4PsgConfig#CONFIG_DEFAULT}を指定した物と同じです。
	 * @param parent
	 * {@link MultiMarker#MultiMarker(PApplet, int, int, String, NyAR4PsgConfig)}を参照。
	 * @param i_width
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_height
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_size
	 * カメラパラメータのサイズ値
	 * @param i_intrinsic_matrix
	 * 3x3 matrix
	 * このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するintrinsic_matrixの値と合致します。
	 * @param i_distortion_coeffs
	 * 4x1 matrix
	 * このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するdistortion_coeffsの値と合致します。
	 */	
	public MultiMarker(PApplet parent,int i_width,int i_height,double[] i_intrinsic_matrix,double[] i_distortion_coeffs)
	{
		try{
			NyARParam cp=NyARParam.createFromCvCalibrateCamera2Result(i_width, i_height, i_intrinsic_matrix, i_distortion_coeffs);
			cp.changeScreenSize(i_width,i_height);
			this.initInstance(parent,cp, NyAR4PsgConfig.CONFIG_DEFAULT);
		}catch(Exception e){
			e.printStackTrace();
			parent.die("Catch an exception!");
		}
		return;
	}
	/**
	 * インスタンスを初期化します。
	 * @param parent
	 * @param i_width
	 * @param i_height
	 * @param i_cparam_file
	 * @param i_patt_resolution
	 * @param i_projection_coord_system
	 * @throws NyARException 
	 */
	protected void initInstance(PApplet parent,NyARParam i_param,NyAR4PsgConfig i_config) throws NyARException
	{
		NyARIntSize s=i_param.getScreenSize();
		this._ss=new PImageSensor(new NyARIntSize(s.w,s.h));
		this._ms=new NyARMarkerSystem(
				new PsgMsCfg(i_param,i_config.env_transmat_mode));
		super.initInstance(parent,i_config);
	}
	@Override
	public PMatrix3D getProjectionMatrix()
	{
		return this._ps_projection;
	}
	@Override
	public PMatrix3D getProjectionMatrix(PMatrix3D i_buf)
	{
		return new PMatrix3D(this._ps_projection);
	}	
	@Override
	public void setARClipping(float i_near,float i_far)
	{
		super.setARClipping(i_near, i_far);
		this._ms.setProjectionMatrixClipping(i_near,i_far);
		NyARIntSize s=this._ms.getARParam().getScreenSize();
		NyARDoubleMatrix44 tmp=new NyARDoubleMatrix44();
		this._ms.getARParam().getPerspectiveProjectionMatrix().makeCameraFrustumRH(s.w,s.h,i_near,i_far,tmp);
		nyarMat2PsMat(tmp,this._ps_projection);
	}	
	
	/** begin-endシーケンスの判定用*/
	private boolean _is_in_begin_end_session=false;
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
	 * //prev_matは現在の行列退避用。endTransformで使用する。<br/>
	 * PMatrix3D prev_mat=new PMatrix3D(((PGraphicsOpenGL)g).projection);<br/>
	 * setARPerspective();<br/>
	 * pushMatrix();<br/>
	 * setMatrix(ar.getMarkerMatrix(i_id));<br/>
	 * :<br/>
	 * <hr/>
	 * </div>
	 * @param i_id
	 * マーカidを指定します。
	 */
	public void beginTransform(int i_id)
	{
		if(this._is_in_begin_end_session){
			this._ref_papplet.die("The function beginTransform is already called.", null);			
		}
		this._is_in_begin_end_session=true;
		
		if(!(this._ref_papplet.g instanceof PGraphicsOpenGL)){
			this._ref_papplet.die("NyAR4Psg require PGraphicsOpenGL instance.");
		}		
		PGraphicsOpenGL pgl=((PGraphicsOpenGL)this._ref_papplet.g);
		//行列の待避
		pgl.pushProjection();
		this.setARPerspective();
		
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
		if(!this._is_in_begin_end_session){
			this._ref_papplet.die("The function beginTransform is never called.", null);			
		}
		this._is_in_begin_end_session=false;	
		
		//ModelViewの復帰
		this._ref_papplet.popMatrix();
		//Projectionの復帰
		PGraphicsOpenGL pgl=((PGraphicsOpenGL)this._ref_papplet.g);
		pgl.popProjection();
		return;
	}
	/**
	 * この関数は、画像からマーカーの検出処理を実行します。
	 * 関数は、i_imageに対して1度だけ{@link PImage#loadPixels()}を実行します。
	 * {@link PImage#loadPixels()}のタイミングをコントロールしたい場合は、{@link #detectWithoutLoadPixels}を使用してください。
	 * @param i_image
	 * 検出処理を行う画像を指定します。
	 */	
	public void detect(PImage i_image)
	{
		i_image.loadPixels();
		this.detectWithoutLoadPixels(i_image);
	}
	/**
	 * {@link PImage#loadPixels()}を伴わない{@link detect()}です。
	 * 引数と戻り値の詳細は、{@link #detect(PImage)}を参照してください。
	 * @param i_image
	 * @see #detect(PImage)
	 */
	public void detectWithoutLoadPixels(PImage i_image)
	{
		try{
			this._ss.update(i_image);
			this._ms.update(this._ss);
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Catch an exception!");
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
		int psid=-1;
		try{
			this._id_map.add(this._ms.addARMarker(this._ref_papplet.createInput(i_file_name), i_patt_resolution, i_edge_percentage, i_width));
			psid=this._id_map.size()-1;
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Catch an exception!");
		}
		return psid;
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
	 * この関数は、i_imgの画像をARマーカパターンとして登録します。
	 * @param i_img
	 * マーカパターンのカラー画像を指定します。
	 * 関数はi_imgの{@link PImage#loadPixels()}を1度だけ呼び出します。
	 * @param i_patt_resolution
	 * 作成するマーカパターンの解像度を指定します。ARToolkitと同一であれば16です。
	 * 数値が高いほどシビアな判定が出来ますが、速度は低下します。
	 * @param i_edge_percentage
	 * エッジ割合を指定します。この数値は、元画像と、抽出するマーカパターンの両方に影響を及ぼします。
	 * ARToolKit互換のパターンの場合、25を指定します。
	 * @param i_width
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 * @return
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 */
	public int addARMarker(PImage i_img,int i_patt_resolution,int i_edge_percentage,float i_width)
	{
		int psid=-1;
		try{
			i_img.loadPixels();
			PImageRaster pr=new PImageRaster(i_img);
			//初期化済みのアイテムを生成
			this._id_map.add(this._ms.addARMarker(pr, i_patt_resolution, i_edge_percentage, i_width));
			psid=this._id_map.size()-1;
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Catch an exception!");
			
		}
		return psid;
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
		int psid=-1;
		//初期化済みのアイテムを生成
		try{
			this._id_map.add(this._ms.addNyIdMarker(i_nyid_range_s,i_nyid_range_e,i_width));
			psid=this._id_map.size()-1;
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Catch an exception!");
		}
		return psid;
	}
	/**
	 * この関数は、PSVitaのARプレイカードを検出対象に追加します。
	 * @param i_psarid
	 * PSVitaのARプレイカードのIDを指定します。1から6間での数値です。
	 * @param i_width
	 * マーカの物理サイズをmm単位で指定します。
	 * @return
	 * 0から始まるマーカーIDを返します。
	 * この数値は、マーカを区別するためのId値です。
	 */
	public int addPsARPlayCard(int i_psarid,int i_width)
	{
		int psid=-1;
		//初期化済みのアイテムを生成
		try{
			this._id_map.add(this._ms.addPsARPlayCard(i_psarid, i_width));
			psid=this._id_map.size()-1;
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Catch an exception!");
		}
		return psid;
	}	
	/**
	 * この関数は、マーカのスクリーン上の4頂点を返します。
	 * @return
	 */
	public PVector[] getMarkerVertex2D(int i_id)
	{
		int msid=this._id_map.get(i_id);
		PVector[] r=new PVector[4];
		try{
			NyARIntPoint2d[] pos=this._ms.getMarkerVertex2D(msid);
			for(int i=0;i<4;i++){
				r[i]=new PVector((float)(pos[i].x),(float)(pos[i].y));
			}
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Catch an exception!");
		}
		return r;
	}
	
	/**
	 * この関数は、マーカの姿勢行列を返します。
	 * 返却した行列は{@link PApplet#setMatrix}でProcessingにセットできます。
	 * @param i_armk_id
	 * マーカidを指定します。
	 * @return
	 * マーカの姿勢行列を返します。
	 */
	public PMatrix3D getMarkerMatrix(int i_id)
	{
		PMatrix3D p=new PMatrix3D();
		//存在チェック
		if(!this.isExistMarker(i_id)){
			this._ref_papplet.die("Marker id " +i_id + " is not exist on image.", null);
		}
		int msid=this._id_map.get(i_id);
		try{
			matResult2PMatrix3D(this._ms.getMarkerMatrix(msid),this._config._coordinate_system,p);
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Catch an exception!");
		}			
		return p;
	}
	/**
	 * この関数は、指定idのARマーカパターンの一致率を返します。
	 * {@list #isExistMarker}がtrueを返すときだけ有効です。
	 * @param i_id
	 * マーカidを指定します。{@link #addARMarker}で登録したidである必要があります。
	 * @return
	 * マーカの一致率を返します。0から1.0までの数値です。
	 * この値は、{@link #isExistMarker}がtrueの時だけ正しい数を返します。
	 */
	public double getConfidence(int i_id)
	{
		int msid=this._id_map.get(i_id);
		try{
			return this._ms.getConfidence(msid);
		}catch(Exception e){
			this._ref_papplet.die("Marker id " +i_id + " is not AR Marker or not exist.", null);
		}
		return Double.NaN;
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
		int msid=this._id_map.get(i_id);
		try{
			return this._ms.getNyId(msid);
		}catch(Exception e){
			this._ref_papplet.die("Marker id " +i_id + " is not NyId Marker or not exist.", null);
		}
		return -1;
	}
	
	/**
	 * この関数は、指定idのマーカが有効かを返します。
	 * {@list #isExistMarker}がtrueを返すときだけ有効です。
	 * @param i_id
	 * マーカidを指定します。
	 * @return
	 * マーカが有効ならばtrueです。無効ならfalseです。
	 */
	public boolean isExistMarker(int i_id)
	{
		int msid=this._id_map.get(i_id);
		try{
			return this._ms.isExistMarker(msid);
		}catch(Exception e){
			this._ref_papplet.die("Catch an exception!", null);
			return false;
		}
	}
	
	/**
	 * この関数は、指定idのマーカの認識状態を返します。
	 * 数値は、マーカが連続して認識に失敗した回数です。
	 * @param i_id
	 * マーカidを指定します。
	 * @return
	 * 0から{@link #setLostDelay(int)}で設定した範囲の値を返します。
	 */
	public int getLostCount(int i_id)
	{
		if(!this.isExistMarker(i_id)){
			this._ref_papplet.die("Marker id " +i_id + " is not on image.", null);
		}
		int msid=this._id_map.get(i_id);
		try{
			return (int)this._ms.getLostCount(msid);
		}catch(Exception e){
			this._ref_papplet.die("Catch an exception!", null);
			return -1;
		}
	}
	/**
	 * この関数は、指定idのマーカのライフ値を返します。
	 * ライフ値は、マーカが認識されるたびにインクリメントされる値です。
	 * 例えば、idマーカとARマーカの分離が難しい時に、十分な値のあるマーカだけを認識することにより、問題を解決できます。
	 * @param i_id
	 * マーカidを指定します。
	 * @return
	 * 0以上のライフ値です。
	 */
	public long getLife(int i_id)
	{
		try{
			if(!this.isExistMarker(i_id)){
				this._ref_papplet.die("Marker id " +i_id + " is not on image.", null);
			}
			int msid=this._id_map.get(i_id);
			return this._ms.getLife(msid);
		}catch(Exception e){
			this._ref_papplet.die("Catch an exception!", null);
			return -1;
		}
	}
	/**
	 * この関数は、idで示されるマーカ座標系の点をスクリーン座標へ変換します。
	 * @param i_id
	 * マーカidを指定します。
	 * @param i_x
	 * マーカ座標系のX座標
	 * @param i_y
	 * マーカ座標系のY座標
	 * @param i_z
	 * マーカ座標系のZ座標
	 * @return
	 * スクリーン座標
	 */
	public PVector marker2ScreenCoordSystem(int i_id,double i_x,double i_y,double i_z)
	{
		try{
			int msid=this._id_map.get(i_id);
			NyARDoublePoint2d pos=new NyARDoublePoint2d();
			this._ms.getScreenPos(msid, i_x, i_y, i_z,pos);
			PVector ret=new PVector();
			ret.x=(float)pos.x;
			ret.y=(float)pos.y;
			ret.z=0;
			return ret;
		}catch(Exception e){
			this._ref_papplet.die("Catch an exception!", null);
			return null;
		}
	}

	/**
	 * この関数は、スクリーン座標をidで指定したマーカ平面座標へ変換して返します。
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
		try{
			int msid=this._id_map.get(i_id);
			PVector ret=new PVector();
			NyARDoublePoint3d tmp=new NyARDoublePoint3d();
			this._ms.getMarkerPlanePos(msid,i_x, i_y,tmp);
			ret.x=(float)tmp.x;
			ret.y=(float)tmp.y;
			ret.z=(float)tmp.z;
			if(this._config._coordinate_system==NyAR4PsgConfig.CS_LEFT_HAND){
				ret.x*=-1;
			}		
			return ret;
		}catch(Exception e){
			this._ref_papplet.die("Catch an exception!", null);
			return null;
		}			
	}
	/**
	 * この関数は、idで指定したマーカの画像のXY平面上の4頂点でかこまれた領域から、画像を取得します。
	 * 取得元画像には、最後に{@link #detect}関数に入力した画像を使います。
	 * 座標点は、[mm]単位です。出力解像度はo_outの解像度に伸縮します。
	 * 座標点の指定順序は、右手系{@link #CS_RIGHT_HAND}なら右上から反時計回りです。
	 * 座標点の指定順序は、左手系{@link #CS_LEFT_HAND}なら左上から時計回りです。
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
		PImage p=new PImage(i_out_w_pix,i_out_h_pix);
		try{
			PImageRaster pr=new PImageRaster(p);
			int msid=this._id_map.get(i_id);
			this._ms.getMarkerPlaneImage(msid, this._ss, i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4,pr);
			p.updatePixels();
		}catch(Exception e){
			this._ref_papplet.die("pickupMarkerImage failed.", null);
		}
		return p;
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
