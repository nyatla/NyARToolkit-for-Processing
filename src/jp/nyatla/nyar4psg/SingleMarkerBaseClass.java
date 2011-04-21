package jp.nyatla.nyar4psg;

import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

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
	/** モデルビュー行列を保持します。*/
	protected final PMatrix3D _pmodelview_mat=new PMatrix3D();
	/** begin-endシーケンスで使う。*/
	private PMatrix3D _old_matrix;
	
	/**
	 * この関数は、マーカの座標変換行列を返します。
	 * 返却する行列はProcessing座標系です。
	 * @return
	 */
	public PMatrix3D getMarkerMatrix()
	{
		return new PMatrix3D(this._pmodelview_mat);
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
	public void beginTransform(PGraphicsOpenGL i_pgl)
	{
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
	 * setARPerspective(prev_mat);//prev_matは現在の行列退避用。<br/>
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
		this._ref_papplet.setMatrix(this._pmodelview_mat);
		return;	
	}
	/**
	 * {@link #beginTransform}でセットしたProjectionとModelViewを元に戻します。
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
	protected void updateTransmat(NyARSquare i_square,NyARTransMatResult i_src)
	{
		matResult2PMatrix3D(i_src,this._coord_system,this._pmodelview_mat);
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
	protected void initInstance(PApplet parent,String i_cparam_file, int i_width,int i_height,int i_coord_system)
	{
		super.initInstance(parent,i_cparam_file,i_width,i_height,i_coord_system);
		//互換性の維持目的
		PMatrix2GLProjection(this._ps_projection,this.transmat);
	}
	
	

	protected SingleMarkerBaseClass(PApplet parent,String i_cparam_file, int i_width,int i_height,int i_coord_system)
	{
		super();
		this.initInstance(parent,i_cparam_file,i_width,i_height,i_coord_system);
	}


	
	
	
	
}