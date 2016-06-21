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
	/**
	 * この関数は、{@link NyARDoubleMatrix44}の内容をダンプします。
	 * @param i_mat
	 * ダンプするオブジェクト。
	 */
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
	
	/**
	 * NyARToolKitのMatrixをProcessing形式に変換します。
	 * @param i_src
	 * 変換元のオブジェクト
	 * @param i_dst
	 * 変換先のオブジェクト
	 * @return
	 * i_dstをそのまま返します。
	 */
	public static PMatrix3D nyarMat2PsMat(NyARDoubleMatrix44 i_src,PMatrix3D i_dst)
	{
		i_dst.m00=(float)(i_src.m00);
		i_dst.m01=(float)(i_src.m01);
		i_dst.m02=(float)(i_src.m02);
		i_dst.m03=(float)(i_src.m03);
		i_dst.m10=(float)(i_src.m10);
		i_dst.m11=(float)(i_src.m11);
		i_dst.m12=(float)(i_src.m12);
		i_dst.m13=(float)(i_src.m13);
		i_dst.m20=(float)(i_src.m20);
		i_dst.m21=(float)(i_src.m21);
		i_dst.m22=(float)(i_src.m22);
		i_dst.m23=(float)(i_src.m23);
		i_dst.m30=(float)(i_src.m30);
		i_dst.m31=(float)(i_src.m31);
		i_dst.m32=(float)(i_src.m32);
		i_dst.m33=(float)(i_src.m33);
		return i_dst;
	}
	/**
	 * PMatrix3Dをfloat配列に変換します。
	 * @param i_in
	 * 変換元のオブジェクト
	 * @param o_out
	 * 変換先のオブジェクト
	 * @return
	 * o_outをそのまま返します。
	 */	
	protected static float[] pMatrix2GLMatrix(PMatrix3D i_in,float[] o_out)
	{
		o_out[ 0]=i_in.m00;
		o_out[ 1]=i_in.m10;
		o_out[ 2]=i_in.m20;
		o_out[ 3]=i_in.m30;
		o_out[ 4]=i_in.m01;
		o_out[ 5]=i_in.m11;
		o_out[ 6]=i_in.m21;
		o_out[ 7]=i_in.m31;
		o_out[ 8]=i_in.m02;
		o_out[ 9]=i_in.m12;
		o_out[10]=i_in.m22;
		o_out[11]=i_in.m32;
		o_out[12]=i_in.m03;
		o_out[13]=i_in.m13;
		o_out[14]=i_in.m23;
		o_out[15]=i_in.m33;	
		return o_out;
	}
	/**
	 * PMatrix3Dをdouble配列に変換します。
	 * @param i_in
	 * 変換元のオブジェクト
	 * @param o_out
	 * 変換先のオブジェクト
	 * @return
	 * o_outをそのまま返します。
	 */	
	protected static double[] pMatrix2GLMatrix(PMatrix3D i_in,double[] o_out)
	{
		o_out[ 0]=i_in.m00;
		o_out[ 1]=i_in.m10;
		o_out[ 2]=i_in.m20;
		o_out[ 3]=i_in.m30;
		o_out[ 4]=i_in.m01;
		o_out[ 5]=i_in.m11;
		o_out[ 6]=i_in.m21;
		o_out[ 7]=i_in.m31;
		o_out[ 8]=i_in.m02;
		o_out[ 9]=i_in.m12;
		o_out[10]=i_in.m22;
		o_out[11]=i_in.m32;
		o_out[12]=i_in.m03;
		o_out[13]=i_in.m13;
		o_out[14]=i_in.m23;
		o_out[15]=i_in.m33;
		return o_out;
	}	
}