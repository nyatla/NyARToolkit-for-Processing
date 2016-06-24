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



import java.io.InputStream;
import java.util.ArrayList;

import jp.nyatla.nyar4psg.utils.NyARPsgBaseClass;
import jp.nyatla.nyar4psg.utils.PImageRaster;
import jp.nyatla.nyar4psg.utils.PImageSensor;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftDataSet;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.*;
import processing.core.*;
import processing.opengl.*;
import jp.nyatla.nyartoolkit.nftsystem.NyARNftSystem;
import jp.nyatla.nyartoolkit.nftsystem.NyARNftSystemConfig;


/**
 * このクラスは、同時に複数のNFTマーカを取り扱える検出・トラッキングクラスです。
 * PImage画像の入力から、NFTマーカの位置姿勢を検出することができます。
 */
public class MultiNft extends NyARPsgBaseClass
{
	
	protected PImageSensor _ss;
	protected NyARNftSystem _ns;
	final private int _coordinate_system;



	/** PSGのIDとMarkerSystemのマッピング*/
	private ArrayList<Integer> _id_map=new ArrayList<Integer>();

	/**
	 * インスタンスを初期化します。
	 * @param parent
	 * @param i_width
	 * @param i_height
	 * @param i_cparam_file
	 * @param i_patt_resolution
	 * @param i_projection_coord_system
	 * @throws NyARRuntimeException 
	 */	
	private MultiNft(PApplet i_applet,SingleCameraView i_view,int i_coordinate_system)
	{
		super(i_applet,i_view);
		this._coordinate_system=i_coordinate_system;
		NyARIntSize s=i_view._view.getARParam().getScreenSize();
		this._ss=new PImageSensor(new NyARIntSize(s.w,s.h));
		this._ns=new NyARNftSystem(new NyARNftSystemConfig(i_view._view.getARParam()));
		return;
	}
	
	private MultiNft(PApplet i_applet,NyARParam i_param,NyAR4PsgConfig i_config)
	{
		this(
			i_applet,
			new SingleCameraView(i_applet,i_param,i_config._ps_patch_version),
			i_config._coordinate_system);
	}
	/**
	 * コンストラクタです。カメラパラメータを{@link InputStream}から読みだしてインスタンスを生成します。
	 * @param parent
	 * 親となるAppletオブジェクトを指定します。
	 * @param i_width
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_height
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_cparam_is
	 * ARToolKitフォーマットのカメラパラメータファイルを読み出す{@link InputStream}です。
	 * @param i_config
	 */
	public MultiNft(PApplet parent, int i_width,int i_height,InputStream i_cparam_is,NyAR4PsgConfig i_config)
	{
		this(parent,NyARParam.loadFromARParamFile(i_cparam_is,i_width,i_height),i_config);
	}

	/**
	 * コンストラクタです。
	 * @param parent
	 * 親となるAppletオブジェクトを指定します。
	 * @param i_cparam_file
	 * ARToolKitフォーマットのカメラパラメータファイルの名前を指定します。
	 * @param i_width
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_height
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_config
	 * コンフィギュレーションオブジェクトを指定します。
	 */
	public MultiNft(PApplet parent, int i_width,int i_height,String i_cparam_file,NyAR4PsgConfig i_config)
	{
		this(parent,i_width,i_height,parent.createInput(i_cparam_file),i_config);
	}
	/**
	 * コンストラクタです。
	 * {@link MultiNft#MultiNft(PApplet, int, int, String, NyAR4PsgConfig)}のコンフィギュレーションに、{@link NyAR4PsgConfig#CONFIG_DEFAULT}を指定した物と同じです。
	 * @param parent
	 * 親となるAppletオブジェクトを指定します。
	 * @param i_width
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_height
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_cparam_file
	 * ARToolKitフォーマットのカメラパラメータファイルの名前を指定します。
	 */
	public MultiNft(PApplet parent,int i_width,int i_height,String i_cparam_file)
	{
		this(
			parent,
			i_width,i_height,i_cparam_file,NyAR4PsgConfig.CONFIG_OLD);
		return;
	}
	/**
	 * コンストラクタです。
	 * OpenCVのカメラパラメータ値を使ってインスタンスを生成します。
	 * @param i_width
	 * カメラパラメータのサイズ値
	 * @param i_height
	 * カメラパラメータのサイズ値
	 * @param i_intrinsic_matrix
	 * 3x3 matrix
	 * このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するintrinsic_matrixの値と合致します。
	 * @param i_distortion_coeffs
	 * 4x1 matrix
	 * このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するdistortion_coeffsの値と合致します。
	 * @param i_screen_width
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 * @param i_screen_height
	 * 入力画像の横解像度を指定します。通常、キャプチャ画像のサイズを指定します。
	 */	
	public MultiNft(PApplet parent,int i_width,int i_height,double[] i_intrinsic_matrix,double[] i_distortion_coeffs,int i_screen_width,int i_screen_height)
	{
		this(
			parent,
			NyARParam.loadFromCvCalibrateCamera2Result(i_width, i_height, i_intrinsic_matrix, i_distortion_coeffs,i_screen_width,i_screen_height),
			NyAR4PsgConfig.CONFIG_OLD);
		return;
	}

	
	
	/** begin-endシーケンスの判定用*/
	private boolean _is_in_begin_end_session=false;
	/**
	 * この関数は、ProcessingのProjectionMatrixとModelview行列に指定idのマーカ平面にセットします。
	 * 必ず{@link #endTransform}とペアで使ってください。
	 * 関数を実行すると、現在のModelView行列とProjection行列を退避した後に、新しい行列がセットされます。
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
		this._ref_papplet.setMatrix(this.getMatrix(i_id));
		return;	
	}
	/**
	 * この関数は、{@link #beginTransform}でセットしたProjectionとModelViewを元に戻します。
	 * 必ず{@link #beginTransform}とペアで使ってください。
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
		this.detect(i_image,true);
	}
	/**
	 * この関数は、画像からマーカーの検出処理を実行します。
	 * 引数と戻り値の詳細は、{@link #detect(PImage)}を参照してください。
	 * @param i_image
	 * 検出処理を行う画像を指定します。
	 * @param i_with_loadpixels
	 * {@link PImage#loadPixels()}を実行するかのフラグ値です。
	 * @see #detect(PImage)
	 */
	public void detect(PImage i_image,boolean i_with_loadpixels)
	{
		if(i_with_loadpixels){
			i_image.loadPixels();
			this._ss.update(i_image);
			i_image.updatePixels();
		}else{
			this._ss.update(i_image);
		}
		this.detect(this._ss);
	}
	/**
	 * {@link PImageSensor}の内容でインスタンスを更新します。
	 * 他のインスタンスで処理した{@link PImageSensor}を使うことができます。
	 * @param i_source_image
	 */
	public void detect(PImageSensor i_source_image)
	{
		this._ns.update(i_source_image);
	}
	private int addNftTarget(NyARNftDataSet i_dataset)
	{
		//初期化済みのアイテムを生成
		int psid=-1;
		try{
			this._id_map.add(this._ns.addNftTarget(i_dataset));
			psid=this._id_map.size()-1;
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Catch an exception!");
		}
		return psid;
	}	
	
	/**
	 * この関数は、NFTファイルセットを{@link InputStream}から読みだして、NFTターゲットを生成して登録します。
	 * 同じターゲットを複数個登録することはできません。
	 * @param i_iset_is
	 * isetファイルを読み出す{@link InputStream}
	 * @param i_fset_is
	 * fsetファイルを読み出す{@link InputStream}
	 * @param i_fset3_is
	 * fset3ファイルを読み出す{@link InputStream}
	 * @param i_page_no
	 * fset3のページ番号を指定します。
	 * @param i_target_width
	 * NFTパターンの横幅をmm単位で指定します。
	 * @return
	 * 0から始まるマーカーIDを返します。
	 * この数値は、マーカを区別するためのId値です。0から始まり、{@link #addNftTarget}関数を呼ぶたびにインクリメントされます。
	 * {@link #getMatrix},{@link #isExistTarget},{@link #addNftTarget},
	 * {@link #screen2CoordSystem},{@link #pickupTargetImage},{@link #pickupRectTargetImage}
	 * のid値に使います。
	 */	
	public int addNftTarget(InputStream i_iset_is,InputStream i_fset_is,InputStream i_fset3_is,int i_page_no,float i_target_width)
	{
		return this.addNftTarget(
			NyARNftDataSet.loadFromNftFiles(
			i_iset_is,i_fset_is,i_fset3_is,i_page_no,
			Float.isNaN(i_target_width)?Double.NaN:i_target_width));
	}
	
	
	/**
	 * この関数は、NFTファイルセットからNFTターゲットを生成して登録します。
	 * 同じターゲットを複数個登録することはできません。
	 * @param i_file_name_prefix
	 * NFTファイルセットの名前を指定します。名前は拡張子を取り除いてください。
	 * 例えば"d:\\test"は、"test.iset","test.fset","test.fset3"のファイルセットを登録します。
	 * @param i_page_no
	 * fset3のページ番号を指定します。
	 * @param i_target_width
	 * NFTパターンの横幅をmm単位で指定します。
	 * @return
	 * 0から始まるマーカーIDを返します。
	 * この数値は、マーカを区別するためのId値です。0から始まり、{@link #addNftTarget}関数を呼ぶたびにインクリメントされます。
	 * {@link #getMatrix},{@link #isExistTarget},{@link #addNftTarget},
	 * {@link #screen2CoordSystem},{@link #pickupTargetImage},{@link #pickupRectTargetImage}
	 * のid値に使います。
	 */
	public int addNftTarget(String i_file_name_prefix,int i_page_no,float i_target_width)
	{
		return this.addNftTarget(
			this._ref_papplet.createInput(i_file_name_prefix+".iset"),
			this._ref_papplet.createInput(i_file_name_prefix+".fset"),
			this._ref_papplet.createInput(i_file_name_prefix+".fset3"),i_page_no,
			i_target_width);
	}	
	
	
	
	/**
	 * この関数は、NFTファイルセットからNFTターゲットを生成して登録します。
	 * {@link #addNftTarget(String, int, float)}の第二引数に0を設定したものと同じです。
	 * @param i_file_name_prefix
	 * {@link #addNftTarget(String, int, float)}を参照してください。
	 * @param i_target_width
	 * {@link #addNftTarget(String, int, float)}を参照してください。
	 * @return
	 * {@link #addNftTarget(String, int, float)}を参照してください。
	 */
	public int addNftTarget(String i_file_name_prefix,float i_target_width)
	{
		return this.addNftTarget(i_file_name_prefix,0,i_target_width);
	}
	/**
	 * この関数は、NFTファイルセットからNFTターゲットを生成して登録します。
	 * NFTターゲット画像のサイズは、ファイルセットのものをそのまま使います。
	 * {@link #addNftTarget(String, int, float)}の第二引数に0,第三引数にNaNを設定したものと同じです。
	 * @param i_file_name_prefix
	 * {@link #addARMarker(String, int, int, double)}を参照。
	 */
	public int addNftTarget(String i_file_name_prefix)
	{
		return this.addNftTarget(i_file_name_prefix,Float.NaN);
	}
	/**
	 * この関数は、NFTデータセットを{@link InputStream}から読みだして、NFTターゲットを登録します。
	 * 同じターゲットを複数個登録することはできません。
	 * @param i_isetdataset
	 * nftdatasetファイルを読み出す{@link InputStream}です。
	 * @param i_target_width
	 * NFTパターンの横幅をmm単位で指定します。
	 * @return
	 * 0から始まるマーカーIDを返します。
	 * この数値は、マーカを区別するためのId値です。0から始まり、{@link #addNftTarget}関数を呼ぶたびにインクリメントされます。
	 * {@link #getMatrix},{@link #isExistTarget},{@link #addNftTarget},
	 * {@link #screen2CoordSystem},{@link #pickupTargetImage},{@link #pickupRectTargetImage}
	 * のid値に使います。
	 */	
	public int addNftTarget(InputStream i_isetdataset,int i_page_no,float i_target_width)
	{
		return this.addNftTarget(NyARNftDataSet.loadFromNftDataSet(i_isetdataset, i_target_width));
	}
	
	/**
	 * この関数は、マーカの姿勢行列を返します。
	 * 返却した行列は{@link PApplet#setMatrix}でProcessingにセットできます。
	 * @param i_armk_id
	 * マーカidを指定します。
	 * @return
	 * マーカの姿勢行列を返します。
	 */
	public PMatrix3D getMatrix(int i_id)
	{
		PMatrix3D p=new PMatrix3D();
		//存在チェック
		if(!this.isExist(i_id)){
			this._ref_papplet.die("Marker id " +i_id + " is not exist on image.", null);
		}
		int msid=this._id_map.get(i_id);
		try{
			matResult2PMatrix3D(this._ns.getTransformMatrix(msid),this._coordinate_system,p);
		}catch(Exception e){
			e.printStackTrace();
			this._ref_papplet.die("Catch an exception!");
		}			
		return p;
	}

	/**
	 * 指定idのターゲットが有効かを返します。
	 * @param i_id
	 * マーカidを指定します。
	 * @return
	 * マーカが有効ならばtrueです。無効ならfalseです。
	 */
	public boolean isExist(int i_id)
	{
		int msid=this._id_map.get(i_id);
		try{
			return this._ns.isExist(msid);
		}catch(Exception e){
			this._ref_papplet.die("Catch an exception!", null);
			return false;
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
	public PVector object2ScreenCoordSystem(int i_id,double i_x,double i_y,double i_z)
	{
		try{
			int msid=this._id_map.get(i_id);
			NyARDoublePoint2d pos=new NyARDoublePoint2d();
			this._ns.getScreenPos(msid, i_x, i_y, i_z,pos);
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
	public PVector screen2ObjectCoordSystem(int i_id,int i_x,int i_y)
	{
		try{
			int msid=this._id_map.get(i_id);
			PVector ret=new PVector();
			NyARDoublePoint3d tmp=new NyARDoublePoint3d();
			this._ns.getPlanePos(msid,i_x, i_y,tmp);
			ret.x=(float)tmp.x;
			ret.y=(float)tmp.y;
			ret.z=(float)tmp.z;
			if(this._coordinate_system==NyAR4PsgConfig.CS_LEFT_HAND){
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
	public PImage pickupImage(int i_id,int i_x1,int i_y1,int i_x2,int i_y2,int i_x3,int i_y3,int i_x4,int i_y4,int i_out_w_pix,int i_out_h_pix)
	{
		PImage p=new PImage(i_out_w_pix,i_out_h_pix);
		try{
			PImageRaster pr=new PImageRaster(p);
			int msid=this._id_map.get(i_id);
			this._ns.getPlaneImage(msid, this._ss, i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4,pr);
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
	public PImage pickupRectImage(int i_id,int i_l,int i_t,int i_w,int i_h,int i_out_w_pix,int i_out_h_pix)
	{
		return pickupImage(
			i_id,
			i_l+i_w-1,i_t+i_h-1,
			i_l,i_t+i_h-1,
			i_l,i_t,
			i_l+i_w-1,i_t,
			i_out_w_pix,i_out_h_pix);
	}
	//
	//	duplicated functions.
	//
	
	/**
	 * {@link #isExist}のエイリアスです。
	 * @param i_id
	 * @return
	 */
	@Deprecated
	public boolean isExistTarget(int i_id)
	{
		return this.isExist(i_id);
	}
	/**
	 * {@link #getMatrix}のエイリアスです。
	 * @param i_id
	 * @return
	 */
	@Deprecated
	public PMatrix3D getTargetMatrix(int i_id)
	{
		return this.getMatrix(i_id);
	}
	/**
	 * {@link #pickupImage}のエイリアスです。
	 */
	@Deprecated
	public PImage pickupTargetImage(int i_id,int i_x1,int i_y1,int i_x2,int i_y2,int i_x3,int i_y3,int i_x4,int i_y4,int i_out_w_pix,int i_out_h_pix)
	{
		return this.pickupImage(i_id, i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4, i_out_w_pix, i_out_h_pix);
	}
	/**
	 * {@link #pickupRectImage}のエイリアスです。
	　*/
	@Deprecated
	public PImage pickupRectTargetImage(int i_id,int i_l,int i_t,int i_w,int i_h,int i_out_w_pix,int i_out_h_pix)
	{
		return this.pickupRectImage(i_id, i_l, i_t, i_w, i_h, i_out_w_pix, i_out_h_pix);
	}	
	
	/**
	 * {@link #screen2ObjectCoordSystem}のエイリアスです。
	 */
	@Deprecated	
	public PVector screen2TargetCoordSystem(int i_id,int i_x,int i_y)
	{
		return this.screen2ObjectCoordSystem(i_id, i_x, i_y);
	}
	/**
	 * {link #object2ScreenCoordSystem}のエイリアスです。
	 */
	@Deprecated
	public PVector target2ScreenCoordSystem(int i_id,double i_x,double i_y,double i_z)
	{
		return this.object2ScreenCoordSystem(i_id, i_x, i_y, i_z);
	}

	
	
	/**
	 * Processing callback function
	 * インスタンスが起動したマルチスレッドを終了時に停止します。
	 */
	public void dispose()
	{
		this._ns.shutdown();
	}
}
