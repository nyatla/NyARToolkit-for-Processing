package jp.nyatla.nyar4psg;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.opengl.PGraphicsOpenGL;
import jp.nyatla.nyar4psg.utils.PatchCollection;
import jp.nyatla.nyar4psg.utils.PatchCollection_Psg2x;
import jp.nyatla.nyar4psg.utils.PatchCollection_Psg302;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.markersystem.NyARSingleCameraView;

/**
 * カメラ1台の視点を管理するクラスです。
 * オブジェクト検出クラスに視点情報に関連した機能を提供します。
 */
public class SingleCameraView
{
	final PatchCollection _patch_collection;
	/** このリソースはフレームワーク内で共有します。*/
	public final NyARSingleCameraView _view;
	PApplet _ref_applet;
	/** このリソースはフレームワーク内で共有します。*/
	SingleCameraView(PApplet i_applet,NyARParam i_ar_param,int i_patch_id)
	{
		this._view=new NyARSingleCameraView(i_ar_param);
		this._ref_applet=i_applet;
		this._patch_collection=createPatchCollection(i_patch_id);
		return;
	}
	private float _clip_far;
	private float _clip_near;
	
	/**　バックグラウンド用のModelviewMatrixです。*/
	final protected PMatrix3D _ps_background_mv=new PMatrix3D();	
	final private PMatrix3D _ps_projection=new PMatrix3D();
	/**
	 * この関数は、PImageをバックグラウンドへ描画します。PImageはfarclip面+1の部分に描画します。
	 * <div>この関数は、次のコードと等価です。</div>
	 * <hr/>
	 * :<br/>
	 * PMatrix3D om=new PMatrix3D(((PGrapPGraphicsOpenGLhics3D)g).projection);<br/>
	 * setBackgroundOrtho(img.width,img.height)<br/>
	 * pushMatrix();<br/>
	 * resetMatrix();<br/>
	 * translate(0,0,-(far*0.99f));<br/>
	 * image(img,-width/2,-height/2);<br/>
	 * popMatrix();<br/>
	 * setPerspective(om);<br/>
	 * :<br/>
	 * <hr/>
	 * この関数は、PrjectionMatrixとModelViewMatrixを復帰するため、若干のオーバヘッドがあります。
	 * 高速な処理が必要な場合には、展開してください。
	 * @param i_img
	 * 背景画像を指定します。
	 */
	public void drawBackground(PImage i_img)
	{
		PApplet pa=this._ref_applet;
		PGraphicsOpenGL pgl=((PGraphicsOpenGL)pa.g);
		//行列の待避
		pgl.pushProjection();
		this.setBackgroundOrtho(i_img.width,i_img.height);
		pa.pushMatrix();
		pa.setMatrix(this._ps_background_mv);
		pa.image(i_img,-i_img.width/2,-i_img.height/2);
		pa.popMatrix();
		//行列の復帰
		pgl.popProjection();
	}
	
	
	/**
	 * この関数は、正射影行列をProcessingへセットします。
	 * 画面の中心が0,0にセットされます。
	 * nearクリップには、{@link #setARClipping}でセットしたクリップ面を指定します。
	 *　farクリップには、{@link #setARClipping}でセットしたクリップ面+1を指定します。
	 * <div>この関数は、次のコードと等価です。</div>
	 * <hr/>
	 * :<br/>
	 * //for 1.x<br/>
	 * ortho(-i_width/2, i_width/2,-i_height/2,i_height/2,near,far+1);<br/>
	 * //for 2.x<br/>
	 * ortho(0,i_width,0,i_height,near,far+1);<br/>
	 * :<br/>
	 * <hr/>
	 * @param i_width
	 * 幅を指定します。
	 * @param i_height
	 * 高さを指定します。
	 */
	public void setBackgroundOrtho(int i_width,int i_height)
	{		
		this._patch_collection.setBackgroundOrtho(this._ref_applet, i_width, i_height,this._clip_near,this._clip_far);
//		this._ref_applet.ortho(0,i_width,0,i_height,this._clip_near,this._clip_far+1);
	}
	
	/**
	 * 現在の正射影行列を返します。
	 * @return
	 */
	public PMatrix3D getProjectionMatrix()
	{
		return this._ps_projection;
	}	
	/**
	 * この関数は、視錐台のクリップ面を設定します。この値のデフォルト値は、{@link #FRUSTUM_DEFAULT_NEAR_CLIP}と{@link #FRUSTUM_DEFAULT_FAR_CLIP}です。
	 * 設定値は、次回の{@link #setARPerspective()}から影響を及ぼします。現在の設定値にただちに影響を及ぼすものではありません。
	 * @param i_width
	 * @param i_height
	 * @param i_near
	 * NearPlaneの値を設定します。単位は[mm]です。
	 * @param i_far
	 * FarPlaneの値を設定します。単位は[mm]です。
	 */
	public void setARClipping(int i_width,int i_height,float i_near,float i_far)
	{
		this._clip_far=i_far;
		this._clip_near=i_near;
		this._ps_background_mv.reset();
		this._ps_background_mv.translate(0,0,-i_far);
		NyARDoubleMatrix44 tmp=new NyARDoubleMatrix44();
		this._view.getARParam().getPerspectiveProjectionMatrix().makeCameraFrustumRH(i_width,i_height,i_near,i_far,tmp);
		NyPsUtils.nyarMat2PsMat(tmp,this._ps_projection);
	}
	/**
	 * バージョンIDからパッチコレクションオブジェクトを返します。からコンストラクタから使います。
	 * @param i_version_id
	 * ProcessingのバージョンIDです。
	 * @return
	 */
	private static PatchCollection createPatchCollection(int i_version_id)
	{
		switch(i_version_id){
		case NyAR4PsgConfig.PV_221:
			return new PatchCollection_Psg2x();
		case NyAR4PsgConfig.PV_302:
		default:
			return new PatchCollection_Psg302();
		}
	}	
	
	
}
