
package cn.faury.android.library.audioplayer;

import java.io.Serializable;

public class AudioInfo implements Serializable
{

	/**
	 * 音频实体
	 */
	private static final long serialVersionUID = 1L;

	public String type;

	public String ver;

	public String goodsId;

	public boolean isLocal;

	public String userId;

	public int height;

	public String mp3CoverUrl; // mp3封面

	public String path; // mp3的路径

	public String subPic1; // 字幕图片路径1
	
	public String subPic2;

	public int subPicHeight; // 字幕图片高度

	public int subPicWidth; // 字幕图片宽度

	public String subtitles; // 字幕

	public String title; // 标题

	public int width;

}
