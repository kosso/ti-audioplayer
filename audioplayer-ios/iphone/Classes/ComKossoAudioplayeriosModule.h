/**
 * audioplayer-ios
 *
 * Created by Kosso
 * Copyright (c) 2017 . All rights reserved.
 */

#import "TiModule.h"
#import <MediaPlayer/MPMediaItem.h>
#import <MediaPlayer/MPNowPlayingInfoCenter.h>
#import <MediaPlayer/MPRemoteCommandCenter.h>

@interface ComKossoAudioplayeriosModule : TiModule
{
}

@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_PLAY;
@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_PAUSE;
@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_STOP;
@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_PLAY_PAUSE;
@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_NEXT;
@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_PREV;
@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_START_SEEK_BACK;
@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_END_SEEK_BACK;
@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_START_SEEK_FORWARD;
@property (nonatomic,readonly) NSNumber *REMOTE_CONTROL_END_SEEK_FORWARD;

@end
