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

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import processing.core.PImage;

/**
 * PImageをラップするRGBラスタです。
 *　ラップした{@link PImage}の{@link PImage#pixels}を参照します。
 * {@link PImage#pixels}との同期は外部で調整してください。
 */
class PImageRaster extends NyARRgbRaster
{
	public final static int BUFFER_TYPE=NyARBufferType.INT1D_X8R8G8B8_32;
	/**
	 * i_imgをラップします。具体的には、i_imgのpixels配列をラップします。
	 * @param i_img
	 * @throws NyARException
	 */
	public PImageRaster(PImage i_img) throws NyARException
	{
		super(i_img.width,i_img.height,BUFFER_TYPE,false);
		this.wrapBuffer(i_img);
		return;
	}	
	public PImageRaster(int i_width, int i_height) throws NyARException
	{
		super(i_width,i_height,BUFFER_TYPE,false);
		return;
	}
	/**
	 * PImageをラップします。
	 * 画像のサイズはコンストラクタに指定したサイズと一致させてください。
	 * @param i_ref_image
	 * @throws NyARException
	 */
	public void wrapBuffer(PImage i_ref_image) throws NyARException
	{
		assert(this._size.isEqualSize(i_ref_image.width,i_ref_image.height));
		super.wrapBuffer(i_ref_image.pixels);
	}
}

