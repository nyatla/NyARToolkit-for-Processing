package jp.nyatla.nyar4psg;

import jp.nyatla.nyartoolkit.core.NyARVersion;

public class Version {
	public final static String MODULE = "NyAR4psg";
	public final static int MAJOR = 3;
	public final static int MINER = 0;
	public final static int PATCH = 10;
	public final static String STRING=String.format("%s/%d.%d.%d;%d",Version.MODULE,Version.MAJOR,Version.MINER,Version.PATCH,NyARVersion.VERSION_STRING);
}
