package jp.nyatla.nyar4psg;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import processing.core.*;

/**
 * 単一マーカ認識ユースケースのベースクラス。同時に一つの座標変換行列を提供します。
 * 座標変換行列の管理関数とプロパティ機能を提供します。
 * @author nyatla
 *
 */
class SingleMarkerBaseClass extends NyARPsgBaseClass
{
	
	/**
	 * [read only]マーカのx,y,zの傾き角度です。
	 * この角度は、ARToolKit座標系での値です。
	 * @deprecated
	 * この変数は互換性の為に残されています。{@link #getMarkerMatrix()}で得られる値から計算してください。
	 */
	public final PVector angle=new PVector();
	/**
	 * [read only]マーカのx,y,zの平行移動量です。
	 * この角度は、ARToolKit座標系での値になります。
	 * @deprecated
	 * この変数は互換性の為に残されています。{@link #getMarkerMatrix()}で得られる値から計算してください。
	 */
	public final PVector trans=new PVector();
	/**
	 * [read only]検出したマーカの4隅の２次元画像上の位置です。
	 * @deprecated
	 * この変数は互換性の為に残されています。{@link #getMarkerVertex2D()}の値を使用してください。
	 */
	public final int[][] pos2d=new int[4][2];
	/**
	 * [read only]検出したマーカの変換行列です。
	 * この角度は、ARToolKit座標系での値になります。
	 * @deprecated
	 * この変数は互換性の為に残されています。{@link #getMarkerMatrix()}で得られる値を使用してください。
	 */
	public final double[] transmat=new double[16];
	/**
	 * [read only] OpenGLスタイルのProjectionMatrixです。
	 * @deprecated
	 * この変数は互換性の為に残されています。{@link #getProjectionMatrix()}で得られる値を使用してください。
	 */
	public final double[] projection=new double[16];
	/** NyARToolkit形式のModelview行列*/
	private final NyARDoubleMatrix44 _result=new NyARDoubleMatrix44();
	
	/** begin-endシーケンスで使う。*/
	private PMatrix3D _old_matrix;
	
	/**
	 * この関数は、マーカの座標変換行列を返します。
	 * 返却する行列はProcessing座標系です。
	 * @return
	 */
	public PMatrix3D getMarkerMatrix()
	{
		PMatrix3D ret=new PMatrix3D();
		matResult2PMatrix3D(this._result,this._config._coordinate_system,ret);
		return new PMatrix3D(ret);
	}
	/**
	 * この関数は、マーカのスクリーン上の4頂点を返します。
	 * @return
	 */
	public PVector[] getMarkerVertex2D()
	{
		PVector[] r=new PVector[4];
		for(int i=0;i<4;i++){
			r[i]=new PVector(pos2d[i][0],pos2d[i][1]);
		}
		return r;
	}
	/**
	 * この関数は互換性の為に残されています。
	 * {@link #beginTransform()}を使ってください。
	 * @param i_pgl
	 * 通常は、{@link PApplet#g}をキャストして指定します。
	 * @deprecated
	 */
	public void beginTransform(PGraphics3D i_pgl)
	{
		assert(i_pgl.equals(this._ref_papplet.g));
		this.beginTransform();
	}
	
	/**
	 * この関数は、ProcessingのModelviewとProjectionをマーカ平面にセットします。
	 * この関数は、必ず{@link #endTransform}とペアで使います。
	 * 関数を実行すると、現在のModelView行列とProjection行列がインスタンスに保存され、新しい行列がセットされます。
	 * これらを復帰するには、{@link #endTransform}を使います。
	 * <div>この関数は、次のコードと等価です。</div>
	 * <hr/>
	 * :<br/>
	 * PMatrix3D prev_mat=setARPerspective();//prev_matは現在の行列退避用。<br/>
	 * pushMatrix();<br/>
	 * setMatrix(ar.getMarkerMatrix());<br/>
	 * :<br/>
	 * <hr/>
	 * </div>
	 */
	public void beginTransform()
	{
		if(this._old_matrix!=null){
			this._ref_papplet.die("The function beginTransform is already called.", null);			
		}
		//projectionの切り替え
		this._old_matrix=this.setARPerspective();
		//ModelViewの設定
		this._ref_papplet.pushMatrix();
		this._ref_papplet.setMatrix(this.getMarkerMatrix());
		return;	
	}
	/**
	 * {@link #beginTransform}でセットしたProjectionとModelViewを元に戻します。
	 * この関数は、必ず{@link #beginTransform}とペアで使います。
	 * <div>この関数は、次のコードと等価です。</div>
	 * <hr/>
	 * :<br/>
	 * setPerspective(prev_mat);//prev_matはsetARPerspectiveで退避した行列。<br/>
	 * popMatrix();<br/>
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
	 * この関数は、スクリーン座標を、idで指定したマーカ平面座標へ変換して返します。
	 * @param i_x
	 * スクリーン座標を指定します。
	 * @param i_y
	 * スクリーン座標を指定します。
	 * @return
	 * マーカ平面上の座標点です。
	 */
	public PVector screen2MarkerCoordSystem(int i_x,int i_y)
	{
		return super.screen2MarkerCoordSystem(this._result, i_x, i_y);
	}
	/**
	 * この関数は、マーカの画像のXY平面上の4頂点でかこまれた領域から、画像を取得します。
	 * 取得元画像には、最後に{@link #detect}関数に入力した画像を使います。
	 * 座標点は、[mm]単位です。出力解像度はo_outの解像度に伸縮します。
	 * 座標点の指定順序は、右手系{@link #CS_RIGHT_HAND}なら右上から時計回りです。
	 * 座標点の指定順序は、左手系{@link #CS_LEFT_HAND}なら左上から時計回りです。
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
	public PImage pickupMarkerImage(int i_x1,int i_y1,int i_x2,int i_y2,int i_x3,int i_y3,int i_x4,int i_y4,int i_out_w_pix,int i_out_h_pix)
	{
		return super.pickupMarkerImage(
			this._result,
			i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4, i_out_w_pix, i_out_h_pix);
	}
	/**
	 * この関数は、マーカのXY平面上の矩形領域から、画像を取得します。
	 * 座標点は、[mm]単位です。出力は、o_outの解像度に伸縮します。
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
	public PImage pickupRectMarkerImage(int i_l,int i_t,int i_w,int i_h,int i_out_w_pix,int i_out_h_pix)
	{
		return pickupMarkerImage(
			i_l+i_w-1,i_t+i_h-1,
			i_l,i_t+i_h-1,
			i_l,i_t,
			i_l+i_w-1,i_t,
			i_out_w_pix,i_out_h_pix);
	}	
	protected void updateTransmat(NyARSquare i_square,NyARTransMatResult i_src)
	{
		this._result.setValue(i_src);
		matResult2GLArray(i_src,this.transmat);
		//angle
		i_src.getZXYAngle(this._tmp_d3p);
		
		this.angle.x=(float)this._tmp_d3p.x;
		this.angle.y=(float)this._tmp_d3p.y;
		this.angle.z=(float)this._tmp_d3p.z;
		//trans
		this.trans.x=(float)i_src.m03;
		this.trans.y=(float)i_src.m13;
		this.trans.z=(float)i_src.m23;

		//pos反映
		final NyARDoublePoint2d[] pts=i_square.sqvertex;
		for(int i=0;i<4;i++){
			this.pos2d[i][0]=(int)pts[i].x;
			this.pos2d[i][1]=(int)pts[i].y;
		}		
		
		return;	
	}
	/********
	 * 	protected/private
	 *******/
	private final NyARDoublePoint3d _tmp_d3p=new NyARDoublePoint3d();
	protected void initInstance(PApplet parent,String i_cparam_file, int i_width,int i_height,NyAR4PsgConfig i_config) throws NyARException
	{
		super.initInstance(parent,i_cparam_file,i_width,i_height,i_config);
		//互換性の維持目的
		PMatrix2GLProjection(this._ps_projection,this.transmat);
	}
	
	

	protected SingleMarkerBaseClass()
	{
		super();
	}


	
	
	
	
}