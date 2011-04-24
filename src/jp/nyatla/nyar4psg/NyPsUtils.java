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

import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import processing.core.PApplet;
import processing.core.PMatrix3D;

/**
 * このクラスは、Processingに関わるユーティリティ関数を実装します。
 */
public class NyPsUtils
{
	/**
	 * この関数は、{@link PMatrix3D}の内容をダンプします。
	 * @param i_mat
	 * ダンプするオブジェクト。
	 */
	public static void dumpObject(PMatrix3D i_mat)
	{
		PApplet.println("PMatrix3D");
		PApplet.println(String.format("%f %f %f %f",i_mat.m00,i_mat.m01,i_mat.m02,i_mat.m03));
		PApplet.println(String.format("%f %f %f %f",i_mat.m10,i_mat.m11,i_mat.m12,i_mat.m13));
		PApplet.println(String.format("%f %f %f %f",i_mat.m20,i_mat.m21,i_mat.m22,i_mat.m23));
		PApplet.println(String.format("%f %f %f %f",i_mat.m30,i_mat.m31,i_mat.m32,i_mat.m33));
	}
	public static void dumpObject(NyARDoubleMatrix44 i_mat)
	{
		PApplet.println("NyARDoubleMatrix44");
		PApplet.println(String.format("%f %f %f %f",i_mat.m00,i_mat.m01,i_mat.m02,i_mat.m03));
		PApplet.println(String.format("%f %f %f %f",i_mat.m10,i_mat.m11,i_mat.m12,i_mat.m13));
		PApplet.println(String.format("%f %f %f %f",i_mat.m20,i_mat.m21,i_mat.m22,i_mat.m23));
		PApplet.println(String.format("%f %f %f %f",i_mat.m30,i_mat.m31,i_mat.m32,i_mat.m33));
	}	
	/**
	 * この関数は、double配列の内容をダンプします。
	 * @param i_val
	 * ダンプするオブジェクト。
	 */
	public static void dumpObject(double[] i_val)
	{
		PApplet.println("double[]");
		for(int i=0;i<i_val.length;i++){
			PApplet.println(i_val[i]);
		}
	}
}