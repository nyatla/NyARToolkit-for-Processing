package jp.nyatla.nyar4psg;

import processing.core.PApplet;

/**
 * このクラスは、コンフィギュレーション定数を保持します。
 * また、2つの定義済みコンフィギュレーションを定義します。
 */
public class NyAR4PsgConfig
{
	/**定数値です。ARToolkit互換の姿勢計算アルゴリズムを選択します。
	 * このアルゴリズムは、ARToolkitと完全に同じです。
	 */
	public final static int TM_ARTK  =0;
	/**定数値です。NyARToolkitの姿勢計算アルゴリズムを選択します。
	 * このアルゴリズムは高速ですが、角度の浅い時にずれが大きくなります。
	 */
	public final static int TM_NYARTK=1;
	/**
	 * 定数値です。RightHand系の座標を選択します。
	 * RightHand座標系は、ARToolKitと互換性のある座標系ですが、Processingの座標系と互換性がないため、text等の出力が鏡像になります。
	 */
	public final static int CS_RIGHT_HAND=0;
	/**
	 * 定数値です。LeftHand座標系を構築します。
	 * RightHand座標系は、ARToolKitと互換性のない座標系ですが、Processingの座標系と互換性があります。
	 * processing関数で描画する場合は、こちらを選択してください。
	 */
	public final static int CS_LEFT_HAND =1;
	
	/**　この値は、姿勢計算アルゴリズムの選択値です。*/
	public final int env_transmat_mode;
	/**　この値は、座標系の選択値です。*/
	public final int _coordinate_system;

	/**
	 * コンストラクタです。
	 * コンフィギュレーション値を格納したインスタンスを作成します。
	 * @param i_cs
	 * 座標系を選択します。
	 * <ul>
	 * <li> {@link #CS_RIGHT_HAND} 
	 * <li> {@link #CS_LEFT_HAND}
	 * </ul>
	 * @param i_tm
	 * 姿勢計算アルゴリズムを選択します。
	 * <ul>
	 * <li> {@link #TM_NYARTK} - NyARToolKitの姿勢推定を使用します。
	 * <li> {@link #TM_ARTK} - ARToolKitの姿勢推定を使用します。
	 * </ul>
	 */
	public NyAR4PsgConfig(int i_cs,int i_tm)
	{
		switch(i_cs){
		case CS_LEFT_HAND:
		case CS_RIGHT_HAND:
			break;
		default:
			PApplet.println("Invalid CS param. select CS_LEFT_HAND or CS_RIGHT_HAND.");
		}
		switch(i_tm){
		case TM_NYARTK:
		case TM_ARTK:
			break;
		default:
			PApplet.println("Invalid TM param. select TM_NYARTK or TM_ARTK.");
		}
		this._coordinate_system=i_cs;
		this.env_transmat_mode=i_tm;
	}
	/**
	 * 定数値です。
	 * Processingと互換性のあるコンフィギュレーションです。
	 * パラメータは、座標系=左手系、姿勢推定アルゴリズム=NyARToolkitです。
	 */
	public static final NyAR4PsgConfig CONFIG_PSG=new NyAR4PsgConfig(CS_LEFT_HAND,TM_NYARTK);
	/**
	 * 定数値です。
	 * システムのデフォルトコンフィギュレーションです。
	 * nyar4psg/0.2.xのCS_LEFTと互換性があります。（nyar4psg/0.2.xのCS_RIGHTと互換性のある値はありません。）
	 * パラメータは、座標系=右手系、姿勢推定アルゴリズム=NyARToolkitです。
	 */
	public static final NyAR4PsgConfig CONFIG_DEFAULT=new NyAR4PsgConfig(CS_RIGHT_HAND,TM_NYARTK);
}
