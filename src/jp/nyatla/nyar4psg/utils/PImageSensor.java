package jp.nyatla.nyar4psg.utils;

import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
import processing.core.PImage;

public class PImageSensor extends NyARSensor
{
	private PImageRaster _src;
	public PImageSensor(NyARIntSize i_size)
	{
		super(i_size);
	}
	public void update(PImage i_img)
	{
		this._src=new PImageRaster(i_img);
		super.update(this._src);
	}	
}
