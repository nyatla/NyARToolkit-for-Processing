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
import javax.media.opengl.*;
import processing.core.*;
import processing.opengl.*;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.detector.*;

/**
 * このクラスは、内部クラスです。
 * @author nyatla
 */
class ARToolkitThreshold4PImage implements INyARRasterFilter_RgbToBin
{
	private int _threshold;

	public ARToolkitThreshold4PImage()
	{
	}
	public void setThreshold(int i_threshold)
	{
		this._threshold = i_threshold;
	}

	public void doFilter(INyARRgbRaster i_input, NyARBinRaster i_output) throws NyARException
	{
		INyARBufferReader in_buffer_reader=i_input.getBufferReader();	
		INyARBufferReader out_buffer_reader=i_output.getBufferReader();

		assert (out_buffer_reader.isEqualBufferType(INyARBufferReader.BUFFERFORMAT_INT1D_BIN_8));
		assert (in_buffer_reader.isEqualBufferType(INyARBufferReader.BUFFERFORMAT_INT1D_X8R8G8B8_32));
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);

		NyARIntSize size = i_output.getSize();
		convert((int[]) in_buffer_reader.getBuffer(), (int[]) out_buffer_reader.getBuffer(), size);
		return;
	}

	private void convert(int[] i_in, int[] i_out, NyARIntSize i_size)
	{
		final int th=this._threshold*3;
		int w;
		int xy;
		final int pix_count   =i_size.h*i_size.w;
		final int pix_mod_part=pix_count-(pix_count%8);
		for(xy=pix_count-1;xy>=pix_mod_part;xy--){
			w=i_in[xy];
			i_out[xy]=(w&0xff)+((w>>8)&0xff)+((w>>16)&0xff)<=th?0:1;
		}
		//タイリング
		for (;xy>=0;) {
			w=i_in[xy];
			i_out[xy]=(w&0xff)+((w>>8)&0xff)+((w>>16)&0xff)<=th?0:1;
			xy--;
			w=i_in[xy];
			i_out[xy]=(w&0xff)+((w>>8)&0xff)+((w>>16)&0xff)<=th?0:1;
			xy--;
			w=i_in[xy];
			i_out[xy]=(w&0xff)+((w>>8)&0xff)+((w>>16)&0xff)<=th?0:1;
			xy--;
			w=i_in[xy];
			i_out[xy]=(w&0xff)+((w>>8)&0xff)+((w>>16)&0xff)<=th?0:1;
			xy--;
			w=i_in[xy];
			i_out[xy]=(w&0xff)+((w>>8)&0xff)+((w>>16)&0xff)<=th?0:1;
			xy--;
			w=i_in[xy];
			i_out[xy]=(w&0xff)+((w>>8)&0xff)+((w>>16)&0xff)<=th?0:1;
			xy--;
			w=i_in[xy];
			i_out[xy]=(w&0xff)+((w>>8)&0xff)+((w>>16)&0xff)<=th?0:1;
			xy--;
			w=i_in[xy];
			i_out[xy]=(w&0xff)+((w>>8)&0xff)+((w>>16)&0xff)<=th?0:1;
			xy--;
		}
		return;
	}
}

/**
 * このクラスは、内部クラスです。
 * @author nyatla
 */
class NyARRgbRaster_PImage extends NyARRgbRaster_BasicClass implements INyARRgbRaster
{
	private class PixelReader implements INyARRgbPixelReader
	{
		private NyARRgbRaster_PImage _parent;

		public PixelReader(NyARRgbRaster_PImage i_parent)
		{
			this._parent = i_parent;
		}

		public void getPixel(int i_x, int i_y, int[] o_rgb)
		{
			int[] ref_buf = this._parent._ref_image.pixels;
			int bp = ref_buf[(i_x + i_y * this._parent._size.w)];
			o_rgb[0] = ((bp>>16) & 0xff);// R
			o_rgb[1] = ((bp>>8) & 0xff);// G
			o_rgb[2] = (bp & 0xff);// B
			return;
		}

		public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
		{
			int[] ref_buf = this._parent._ref_image.pixels;
			int width = _parent._size.w;
			int bp;
			for (int i = i_num - 1; i >= 0; i--) {
				bp =ref_buf[(i_x[i] + i_y[i] * width)];
				o_rgb[i * 3 + 0] = (bp>>16) & 0xff;// R
				o_rgb[i * 3 + 1] = (bp>>8) & 0xff;// G
				o_rgb[i * 3 + 2] = (bp & 0xff);// B
			}
			return;
		}
	}
	public class BufferReader extends NyARBufferReader
	{
		NyARRgbRaster_PImage _parent;
		public BufferReader(NyARRgbRaster_PImage i_parent)
		{
			super();
			this._buffer_type=INyARBufferReader.BUFFERFORMAT_INT1D_X8R8G8B8_32;
			this._parent=i_parent;
		}
		public Object getBuffer()
		{
			return this._parent._ref_image.pixels;
		}
	}
	private INyARRgbPixelReader _rgb_reader;
	private INyARBufferReader _buffer_reader;

	public NyARRgbRaster_PImage(int i_width, int i_height)
	{
		super(new NyARIntSize(i_width,i_height));

		this._rgb_reader = new PixelReader(this);
		this._buffer_reader=new BufferReader(this);
		return;
	}
	private PImage _ref_image;
	public void bindImage(PImage i_img)
	{
		this._ref_image=i_img;
		this._ref_image.updatePixels();
	}
	public void unBindImage(PImage i_img)
	{
		this._ref_image=null;
		return;
	}
	public INyARRgbPixelReader getRgbPixelReader()
	{
		return this._rgb_reader;
	}
	public INyARBufferReader getBufferReader()
	{
		return this._buffer_reader;
	}
}



/**
 * This class calculate a ARToolKit base transformation matrix from PImage.
 * This class handles one marker, and calculates the transformation matrix that displays the object above the marker.
 * <pre>
 * NyARBoard needs two an external configuration file "Marker file" and "Camera parameter" files.
 * These are compatible with the original ARToolKit. 
 * Please place these under the data directory of the sketch. (Look at example.)
 * </pre>
 * <br/>JP:
 * このクラスは、PImageからARToolKit準拠の変換行列を求めるクラスです。
 * マーカを１枚の板に見立てて、そこを中心にした座標系を計算します。
 * 同時検出可能なマーカは１種類、１個です。
 * <pre>
 * NyARBoardは、２つの外部設定ファイル"マーカファイル"と"カメラパラメータ"ファイルを必要とします。
 * これらはARToolKitのものと互換性があります。
 * これらはスケッチのdataディレクトリ以下に配置をして下さい。（exampleを見てください。）
 * </pre>
 * @author nyatla
 *
 */
public class NyARBoard
{
	/**
	 * version information.
	 * <br/>JP:
	 * バージョン文字列です。
	 */
	public final String VERSION = "NyAR4psg/0.2.1;NyARToolkit for java/2.2.0;ARToolKit/2.72.1";
	/**
	 * NyARBoard ignores that it lost the marker while under specified number.
	 * Must be "n&gt;=0".
	 * <br/>JP:
	 * 消失遅延数。マーカ消失後、指定回数は過去の情報を維持します。
	 * 値は0以上であること。
	 */
	public int lostDelay =10;
	/**
	 * It is a number in which it continuously lost the marker.
	 * This value range is "0&lt;n&lt;lostDelay"
	 * <br/>JP:
	 * 連続でマーカを見失った回数。この値は0<n<lostDelayをとります。
	 */
	public int lostCount = 0;
	/**
	 * The threshold value of marker pattern confidence.
	 * This value range is "0.0&lt;n&lt;1.0".
	 * When marker confidence is larger than this value, NyARBoard detects the marker.
	 * <br/>JP:
	 * マーカの座標変換を行う閾値。0.0&lt;n&lt;1.0の値をとります。
	 * この数値より一致度が大きい場合のみ、マーカを検出したと判定され、座標計算が行われます。
	 */
	public double cfThreshold=0.4;
	/**
	 * The projection matrix adday for OpenGL projection.
	 * This value is initialized by constructor.
	 * <br/>JP:
	 * OpenGLのProjection配列。コンストラクタで初期化されます。
	 */
	public final double[] projection=new double[16];
	/**
	 * The angle value in radian unit of "x,y,z" .
	 * <br/>JP:
	 * マーカのx,y,zの傾き角度です。
	 */
	public final PVector angle=new PVector();
	/**
	 * The translation value in radian unit of "x,y,z".
	 * <br/>JP:
	 * マーカのx,y,zの平行移動量です。
	 */
	public final PVector trans=new PVector();
	/**
	 * The confidence value of detected marker.
	 * <br/>JP:
	 * 検出したマーカの一致度です。
	 */
	public double confidence=0.0;
	/**
	 * The position of 4 corner of marker.
	 * <br/>JP:
	 * 検出したマーカの4隅の２次元画像上の位置です。
	 */
	public final int[][] pos2d=new int[4][2];
	/**
	 * The transform matrix of detected marker.
	 * <br/>JP:
	 * 検出したマーカの変換行列です。
	 */
	public final double[] transmat=new double[16];
	/**
	 * The threshold value of labeling process from gray scale image.
	 * This value range is "0&lt;=n&lt;=255".
	 * <br/>JP:
	 * マーカ検出時のグレースケール閾値を指定します。この値は、0&lt;=n&lt;=255をります。
	 */
	public int gsThreshold=110;
	
	
	
	private final NyARTransMatResult _result=new NyARTransMatResult();
	private PApplet _pa;
	private NyARCustomSingleDetectMarker _nya;
	private NyARParam _ar_param;
	private NyARRgbRaster_PImage _raster;
	private ARToolkitThreshold4PImage _filter;
	/**
	 * This function is constructor.
	 * <br/>JP:
	 * この関数はコンストラクタです。
	 * @param parent
	 * Specify processing instance.
	 * <br/>JP:
	 * processingのインスタンスを指定します。
	 * @param i_width
	 * Width of source image size for "detect()".
	 * <br/>JP:
	 * detect()に渡す入力画像の幅を指定します。
	 * @param i_htight
	 * Height of source image size for "detect()".
	 * <br/>JP:
	 * detectに渡す入力画像の高さを指定します。
	 * @param i_cparam
	 * The file name of the camera parameter of ARToolKit format.
	 * Place the file to "data" directory at sketch.
	 * <br/>JP:
	 * ARToolKitのパラメータファイル名を指定します。パラメータファイルはdataディレクトリにおいて下さい。
	 * @param i_patt
	 * The file name of the marker pattern file of ARToolkit.
	 * Place the file to "data" directory at sketch.
	 * The marker resolution must be 16x16.
	 * <br/>JP:
	 * マーカのパターンファイル名を指定します。パターンファイルは、dataディレクトリにおいて下さい。
	 * マーカの解像度は、16x16である必要があります。
	 * @param i_patt_width
	 * The length of one side of a square marker in millimeter unit.
	 * <br/>JP:
	 * マーカのサイズを指定します。単位はmmです。
	 */
	public NyARBoard(PApplet parent, int i_width,int i_htight,String i_cparam,String i_patt,int i_patt_width)
	{
		NyARCode code;
		this._pa=parent;
		try{
			this._raster=new NyARRgbRaster_PImage(i_width, i_htight);
			this._ar_param=new NyARParam();
			this._ar_param.loadARParam(this._pa.createInput(i_cparam));
			this._ar_param.changeScreenSize(i_width, i_htight);
			initProjection(this._ar_param,projection);
			code=new NyARCode(16,16);
			code.loadARPatt(this._pa.createInput(i_patt));
			this._filter=new ARToolkitThreshold4PImage();
			this._nya=new NyARCustomSingleDetectMarker(this._ar_param,code,(double)i_patt_width,this._filter);
		}catch(NyARException e){
			this._pa.die("Error while setting up NyARToolkit for java", e);
		}
	}
	/**
	 * This function detect a marker which is must higher confidence in i_image.
	 * When function detects marker, properties (pos2d,angle,trans,confidence,transmat) are updated.
	 * <br/>JP:
	 * i_imageから最も一致度の高いマーカを検出し、cfThreshold以上の一致度であれば、
	 * pos2d,angle,trans,confidence,transmatのプロパティを更新します。
	 * @param i_image
	 * Specify source image.
	 * <br/>JP:
	 * 検出するイメージを設定します。
	 * @return
	 * TRUE if marker found;otherwise FALSE.
	 * <br/>JP:
	 * マーカが検出され、有効な値が得られればTRUEを返します。
	 * TRUEであれば、プロパティが更新されています。
	 */
	public boolean detect(PImage i_image)
	{
		boolean is_marker_exist=false;
		try{
			this._raster.bindImage(i_image);
			//マーカの検出をするよ。
			this._filter.setThreshold(this.gsThreshold);
			is_marker_exist = this._nya.detectMarkerLite(this._raster);
			//マーカ見つかったら一致度確認
			if(is_marker_exist){
				double cf=this._nya.getConfidence();
				if(cf<this.cfThreshold){
					is_marker_exist=false;
				}
			}
			//マーカとして処理？
			if(is_marker_exist){
				final NyARIntPoint[] pts=this._nya.refSquarePosition();
				for(int i=0;i<4;i++){
					this.pos2d[i][0]=pts[i].x;
					this.pos2d[i][1]=pts[i].y;
				}
				this.confidence=this._nya.getConfidence();
				this.lostCount=0;
				//座標変換
				this._nya.getTransmationMatrix(this._result);
				updateTransmat(this._result);

			}else if(this.lostCount<this.lostDelay){
				this.lostCount++;
				is_marker_exist=true;
			}else{
				is_marker_exist=false;				
			}
			
			this._raster.unBindImage(i_image);
			return is_marker_exist;
		}catch(NyARException e){
			this._pa.die("Error while marker detecting up NyARToolkit for java", e);
		}
		return is_marker_exist;
	}
	//キャッシュたち
	private GL _gl=null;
	private PGraphicsOpenGL _pgl=null;

	/**
	 * This function sets corresponding transform matrix to the surface of the marker to OpenGL.
	 * The coordinate system of processing moves to the surface of the marker when this function is executed.
	 * Must return the coordinate system by using endTransform function at the end.
	 * <br/>JP:
	 * 座標変換を実行したMatrixを準備します。
	 * この関数を実行すると、processingの座標系がマーカ表面に設定されます。
	 * 描画終了後には、必ずendTransform関数を呼び出して座標系を戻してください。
	 * @param i_pgl
	 * Specify PGraphicsOpenGL instance.
	 * Please cast and set a "g" member of processing.
	 * <br/>JP:
	 * PGraphicsOpenGLインスタンスを設定します。processingのgメンバをキャストして設定してください。
	 */
	public void beginTransform(PGraphicsOpenGL i_pgl)
	{
		if(this._pgl!=null){
			this._pa.die("The function beginTransform is already called.", null);			
		}
		this._pgl=i_pgl;
		this._gl = i_pgl.beginGL();  // always use the GL object returned by beginGL
		this._gl.glMatrixMode(GL.GL_PROJECTION);
		this._pa.pushMatrix();
		this._pa.resetMatrix();
		this._gl.glLoadMatrixd(this.projection,0);
		this._gl.glMatrixMode(GL.GL_MODELVIEW);
		this._pa.pushMatrix();
		this._pa.resetMatrix();
		this._gl.glLoadMatrixd(this.transmat,0);  
		this._pa.pushMatrix();
		return;
	}
	/**
	 * This function recover coordinate system that was changed by beginTransform function.
	 * <br/>JP:
	 * beginTransformによる座標変換を解除して元に戻します。
	 */
	public void endTransform()
	{
		if(this._pgl==null){
			this._pa.die("The function beginTransform is never called.", null);			
		}
		this._pa.popMatrix();
		this._pa.popMatrix();
		this._gl.glMatrixMode(GL.GL_PROJECTION);
		this._pa.popMatrix();
		this._gl.glMatrixMode(GL.GL_MODELVIEW);
		this._pgl.endGL();
		this._gl=null;
		this._pgl=null;
		return;
	}

	private static void initProjection(NyARParam i_param,double[] o_projection)
	{
		NyARMat trans_mat = new NyARMat(3, 4);
		NyARMat icpara_mat = new NyARMat(3, 4);
		double[][] p = new double[3][3], q = new double[4][4];
		int i, j;

		final NyARIntSize size=i_param.getScreenSize();
		final int width = size.w;
		final int height = size.h;
		
		i_param.getPerspectiveProjectionMatrix().decompMat(icpara_mat, trans_mat);

		double[][] icpara = icpara_mat.getArray();
		double[][] trans = trans_mat.getArray();
		for (i = 0; i < 4; i++) {
			icpara[1][i] = (height - 1) * (icpara[2][i]) - icpara[1][i];
		}

		for (i = 0; i < 3; i++) {
			for (j = 0; j < 3; j++) {
				p[i][j] = icpara[i][j] / icpara[2][2];
			}
		}
		q[0][0] = (2.0 * p[0][0] / (width - 1));
		q[0][1] = (2.0 * p[0][1] / (width - 1));
		q[0][2] = -((2.0 * p[0][2] / (width - 1)) - 1.0);
		q[0][3] = 0.0;

		q[1][0] = 0.0;
		q[1][1] = -(2.0 * p[1][1] / (height - 1));
		q[1][2] = -((2.0 * p[1][2] / (height - 1)) - 1.0);
		q[1][3] = 0.0;

		q[2][0] = 0.0;
		q[2][1] = 0.0;
		q[2][2] = (view_distance_max + view_distance_min) / (view_distance_min - view_distance_max);
		q[2][3] = 2.0 * view_distance_max * view_distance_min / (view_distance_min - view_distance_max);

		q[3][0] = 0.0;
		q[3][1] = 0.0;
		q[3][2] = -1.0;
		q[3][3] = 0.0;

		q[2][2] = q[2][2] * -1;
		q[2][3] = q[2][3] * -1;
		for (i = 0; i < 4; i++) { // Row.
			// First 3 columns of the current row.
			for (j = 0; j < 3; j++) { // Column.
				o_projection[i + j * 4] = q[i][0] * trans[0][j] + q[i][1] * trans[1][j] + q[i][2] * trans[2][j];
			}
			// Fourth column of the current row.
			o_projection[i + 3 * 4] = q[i][0] * trans[0][3] + q[i][1] * trans[1][3] + q[i][2] * trans[2][3] + q[i][3];
		}
		return;	
	}
	private void updateTransmat(NyARTransMatResult i_src)
	{
		this.transmat[0 + 0 * 4] = i_src.m00; 
		this.transmat[0 + 1 * 4] = i_src.m01;
		this.transmat[0 + 2 * 4] = i_src.m02;
		this.transmat[0 + 3 * 4] = i_src.m03;
		this.transmat[1 + 0 * 4] = -i_src.m10;
		this.transmat[1 + 1 * 4] = -i_src.m11;
		this.transmat[1 + 2 * 4] = -i_src.m12;
		this.transmat[1 + 3 * 4] = -i_src.m13;
		this.transmat[2 + 0 * 4] = -i_src.m20;
		this.transmat[2 + 1 * 4] = -i_src.m21;
		this.transmat[2 + 2 * 4] = -i_src.m22;
		this.transmat[2 + 3 * 4] = -i_src.m23;
		this.transmat[3 + 0 * 4] = 0.0;
		this.transmat[3 + 1 * 4] = 0.0;
		this.transmat[3 + 2 * 4] = 0.0;
		this.transmat[3 + 3 * 4] = 1.0;
		//angle
		this.angle.x=(float)i_src.angle.x;
		this.angle.y=(float)i_src.angle.y;
		this.angle.z=(float)i_src.angle.z;
		//trans
		this.trans.x=(float)i_src.m03;
		this.trans.y=(float)i_src.m13;
		this.trans.z=(float)i_src.m23;
		
		return;	
	}
	
	private final static double view_distance_min = 100;//#define VIEW_DISTANCE_MIN		0.1			// Objects closer to the camera than this will not be displayed.
	private final static double view_distance_max = 10000.0;//#define VIEW_DISTANCE_MAX		100.0		// Objects further away from the camera than this will not be displayed.
}
