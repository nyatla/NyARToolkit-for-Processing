package jp.nyatla.nyar4psg.utils;

import processing.core.PApplet;

final public class PatchCollection_Psg2x implements PatchCollection
{
	public void setBackgroundOrtho(PApplet i_applet,float i_width,float i_height,float i_near,float i_far)
	{
		i_applet.ortho(0,i_width,0,i_height,i_near,i_far+1);
	}
}
