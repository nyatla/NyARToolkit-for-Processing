package jp.nyatla.nyar4psg.utils;

import processing.core.PApplet;

final public class PatchCollection_Psg302 implements PatchCollection
{
	public void setBackgroundOrtho(PApplet i_applet,float i_width,float i_height,float i_near,float i_far)
	{	
		i_applet.ortho(-i_width/2,i_width/2,-i_height/2,i_height/2,i_near,i_far+1);//3/x
	}
}
