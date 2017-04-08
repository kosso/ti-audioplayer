package com.kosso.audioplayerandroid;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.TiLifecycle.OnLifecycleEvent;

import android.app.Activity;

import java.io.IOException;


@Kroll.proxy(creatableInModule=AudioplayerAndroidModule.class, propertyAccessors={
	TiC.PROPERTY_URL,
	TiC.PROPERTY_VOLUME,
	TiC.PROPERTY_TIME,
	TiC.PROPERTY_DURATION
})

public class PlayerProxy extends KrollProxy
implements OnLifecycleEvent 
{
	// Standard Debugging variables
	private static final String LCAT = "PlayerProxy";
	
	@Kroll.constant public static final int STATE_BUFFERING = MediaPlayerWrapper.STATE_BUFFERING;
	@Kroll.constant public static final int STATE_INITIALIZED = MediaPlayerWrapper.STATE_INITIALIZED;
	@Kroll.constant public static final int STATE_PAUSED = MediaPlayerWrapper.STATE_PAUSED;
	@Kroll.constant public static final int STATE_PLAYING = MediaPlayerWrapper.STATE_PLAYING;
	@Kroll.constant public static final int STATE_STARTING = MediaPlayerWrapper.STATE_STARTING;
	@Kroll.constant public static final int STATE_STOPPED = MediaPlayerWrapper.STATE_STOPPED;
	@Kroll.constant public static final int STATE_STOPPING = MediaPlayerWrapper.STATE_STOPPING;
	@Kroll.constant public static final int STATE_WAITING_FOR_DATA = MediaPlayerWrapper.STATE_WAITING_FOR_DATA;
	@Kroll.constant public static final int STATE_WAITING_FOR_QUEUE = MediaPlayerWrapper.STATE_WAITING_FOR_QUEUE;
	
	protected MediaPlayerWrapper snd;
	public String url = "";
	
	// Constructor
	public PlayerProxy()
	{
		super();		
		defaultValues.put(TiC.PROPERTY_VOLUME, 1.0f);
		defaultValues.put(TiC.PROPERTY_TIME, 0);
		defaultValues.put(TiC.PROPERTY_ALLOW_BACKGROUND, true);
	}
	
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);

		if (options.containsKey(TiC.PROPERTY_URL)) {
			Log.d(LCAT, "URL PRESENT ON CREATION");
			setProperty(TiC.PROPERTY_URL, resolveUrl(null, TiConvert.toString(options, TiC.PROPERTY_URL))); // options.get(TiC.PROPERTY_ALLOW_BACKGROUND)
		}
		if (options.containsKey(TiC.PROPERTY_ALLOW_BACKGROUND)) {
			setProperty(TiC.PROPERTY_ALLOW_BACKGROUND, options.get(TiC.PROPERTY_ALLOW_BACKGROUND));
		}
	}

	@Kroll.getProperty 
	@Kroll.method 
	public String getUrl() {
		return TiConvert.toString(getProperty(TiC.PROPERTY_URL));
	}

	//@Kroll.setProperty 
	//@Kroll.method 
	//public void setUrl(KrollInvocation kroll, String url) {
	//	if (url != null) {

	//		Log.d(LCAT, "setUrl KrollInvocation");
			//Log.d(LCAT, "setupUrl : url: " + TiConvert.toString(getProperty("url")));
			//setProperty(TiC.PROPERTY_TIME, 0);
			//setProperty(TiC.PROPERTY_DURATION, 0);
			//setProperty("url", null);
			//release();
			//getSound();
			//setupUrl(url);
	//	}
	//}


	@Kroll.method
	public void setUrl(String url) {
		if (url != null) {
			// Log.d(LCAT, "setupUrl : url: " + TiConvert.toString(getProperty("url")));
			setProperty(TiC.PROPERTY_TIME, 0);
			setProperty(TiC.PROPERTY_DURATION, 0);
			setProperty(TiC.PROPERTY_URL, resolveUrl(null, url));			
			release();
			getSound();
		}
	}

	@Kroll.setProperty @Kroll.method
	public void setVolume(Float volume) {
		if (volume!=null) {
			// Log.d(LCAT, "setVolume : " + TiConvert.toString(volume));
			setProperty(TiC.PROPERTY_VOLUME, volume);
			MediaPlayerWrapper s = getSound();
			if (s != null) {
				s.setVolume(TiConvert.toFloat(volume));
			}
		}
	}

	
	@Kroll.getProperty @Kroll.method
	public boolean isStreaming() {
		MediaPlayerWrapper s = getSound();
		if (s != null) {
			return s.isStreaming();
		}
		return false;
	}

	@Kroll.getProperty @Kroll.method
	public boolean isPlaying() {
		MediaPlayerWrapper s = getSound();
		if (s != null) {
			return s.isPlaying();
		}
		return false;
	}

	@Kroll.getProperty @Kroll.method
	public boolean isPaused() {
		MediaPlayerWrapper s = getSound();
		if (s != null) {
			return s.isPaused();
		}
		return false;
	}

	@Kroll.getProperty @Kroll.method
	public int getDuration() {
		MediaPlayerWrapper s = getSound();
		if (s != null) {
			if(s.isReady()){
				return s.getDuration();
			} else {
				Log.d(LCAT, "DURATION NOT READY YET");
			}
		}
		return 0;
	}

	@Kroll.getProperty @Kroll.method
	public KrollDict getMetadata() {				
		KrollDict metadata = (KrollDict) getProperty("metadata");
		return metadata;
	}
	
	@Kroll.method
	public void start() { // iOS parity method
		play();
	}

	@Kroll.method
	public void play() {
		MediaPlayerWrapper s = getSound();
		if (s != null) {
			s.play();
		} else {
			Log.d(LCAT, "play : media player sound is null!!");
		}
	}

	@Kroll.method
	public void pause() {
		MediaPlayerWrapper s = getSound();
		if (s != null) {
			s.pause();
		}
	}

	@Kroll.method
	public void release() {
		if (snd != null) {
			MediaPlayerWrapper s = getSound();
			if (s != null) {
				s.release();
				snd = null;
			}
		}
	}


	@Kroll.method
	public void destroy() {
		release();
	}

	@Kroll.method
	public void stop() {
		MediaPlayerWrapper s = getSound();
		if (s != null) {
			s.stop();
		}
	}

	@Kroll.method
	public void seek(int position) {
		MediaPlayerWrapper s = getSound();
		if (s != null) {
			Log.d(LCAT, "SEEK!!");

			s.setTime(position);
		}		
	}

	protected MediaPlayerWrapper getSound()
	{
		if (snd == null) {
			try {
				snd = new MediaPlayerWrapper(this);

				// Log.d(LCAT, "getSound!");

			} catch (IOException e) {
				e.printStackTrace();
			}
			setModelListener(snd);
		}
		return snd;
	}

	private boolean allowBackground() {
		boolean allow = false;
		if (hasProperty(TiC.PROPERTY_ALLOW_BACKGROUND)) {
			allow = TiConvert.toBoolean(getProperty(TiC.PROPERTY_ALLOW_BACKGROUND));
		}
		return allow;
	}

	
	@Override
	public void onStart(Activity activity) {
	}
	
	@Override
	public void onResume(Activity activity) {
		if (!allowBackground()) {
			if (snd != null) {
				snd.onResume();
			}
		}
	}
	
	@Override
	public void onPause(Activity activity) {
		if (!allowBackground()) {
			if (snd != null) {
				snd.onPause();
			}
		}
	}

	@Override
	public void onStop(Activity activity) {
	}
	
	@Override
	public void onDestroy(Activity activity) {
		if (snd != null) {
			snd.onDestroy();
		}
		snd = null;
	}

	@Override
	public String getApiName()
	{
		return "com.kosso.audioplayerandroid";
	}
	
	
}