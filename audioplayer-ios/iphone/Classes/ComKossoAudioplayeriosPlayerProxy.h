/**
 

 - Author   : Kosso
 - Date     : August 6, 2015.
 - Updated  : March 6, 2017.
 
 
**/

#import "TiProxy.h"
#import "TiUtils.h"
#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVAudioSession.h>

@interface ComKossoAudioplayeriosPlayerProxy : TiProxy {

    AVPlayer *avPlayer;                 // The AVPlayer
    NSString *url;                      // the url to the audio
    NSTimer *progressUpdateTimer;
    BOOL playing;
    BOOL buffering;
    BOOL paused;
    BOOL stopped;
    BOOL durationavailable;             // flag so it only fires once
    int lastPlayerState;                // playback state
    NSNumber *lastPlayerReadyStatus;    // player status
    BOOL streaming;                     // for live radio. disables durationavailable
    BOOL live_flag;
    // double time;                     // current time
    float rate;
    BOOL pausedForAudioSessionInterruption;
    BOOL gotmeta;
    NSMutableDictionary *metadata; // Track [ID3] metadata
    NSString *initialCategory;
}


#define STATE_BUFFERING 0;
#define STATE_INITIALIZED 1;
#define STATE_PAUSED 2;
#define STATE_PLAYING 3;
#define STATE_STARTING 4;
#define STATE_STOPPED 5;
#define STATE_STOPPING 6;
#define STATE_WAITING_FOR_DATA 7;
#define STATE_WAITING_FOR_QUEUE 8;
#define STATE_FAILED 9; // Not on Android
#define STATE_INTERRUPTED 10; // Not on Android
#define STATE_SEEKING 11; // Not on Android
#define STATE_SEEKING_COMPLETE 12; // Not on Android
 
#define AV_PLAYER_STATUS_UNKNOWN 0;
#define AV_PLAYER_STATUS_READY_TO_PLAY 1;
#define AV_PLAYER_STATUS_FAILED 2;

@property (nonatomic, readwrite, assign) float rate;
@property (nonatomic, readwrite, assign) double duration;
@property (nonatomic, readwrite, assign) double time;
@property (nonatomic, readwrite, assign) int status;
@property (nonatomic, readwrite, assign) int state;

@property (nonatomic, assign) BOOL playing;
@property (nonatomic, assign) BOOL paused;
@property (nonatomic, assign) BOOL buffering;
@property (nonatomic, assign) BOOL live_flag;
@property (nonatomic, assign) BOOL streaming;
@property (nonatomic, assign) BOOL pausedForAudioSessionInterruption;
@property (nonatomic, assign) BOOL gotmeta;

@property (nonatomic, readwrite, assign) NSMutableDictionary *metadata;
@property (nonatomic, readwrite, assign) NSString *initialCategory;



@property(nonatomic, readonly) NSError *error;

- (void)destroy:(id)args;
- (void)start:(id)args;
- (void)play:(id)args;
- (void)stop:(id)args;
- (void)pause:(id)args;
- (void)seek:(id)args;
- (void)seekThenPlay:(id)args;
- (void)speed:(id)args;



@end
