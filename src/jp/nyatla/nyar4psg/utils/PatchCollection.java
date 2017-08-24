package jp.nyatla.nyar4psg.utils;

import processing.core.PApplet;

/**
 * Processingのバージョン毎の差異を吸収するための関数群を定義します。
 *
 */
public interface PatchCollection
{
	public void setBackgroundOrtho(PApplet i_applet,float i_width,float i_height,float i_near,float i_far);
}
