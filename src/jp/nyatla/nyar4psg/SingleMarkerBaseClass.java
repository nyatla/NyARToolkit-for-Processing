package jp.nyatla.nyar4psg;

import javax.media.opengl.GL;

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
	 * <br/>EN:
	 * The angle value in radian unit of "x,y,z" .
	 * @deprecated
	 * この変数は互換性の為に残されています。{@link #allocMarkerMatrix()}で得られる値から計算してください。
	 */
	public final PVector angle=new PVector();
	/**
	 * [read only]マーカのx,y,zの平行移動量です。
	 * この角度は、ARToolKit座標系での値になります。
	 * <br/>EN:
	 * The translation value in radian unit of "x,y,z".
	 * @deprecated
	 * この変数は互換性の為に残されています。{@link #allocMarkerMatrix()}で得られる値から計算してください。
	 */
	public final PVector trans=new PVector();
	/**
	 * [read only]検出したマーカの4隅の２次元画像上の位置です。
	 * <br/>EN:
	 * The position of 4 corner of marker.
	 * @deprecated
	 * この変数は互換性の為に残されています。{@link #allocMarkerVertex2D()}の値を使用してください。
	 */
	public final int[][] pos2d=new int[4][2];
	/**
	 * [read only]検出したマーカの変換行列です。
	 * この角度は、ARToolKit座標系での値になります。
	 * <br/>EN:
	 * The transform matrix of detected marker.
	 * @deprecated
	 * この変数は互換性の為に残されています。{@link #allocMarkerMatrix()}で得られる値を使用してください。
	 */
	public final double[] transmat=new double[16];
	
	protected final PMatrix3D _pmat=new PMatrix3D();
	/**
	 * この関数は、マーカの座標変換行列を返します。
	 * 返却する行列はProcessing座標系です。
	 * @return
	 */
	public PMatrix3D allocMarkerMatrix()
	{
		return new PMatrix3D(this._pmat);
	}
	/**
	 * この関数は、マーカのスクリーン上の4頂点を返します。
	 * @return
	 */
	public PVector[] allocMarkerVertex2D()
	{
		PVector[] r=new PVector[4];
		for(int i=0;i<4;i++){
			r[i]=new PVector(pos2d[i][0],pos2d[i][1]);
		}
		return r;
	}
	
	/**
	 * 座標変換を実行したMatrixを準備します。
	 * この関数を実行すると、processingの座標系がマーカ表面に設定されます。
	 * 描画終了後には、必ずendTransform関数を呼び出して座標系を戻してください。
	 * <br/>EN:
	 * This function sets corresponding transform matrix to the surface of the marker to OpenGL.
	 * The coordinate system of processing moves to the surface of the marker when this function is executed.
	 * Must return the coordinate system by using endTransform function at the end.
	 * @param i_pgl
	 * PGraphicsOpenGLインスタンスを設定します。processingのgメンバをキャストして設定してください。
	 * <br/>EN:
	 * Specify PGraphicsOpenGL instance.
	 * Set cast "g" member of processing graphics object.
	 */
	public void beginTransform(PGraphicsOpenGL i_pgl)
	{
		if(this._gl!=null){
			this._ref_papplet.die("The function beginTransform is already called.", null);			
		}
		this._pgl=i_pgl;
		this._gl=i_pgl.gl;
		{	//projectionの切り替え
			this._gl.glMatrixMode(GL.GL_PROJECTION);
			this._gl.glPushMatrix();
			this._gl.glLoadMatrixd(this.projection,0);
			this._old_matrix=this._pgl.projection;
			this._pgl.projection=this._ps_projection;
			this._gl.glMatrixMode(GL.GL_MODELVIEW);
		}
		{	//ModelViewの設定
			this._ref_papplet.pushMatrix();
			this._ref_papplet.setMatrix(this._pmat);
		}
		return;	
	}
	/**
	 * beginTransformによる座標変換を解除して元に戻します。
	 * <br/>EN:
	 * This function recover coordinate system that was changed by beginTransform function.
	 */
	public void endTransform()
	{
		if(this._gl==null){
			this._ref_papplet.die("The function beginTransform is never called.", null);			
		}
		{	//ModelViewの復帰
			this._ref_papplet.popMatrix();
		}
		{	//projectionの復帰
			this._pgl.projection=this._old_matrix;
			this._gl.glMatrixMode(GL.GL_PROJECTION);
			this._gl.glPopMatrix();
			this._gl.glMatrixMode(GL.GL_MODELVIEW);
		}
		
		this._gl=null;
		this._pgl=null;
		return;
	}	
	protected void updateTransmat(NyARSquare i_square,NyARTransMatResult i_src)
	{
		matResult2PMatrix3D(i_src,this._coord_system,this._pmat);
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
	
	
	//キャッシュたち
	private GL _gl=null;
	private PGraphicsOpenGL _pgl=null;	
	private PMatrix3D _old_matrix;
	protected SingleMarkerBaseClass(PApplet parent,String i_cparam_file, int i_width,int i_htight,int i_coord_system)
	{
		super(parent,i_cparam_file,i_width,i_htight,i_coord_system);
	}


	
	
	
	
}