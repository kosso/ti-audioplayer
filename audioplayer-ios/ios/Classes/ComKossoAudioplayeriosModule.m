/**
 * audioplayer-ios
 *
 * Created by Kosso
 * Copyright (c) 2017 . All rights reserved.
 */

#import "ComKossoAudioplayeriosModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"


@implementation ComKossoAudioplayeriosModule

MAKE_SYSTEM_PROP(REMOTE_CONTROL_PLAY,UIEventSubtypeRemoteControlPlay);
MAKE_SYSTEM_PROP(REMOTE_CONTROL_PAUSE,UIEventSubtypeRemoteControlPause);
MAKE_SYSTEM_PROP(REMOTE_CONTROL_STOP,UIEventSubtypeRemoteControlStop);
MAKE_SYSTEM_PROP(REMOTE_CONTROL_PLAY_PAUSE,UIEventSubtypeRemoteControlTogglePlayPause);
MAKE_SYSTEM_PROP(REMOTE_CONTROL_NEXT,UIEventSubtypeRemoteControlNextTrack);
MAKE_SYSTEM_PROP(REMOTE_CONTROL_PREV,UIEventSubtypeRemoteControlPreviousTrack);
MAKE_SYSTEM_PROP(REMOTE_CONTROL_START_SEEK_BACK,UIEventSubtypeRemoteControlBeginSeekingBackward);
MAKE_SYSTEM_PROP(REMOTE_CONTROL_END_SEEK_BACK,UIEventSubtypeRemoteControlEndSeekingBackward);
MAKE_SYSTEM_PROP(REMOTE_CONTROL_START_SEEK_FORWARD,UIEventSubtypeRemoteControlBeginSeekingForward);
MAKE_SYSTEM_PROP(REMOTE_CONTROL_END_SEEK_FORWARD,UIEventSubtypeRemoteControlEndSeekingForward);

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"7e0e9c2c-a126-4b32-ac41-3e1742cb8039";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"com.kosso.audioplayerios";
}

#pragma mark Lifecycle

-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];
	
    // Lockscreen/remote control listener
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(remoteControlEvent:)
                                                 name:kTiRemoteControlNotification
                                               object:nil];
    NSLog(@"[INFO] %@ loaded",self);
    
}

-(void)shutdown:(id)sender
{
	// this method is called when the module is being unloaded
	// typically this is during shutdown. make sure you don't do too
	// much processing here or the app will be quit forceably

	// you *must* call the superclass
	[super shutdown:sender];
}

#pragma mark Cleanup

-(void)dealloc
{
	// release any resources that have been retained by the module
	[super dealloc];
}

#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	// optionally release any resources that can be dynamically
	// reloaded once memory is available - such as caches
	[super didReceiveMemoryWarning:notification];
}

#pragma mark Listener Notifications

-(void)_listenerAdded:(NSString *)type count:(int)count
{
	if (count == 1 && [type isEqualToString:@"my_event"])
	{
		// the first (of potentially many) listener is being added
		// for event named 'my_event'
	}
}

-(void)_listenerRemoved:(NSString *)type count:(int)count
{
	if (count == 0 && [type isEqualToString:@"my_event"])
	{
		// the last listener called for event named 'my_event' has
		// been removed, we can optionally clean up any resources
		// since no body is listening at this point for that event
	}
}

#pragma Public APIs

-(void)setNowPlayingInfo:(id)args
{
    // Set lockscreen info
    
    ENSURE_SINGLE_ARG(args,NSDictionary);
    NSString *artist = [TiUtils stringValue:@"artist" properties:args def:@""];
    NSString *title = [TiUtils stringValue:@"title" properties:args def:@""];
    NSString *albumTitle = [TiUtils stringValue:@"albumTitle" properties:args def:@""];
    NSString *albumArtwork = [TiUtils stringValue:@"albumArtwork" properties:args def:nil];
    NSString *duration = [TiUtils stringValue:@"duration" properties:args def:nil];
    
    float rate = [TiUtils floatValue:@"rate" properties:args def:1.0f];
    double currentTime = [TiUtils doubleValue:@"currentTime" properties:args def:0];
    
    //NSLog(@"[INFO] LOCKSCREEN set title    : %@", title);
    //NSLog(@"[INFO] LOCKSCREEN set artwork  : %@", albumArtwork);
    //NSLog(@"[INFO] LOCKSCREEN set duration : %@", duration);
    //NSLog(@"[INFO] LOCKSCREEN rate         : %f", rate);
    //NSLog(@"[INFO] LOCKSCREEN currentTime  : %f", currentTime);
    
    Class playingInfoCenter = NSClassFromString(@"MPNowPlayingInfoCenter");
    if (playingInfoCenter) {
        
        NSMutableDictionary *songInfo = [[NSMutableDictionary alloc] init];
        if(albumArtwork != nil){
            UIImage *artworkImage = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:albumArtwork]]];
            MPMediaItemArtwork *albumArt = [[MPMediaItemArtwork alloc] initWithImage:artworkImage];
            [songInfo setObject:albumArt forKey:MPMediaItemPropertyArtwork];
        }

        [songInfo setObject:artist forKey:MPMediaItemPropertyArtist];
        [songInfo setObject:title forKey:MPMediaItemPropertyTitle];
        [songInfo setObject:albumTitle forKey:MPMediaItemPropertyAlbumTitle];
        
        [songInfo setObject:[NSNumber numberWithDouble:currentTime] forKey:MPNowPlayingInfoPropertyElapsedPlaybackTime];
        
        // set the duration
        if(duration!=nil){
            // Live streaming won't have this.
            [songInfo setObject:duration forKey:MPMediaItemPropertyPlaybackDuration];
        }
        [songInfo setObject:[NSNumber numberWithFloat:rate] forKey:MPNowPlayingInfoPropertyPlaybackRate];
        
        [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:songInfo];
        
    }
}

-(void)clearNowPlayingInfo:(id)args
{
    Class playingInfoCenter = NSClassFromString(@"MPNowPlayingInfoCenter");
    if (playingInfoCenter) {
        [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:nil];
    }
}

-(void)emptyFunction
{
    // Do nothing. See below.
}

-(void)showPlayPauseControls:(id)args
{
    
    // Does not appear to be working any more. setEnabled has been removed.
    
    NSLog(@"[INFO] showPlayPauseControls");
    
    Class remoteCommandCenter = NSClassFromString(@"MPRemoteCommandCenter");
    
    if(remoteCommandCenter){
        
        MPRemoteCommandCenter *remoteCommandCenter = [MPRemoteCommandCenter sharedCommandCenter];
        // Remove all buttons apart from the playStop toggle
        [remoteCommandCenter.togglePlayPauseCommand addTarget:self action: @selector(emptyFunction)];
        //[remoteCommandCenter.togglePlayPauseCommand setEnabled:YES];
        [remoteCommandCenter.nextTrackCommand addTarget:self action: @selector(emptyFunction)];
        [remoteCommandCenter.previousTrackCommand addTarget:self action: @selector(emptyFunction)];
        
        [remoteCommandCenter.nextTrackCommand removeTarget:self action: @selector(emptyFunction)];
        [remoteCommandCenter.previousTrackCommand removeTarget:self action: @selector(emptyFunction)];
        //[remoteCommandCenter.nextTrackCommand setEnabled:NO];
        //[remoteCommandCenter.previousTrackCommand setEnabled:NO];
        
    }
}

-(void)showPlaylistControls:(id)args
{
    // Does not appear to be working any more. setEnabled has been removed.

    // NSLog(@"[INFO] showPlaylistControls");
    // args:
    // 0 : bool enable previousTrackCommand
    // 1 : bool enable nextTrackCommand
    Class remoteCommandCenter = NSClassFromString(@"MPRemoteCommandCenter");
    
    if(remoteCommandCenter){
        
        MPRemoteCommandCenter *remoteCommandCenter = [MPRemoteCommandCenter sharedCommandCenter];
        // [remoteCommandCenter.togglePlayPauseCommand addTarget:self action: @selector(emptyFunction)];
        //[remoteCommandCenter.togglePlayPauseCommand setEnabled:YES]; // This used to work on older iOS.
        
        [remoteCommandCenter.nextTrackCommand addTarget:self action: @selector(emptyFunction)];
        //[remoteCommandCenter.nextTrackCommand setEnabled:[TiUtils boolValue:[args objectAtIndex:1]]];
        
        [remoteCommandCenter.previousTrackCommand addTarget:self action: @selector(emptyFunction)];
        //[remoteCommandCenter.previousTrackCommand setEnabled:[TiUtils boolValue:[args objectAtIndex:0]]];
    }
    
}



-(void)remoteControlEvent:(NSNotification *)notification
{
    UIEvent *event = [[notification userInfo] objectForKey:@"event"];
    NSDictionary *e = [NSDictionary dictionaryWithObject:NUMINT(event.subtype) forKey:@"subtype"];
    if ([self _hasListeners:@"remotecontrol"]) {
        [self fireEvent:@"remotecontrol" withObject:e];
    }
}


@end
