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