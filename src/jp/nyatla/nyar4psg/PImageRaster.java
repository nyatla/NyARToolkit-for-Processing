package jp.nyatla.nyar4psg;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import processing.core.PImage;

class PImageRaster extends NyARRgbRaster
{
	public PImageRaster(int i_width, int i_height) throws NyARException
	{
		super(i_width,i_height,NyARBufferType.INT1D_X8R8G8B8_32,false);
		return;
	}
	public void wrapBuffer(PImage i_ref_image) throws NyARException
	{
		super.wrapBuffer(i_ref_image.pixels);
	}
}

