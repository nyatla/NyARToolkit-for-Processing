package jp.nyatla.nyar4psg;


import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;
import processing.core.PApplet;

/**
 * このクラスは、検出器のコンフィギュレーション定数を保持します。
 * 定義済みコンフィギュレーションを２つ定義します。
 */
public class NyAR4PsgConfig
{
	/**
	 * for Processing version 2.2.1
	 * Processing2.2.1向けの設定です。
	 */
	public final static int PV_221=0x0201;
	/**
	 * for Processing version 3.0.2
	 * Processing3.0.2向けの設定です。
	 */
	public final static int PV_302=0x0301;
	
	public final static int PV_DEFAULT=PV_302;
	
	/**定数値です。ARToolkit互換の姿勢計算アルゴリズムを選択します。
	 * このアルゴリズムは、ARToolkitと完全に同じです。
	 */
	public final static int TM_ARTK  =NyARMarkerSystemConfig.TM_ARTKV2;
	/**定数値です。NyARToolkitの姿勢計算アルゴリズムを選択します。
	 * このアルゴリズムは高速ですが、角度の浅い時にずれが大きくなります。
	 */
	public final static int TM_NYARTK=NyARMarkerSystemConfig.TM_NYARTK;
	/**定数値です。ARToolkitV4に実装されているICPアルゴリズムを選択します。
	 * 通常はこのアルゴリズムを選択してください。
	 */
	public final static int TM_ARTKICP=NyARMarkerSystemConfig.TM_ARTKICP;
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
	/** Patch Version*/
	public final int _ps_patch_version;

	/**
	 * コンストラクタです。
	 * コンフィギュレーション値を格納したインスタンスを作成します。
	 * @param i_cs
	 * 座標系を選択します。
	 * <ul>
	 * <li> {@link #CS_RIGHT_HAND} 右手系の座標系です。OPENGLと互換性があります。
	 * <li> {@link #CS_LEFT_HAND} 左手系の座標系です。Processingと互換性があります。
	 * </ul>
	 * @param i_tm
	 * 姿勢計算アルゴリズムを選択します。
	 * <ul>
	 * <li> {@link #TM_NYARTK} - NyARToolKitの姿勢推定を使用します。
	 * <li> {@link #TM_ARTK} - ARToolKitの姿勢推定を使用します。
	 * <li> {@link #TM_ARTKICP} - ARToolKitV4の姿勢推定を使用します。
	 * </ul>
	 * @param i_ps_version_id
	 * Processingのバージョンを指定します。
	 * <ul>
	 * <li> {@link #PV_221} - Processing 2.2.1向けの設定です。
	 * <li> {@link #PV_302} - Processing 3.0.2向けの設定です。Processing3.xで使用できます。
	 * </ul>
	 */
	public NyAR4PsgConfig(int i_cs,int i_tm,int i_ps_version_id)
	{
		switch(i_cs){
		case CS_LEFT_HAND:
		case CS_RIGHT_HAND:
			break;
		default:
			PApplet.println("Invalid CS param. select CS_LEFT_HAND or CS_RIGHT_HAND.");
		}
		switch(i_tm){
		case TM_ARTKICP:
		case TM_NYARTK:
		case TM_ARTK:
			break;
		default:
			PApplet.println("Invalid TM param. select TM_NYARTK or TM_ARTK　or TM_ARTKICP.");
		}
		this._coordinate_system=i_cs;
		this.env_transmat_mode=i_tm;
		this._ps_patch_version=i_ps_version_id;
	}
	public NyAR4PsgConfig(int i_cs,int i_tm)
	{
		this(i_cs,i_tm,PV_DEFAULT);
	}
	/**
	 * 定数値です。
	 * Processingと互換性のあるコンフィギュレーションです。
	 * パラメータは、座標系=左手系、姿勢推定アルゴリズム={@link #TM_ARTKICP}です。
	 */
	public static final NyAR4PsgConfig CONFIG_PSG=new NyAR4PsgConfig(CS_LEFT_HAND,TM_ARTKICP);
	/**
	 * Peocessing v2.2.1用の定数値です。
	 * Processingと互換性のあるコンフィギュレーションです。
	 * パラメータは、座標系=左手系、姿勢推定アルゴリズム={@link #TM_ARTKICP}です。
	 */
	public static final NyAR4PsgConfig CONFIG_PSG_PV221=new NyAR4PsgConfig(CS_LEFT_HAND,TM_ARTKICP,PV_221);
	/**
	 * 定数値です。
	 * nyar4psg/0.2.xのCS_LEFTと互換性があります。（nyar4psg/0.2.xのCS_RIGHTと互換性のある値はありません。）
	 * パラメータは、座標系=右手系、姿勢推定アルゴリズム={@link #TM_ARTKICP}です。
	 */
	public static final NyAR4PsgConfig CONFIG_OLD=new NyAR4PsgConfig(CS_RIGHT_HAND,TM_ARTKICP);	
}
