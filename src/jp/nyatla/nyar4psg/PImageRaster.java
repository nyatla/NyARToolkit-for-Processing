package jp.nyatla.nyar4psg;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import processing.core.PImage;

/**
 * PImageをラップするRGBラスタです。
 */
class PImageRaster extends NyARRgbRaster
{
	public final static int BUFFER_TYPE=NyARBufferType.INT1D_X8R8G8B8_32;
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

