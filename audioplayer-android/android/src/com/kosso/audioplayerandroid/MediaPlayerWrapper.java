package com.kosso.audioplayerandroid;

// Note : As of API 23 (Android 6.0 Marshallow), MediaPlayer.setPlaybackParams will be available. This also allows for pitch and rate adjustment.

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollPropertyChange;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.KrollProxyListener;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.util.TiConvert;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.webkit.URLUtil;

public class MediaPlayerWrapper
	implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, KrollProxyListener,
	MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener
{
	private static final String LCAT = "AudioplayerAndroid";
	
	public static final String PROPERTY_VOLUME = "volume";
	public static final String PROPERTY_URL = "url";
	public static final String PROPERTY_TIME = "time";
	public static final String PROPERTY_DURATION = "duration";
	
	public static final int STATE_BUFFERING	= 0;	// current playback is in the buffering from the network state
	public static final int STATE_INITIALIZED = 1;	// current playback is in the initialization state
	public static final int STATE_PAUSED = 2;	// current playback is in the paused state
	public static final int STATE_PLAYING = 3;	// current playback is in the playing state
	public static final int STATE_STARTING = 4;	// current playback is in the starting playback state
	public static final int STATE_STOPPED = 5; // current playback is in the stopped state
	public static final int STATE_STOPPING = 6; // current playback is in the stopping state
	public static final int STATE_WAITING_FOR_DATA = 7;  // current playback is in the waiting for audio data from the network state
	public static final int STATE_WAITING_FOR_QUEUE	= 8; //	current playback is in the waiting for audio data to fill the queue state

	public static final String STATE_BUFFERING_DESC = "buffering";	// current playback is in the buffering from the network state
	public static final String STATE_INITIALIZED_DESC = "initialized";	// current playback is in the initialization state
	public static final String STATE_PAUSED_DESC = "paused";	// current playback is in the paused state
	public static final String STATE_PLAYING_DESC = "playing";	// current playback is in the playing state
	public static final String STATE_STARTING_DESC = "starting";	// current playback is in the starting playback state
	public static final String STATE_STOPPED_DESC = "stopped"; // current playback is in the stopped state
	public static final String STATE_STOPPING_DESC = "stopping"; // current playback is in the stopping state
	public static final String STATE_WAITING_FOR_DATA_DESC = "waiting for data";  // current playback is in the waiting for audio data from the network state
	public static final String STATE_WAITING_FOR_QUEUE_DESC = "waiting for queue"; //	current playback is in the waiting for audio data to fill the queue state

	public static final String EVENT_COMPLETE = "complete";
	public static final String EVENT_ERROR = "error";
	public static final String EVENT_CHANGE = "change";
	public static final String EVENT_PROGRESS = "progress";
	public static final String EVENT_BUFFERING = "buffering";
	public static final String EVENT_METADATA = "metadata";
	public static final String EVENT_SEEK_COMPLETE = "seekcomplete";
	public static final String EVENT_PLAYER_STATUS_CHANGE = "playerstatuschange"; // to be compat with TiAVAudioPlayer iOS module 0:unknown ,1:ready to play,2: failed 
	public static final String EVENT_DURATION_CHANGE = "durationchange"; // to be compat with TiAVAudioPlayer iOS module 0:unknown ,1:ready to play,2: failed 

	public static final int STATUS_UNKNOWN = 0;			// current player status
	public static final int STATUS_READY_TO_PLAY = 1;	// current player status	
	public static final int STATUS_FAILED = 2;			// current player status
	
	public static final String EVENT_COMPLETE_JSON = "{ type : '" + EVENT_COMPLETE + "' }";

	private boolean streaming = false; // for live streams
	private boolean paused = false;
	private boolean looping = false;
	
	private boolean meta = false;
	private String _url = "";

	protected KrollProxy proxy;
	protected MediaPlayer mp;
	protected float volume;
	protected boolean playOnResume;
	protected boolean remote;
	protected Timer progressTimer;
	
	private static Context mContext;

	private boolean readyToPlay = false;

	public MediaPlayerWrapper(KrollProxy proxy) throws IOException
	{
		this.proxy = proxy;
		this.playOnResume = false;
		this.remote = false;
		
		mContext = TiApplication.getInstance();
		
		String url = TiConvert.toString(proxy.getProperty(TiC.PROPERTY_URL));
		if (url != null && url.length() > 0)
		{
			_url = url;
			this.initialize();			
		}
	}

	protected void initialize()
		throws IOException
	{
		try {

			readyToPlay = true;
			streaming = false;
			paused = false;
			looping = false;
			meta = false;
			
			setState(STATE_STARTING);
			// See: https://developer.android.com/reference/android/media/MediaPlayer.html
			mp = new MediaPlayer();
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			String url = TiConvert.toString(proxy.getProperty(TiC.PROPERTY_URL));
			if (URLUtil.isAssetUrl(url)) {
				String path = url.substring(TiConvert.ASSET_URL.length());
				AssetFileDescriptor afd = null;
				try {
					afd = mContext.getAssets().openFd(path);
					// Why mp.setDataSource(afd) doesn't work is a problem for another day.
					// http://groups.google.com/group/android-developers/browse_thread/thread/225c4c150be92416
					mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				} catch (IOException e) {
					Log.e(LCAT, "Error setting file descriptor: ", e);
				} finally {
					if (afd != null) {
						afd.close();
					}
				}
			} else {
				Uri uri = Uri.parse(url);
				if (uri.getScheme().equals("file")) {
					mp.setDataSource(uri.getPath());
				} else {
					remote = true;
					mp.setDataSource(url);
				}
			}

			mp.setLooping(looping);
			mp.setOnCompletionListener(this);
			mp.setOnErrorListener(this);
			mp.setOnInfoListener(this);
			mp.setOnBufferingUpdateListener(this);
			mp.setOnSeekCompleteListener(this);

			if(remote){ // try async
				// Log.d(LCAT,"remote. use asyncPrepare.. ");
				mp.setOnPreparedListener(this);
				mp.prepareAsync();
			} else {
				// Log.d(LCAT,"not remote. use prepare.. "); 
				mp.setOnPreparedListener(this);
				mp.prepare(); // Probably need to allow for Async

				//setState(STATE_INITIALIZED);
				//setVolume(volume);
				//if (proxy.hasProperty(TiC.PROPERTY_VOLUME)) {
				//	setVolume(TiConvert.toInt(proxy.getProperty(TiC.PROPERTY_VOLUME)));
				//}
				//if (proxy.hasProperty(TiC.PROPERTY_TIME)) {
				//	setTime(TiConvert.toInt(proxy.getProperty(TiC.PROPERTY_TIME)));
				//}

			}

		} catch (Throwable t) {
			Log.w(LCAT, "Issue while initializing : " , t);
			release();
			setState(STATE_STOPPED);
		}
	}

	public boolean isLooping()
	{
		return looping;
	}
	
	public boolean isStreaming()
	{
		return streaming;
	}

	public boolean isPaused()
	{
		return paused;
	}

	public boolean isReady()
	{
		return readyToPlay;
	}

	public boolean isPlaying()
	{
		boolean result = false;
		if (mp != null) {
			result = mp.isPlaying();
		}
		return result;
	}

	public void pause()
	{
		try {
			if (mp != null) {
				if(mp.isPlaying()) {
					//if (remote) {
						stopProgressTimer();
					//}
					mp.pause();
					paused = true;
					setState(STATE_PAUSED);
				}
			}
		} catch (Throwable t) {
			Log.w(LCAT, "Issue while pausing : " , t);
		}
	}

	public void play()
	{
		try {		
			if (mp != null) {
				if (!isPlaying()) {
					
					mp.start();
					setState(STATE_PLAYING);
					paused = false;
					startProgressTimer();
					return;
				}
				setState(STATE_PLAYING);
			}					

		} catch (Throwable t) {
			Log.w(LCAT, "Issue while playing : " , t);
			reset();
		}
	}

	public void reset()
	{
		try {
			if (mp != null) {
				//if (remote) {
					stopProgressTimer();
				//}

				setState(STATE_STOPPING);
				mp.stop();
				mp.seekTo(0);
				looping = false;
				paused = false;
				streaming = false;
				setState(STATE_STOPPED);
			}
		} catch (Throwable t) {
			Log.w(LCAT, "Issue while resetting : " , t);
		}
	}

	public void release()
	{
		try {
			if (mp != null) {

				mp.setOnCompletionListener(null);
				mp.setOnErrorListener(null);
				mp.setOnBufferingUpdateListener(null);
				mp.setOnInfoListener(null);
				mp.setOnPreparedListener(null);
				mp.setOnSeekCompleteListener(null);

				mp.release();
				mp = null;
				
				remote = false;
			}
		} catch (Throwable t) {
			Log.w(LCAT, "Issue while releasing : " , t);
		}
	}

	public void setLooping(boolean loop)
	{
		try {
			if(loop != looping) {
				if (mp != null) {
					mp.setLooping(loop);
				}
				looping = loop;
			}
		} catch (Throwable t) {
			Log.w(LCAT, "Issue while configuring looping : " , t);
		}
	}

	public void setVolume(float volume)
	{
		try {
			if (volume < 0.0f) {
				this.volume = 0.0f;
				Log.w(LCAT, "Attempt to set volume less than 0.0. Volume set to 0.0");
			} else if (volume > 1.0) {
				this.volume = 1.0f;
				proxy.setProperty(PROPERTY_VOLUME, volume);
				Log.w(LCAT, "Attempt to set volume greater than 1.0. Volume set to 1.0");
			} else {
				this.volume = volume; // Store in 0.0 to 1.0, scale when setting hw
			}
			if (mp != null) {
				float scaledVolume = this.volume;
				mp.setVolume(scaledVolume, scaledVolume);
			}
		} catch (Throwable t) {
			Log.w(LCAT, "Issue while setting volume : " , t);
		}
	}

	public int getDuration()
	{
		int duration = 0;
		
		if (mp != null && readyToPlay) {
			duration = mp.getDuration();
		}
		

		return duration;
	}

	public int getTime()
	{
		int time = 0;

		if (mp != null) {
			time = mp.getCurrentPosition();
		} else {
			time = TiConvert.toInt(proxy.getProperty(TiC.PROPERTY_TIME));
		}

		return time;
	}

	public void setTime(int position)
	{
		if (position < 0) {
			position = 0;
		}
		Log.d(LCAT, "setTime!" + position);

		if (mp != null && readyToPlay) {
			
			if(mp.isPlaying()){
				Log.d(LCAT, "PAUSING since it was playing!!");

				mp.pause();
			}

			int duration = mp.getDuration();
			if (position > duration) {
				Log.d(LCAT, "TRIED TO SEEK LATER THAN DURATION!!");
				// maybe check to see what's buffering? 
				position = duration;
			}
			mp.seekTo(position);
		
		}

		proxy.setProperty(TiC.PROPERTY_TIME, position);
	}

	private void setState(int state)
	{
		proxy.setProperty("state", state);
		String stateDescription = "";

		switch(state) {
			case STATE_BUFFERING :
				stateDescription = STATE_BUFFERING_DESC;
				break;
			case STATE_INITIALIZED :
				stateDescription = STATE_INITIALIZED_DESC;
				break;
			case STATE_PAUSED :
				stateDescription = STATE_PAUSED_DESC;
				break;
			case STATE_PLAYING :
				stateDescription = STATE_PLAYING_DESC;
				break;
			case STATE_STARTING :
				stateDescription = STATE_STARTING_DESC;
				break;
			case STATE_STOPPED :
				stateDescription = STATE_STOPPED_DESC;
				break;
			case STATE_STOPPING :
				stateDescription = STATE_STOPPING_DESC;
				break;
			case STATE_WAITING_FOR_DATA :
				stateDescription = STATE_WAITING_FOR_DATA_DESC;
				break;
			case STATE_WAITING_FOR_QUEUE :
				stateDescription = STATE_WAITING_FOR_QUEUE_DESC;
				break;
		}

		proxy.setProperty("stateDescription", stateDescription);
		KrollDict data = new KrollDict();
		data.put("state", state);
		data.put("description", stateDescription);
		proxy.fireEvent(EVENT_CHANGE, data);
	}

	private void setDuration(int duration)
	{
		if(remote && duration == 0){
			streaming = true;
		}
		proxy.setProperty(PROPERTY_DURATION, duration);
		int time = 0;
		KrollDict data = new KrollDict();
		data.put("time", time);		
		data.put("duration", duration);
		proxy.fireEvent(EVENT_DURATION_CHANGE, data);

	}

	private void setStatus(int status)
	{
		proxy.setProperty("status", status);
		// Log.d(LCAT, "Player status changed : " + status);
		int duration = 0;
		if(readyToPlay){
			duration = mp.getDuration();
			if(remote && duration == 0){
				streaming = true;
			}
		}
		KrollDict data = new KrollDict();
		data.put("duration", duration);
		data.put("status", status);
		proxy.fireEvent(EVENT_PLAYER_STATUS_CHANGE, data);

	}

	public void stop()
	{
		try {
			if (mp != null) {
				stopProgressTimer();
				if (mp.isPlaying() || isPaused()) {
					setState(STATE_STOPPING);
					mp.stop();
					setState(STATE_STOPPED);
					mp.setOnPreparedListener(this);

					try {
						if(remote){
							Log.d(LCAT, "re-prepareAsync remote audio after stop.");
							mp.prepareAsync();
						} else {
							Log.d(LCAT, "re-prepare local audio after stop.");
							mp.prepare();
						}
					} catch (IOException e) {
						Log.e(LCAT,"IOException Error while preparing audio after stop(). Ignoring.");
					} catch (IllegalStateException e) {
						Log.w(LCAT, "IllegalStateException Error while preparing audio after stop(). Ignoring.");
					}
				}

				if(isPaused()) {
					paused = false;
				}
			}
		} catch (Throwable t) {
			Log.e(LCAT, "Error in stop() : " , t);
		}
	}

	
	public KrollDict extractMetaData(String url) throws IOException{
		// Get ID3 tags : title, artist
		final MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
		if(remote){
			metaRetriever.setDataSource(url, new HashMap<String, String>());			
		} else {
			String path = url.substring(TiConvert.ASSET_URL.length());
			AssetFileDescriptor afd = null;
			try {
				afd = mContext.getAssets().openFd(path);
				metaRetriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			} catch (IOException e) {
				Log.e(LCAT, "Error setting file descriptor: ", e);
			} finally {
				if (afd != null) {
					afd.close();
				}
			}
		}
		
		KrollDict metadata = new KrollDict();		
		if(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)!=null){ 
			metadata.put("title", metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
		}
		if(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!=null){ 
			metadata.put("artist", metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
		}
		
		if(metadata.isEmpty() ){
			metadata.put("none", null);
		}
		
		return metadata;
	}
	
	public void onCompletion(MediaPlayer mp)
	{
		proxy.fireEvent(EVENT_COMPLETE, null);
		stop();
	}

	public boolean onInfo(MediaPlayer mp, int what, int extra)
	{
		String msg = "OnInfo Unknown media issue.";
		switch(what) {
			case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING :
				msg = "Stream not interleaved or interleaved improperly.";
				break;
			case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE :
				msg = "Stream does not support seeking";
				break;
			case MediaPlayer.MEDIA_INFO_UNKNOWN :
				msg = "Unknown media issue";
				break;
			case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING :
				msg = "Video is too complex for decoder, video lagging."; // shouldn't occur, but covering bases.
				break;
			case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
				msg = "Metadata update.";
				break;
		}
		Log.d(LCAT, "Error " + msg);

		KrollDict data = new KrollDict();
		data.put(TiC.PROPERTY_CODE, 0);
		data.put(TiC.PROPERTY_MESSAGE, msg);
		proxy.fireEvent(EVENT_ERROR, data);
		return true;
	}

	public boolean onError(MediaPlayer mp, int what, int extra)
	{
		int code = 0;
		String msg = "Unknown media error.";
		if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			msg = "Media server died";
		}
		release();

		KrollDict data = new KrollDict();
		data.put(TiC.PROPERTY_CODE, code);
		data.put(TiC.PROPERTY_MESSAGE, msg);
		proxy.fireEvent(EVENT_ERROR, data);

		return true;
	}

	public void onBufferingUpdate(MediaPlayer mp, int percent)
	{
		KrollDict data = new KrollDict();
		data.put("percent", percent);
		proxy.fireEvent(EVENT_BUFFERING, data);
		if(percent==100 || percent < 0){
			mp.setOnBufferingUpdateListener(null);
		}
	}

	private void startProgressTimer()
	{
		if (progressTimer == null) {
			progressTimer = new Timer(true);
		} else {
			progressTimer.cancel();
			progressTimer = new Timer(true);
		}

		progressTimer.schedule(new TimerTask()
		{
			@Override
			public void run() {				
				if (mp != null && mp.isPlaying()) {
					int position = mp.getCurrentPosition();
					int duration = mp.getDuration();
					if(position > duration){
						// Looks like a live stream! 
						Log.d(LCAT, "STREAMING.  stop timer");
						streaming = true;
						stopProgressTimer();
					} else {
						proxy.setProperty(TiC.PROPERTY_TIME, position);
						KrollDict event = new KrollDict();
						event.put("progress", position);
						proxy.fireEvent(EVENT_PROGRESS, event);
						
//						KrollDict metadata = null;
//						if(!meta){
//							meta = true;
//							try {
//								metadata = extractMetaData(_url);
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//							if(metadata.isEmpty() ){
//								metadata.put("none", null);
//							}
//							proxy.fireEvent(EVENT_METADATA, metadata);
//							proxy.setProperty("metadata", metadata);
//						}
					}
				}
			}
		}, 1000, 1000);
	}

	private void stopProgressTimer()
	{
		if (progressTimer != null) {
			progressTimer.cancel();
			progressTimer.purge();
			progressTimer = null;
		}
	}

	protected void onDestroy()
	{
		if (mp != null) {
			stopProgressTimer();
			mp.release();
		}
	}

	protected void onPause()
	{
		if (mp != null) {
			if (isPlaying()) {
				pause();
				playOnResume = true;
			}
		}
	}

	protected void onResume()
	{
		if (mp != null) {
			if (playOnResume) {
				play();
				playOnResume = false;
			}
		}
	}

	public void listenerAdded(String type, int count, KrollProxy proxy) { }

	public void listenerRemoved(String type, int count, KrollProxy proxy) { }

	public void processProperties(KrollDict d)
	{
		if (d.containsKey(PROPERTY_VOLUME)) {
			setVolume(TiConvert.toFloat(d, PROPERTY_VOLUME));
		} else {
		//	setVolume(0.5f);
		}
		if (d.containsKey(TiC.PROPERTY_TIME)) {
		//	setTime(TiConvert.toInt(d, TiC.PROPERTY_TIME));
		}
		if (d.containsKey(TiC.PROPERTY_URL)) {
			//Log.d(LCAT, "processProperties contains TiC.PROPERTY_URL key");
		}
	}

	// @Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy)
	{
		if (PROPERTY_VOLUME.equals(key)) {
			setVolume(TiConvert.toFloat(newValue));
		} else if (TiC.PROPERTY_TIME.equals(key)) {
			setTime(TiConvert.toInt(newValue));
		} else if (TiC.PROPERTY_URL.equals(key)) {
			Log.d(LCAT, "propertyChanged equals TiC.PROPERTY_URL key");
		}
		
	}

	// @Override
	public void propertiesChanged(List<KrollPropertyChange> changes, KrollProxy proxy)
	{
		for (KrollPropertyChange change : changes) {
			propertyChanged(change.getName(), change.getOldValue(), change.getNewValue(), proxy);
		}
	}

	public void onSeekComplete(MediaPlayer mp)
	{
		KrollDict data = new KrollDict();
		data.put("complete", true);
		data.put("duration", proxy.getProperty(TiC.PROPERTY_DURATION));
		data.put("time", proxy.getProperty(TiC.PROPERTY_TIME));
		proxy.fireEvent(EVENT_SEEK_COMPLETE, data);
		data = null;
	}

	public void onPrepared(MediaPlayer player) {
		readyToPlay = true;
		int	duration = mp.getDuration();
		setDuration(duration);
		setStatus(STATUS_READY_TO_PLAY);
		setState(STATE_INITIALIZED);
		// Fire a prepared event. Duration is now avaiable.
		KrollDict data = new KrollDict();
		data.put("prepared", true);
		proxy.fireEvent("prepared", data);		
		data = null;
	}
}