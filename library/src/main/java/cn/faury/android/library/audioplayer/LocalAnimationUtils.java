
package cn.faury.android.library.audioplayer;

import android.content.Context;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

public class LocalAnimationUtils
{

	/**
	 * 动画模板
	 * 
	 * @param animId
	 * @param listener
	 * @return
	 */
	public static TranslateAnimation getAnimation(Context context , int animId, AnimationListener listener)
	{

		TranslateAnimation animation = (TranslateAnimation) AnimationUtils.loadAnimation(context, animId);
		animation.setAnimationListener(listener);
		return animation;
	}


	/**
	 * 音频的光盘旋转动画
	 * @return
	 */
	public static RotateAnimation getAudioDiskAnimation(Context context)
	{

		return (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.anim_audio_disk_rotate);
	}
}
