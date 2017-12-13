function createWindow() {

  var audioplayer;
  var internal_scheme = 'file://';
  if(Ti.Platform.osname === 'android'){
    audioplayer = require('com.kosso.audioplayerandroid');
    internal_scheme = '';
  } else {
    audioplayer = require('com.kosso.audioplayerios');
  }
  // For local file test below.
  var resourcesDirectory = internal_scheme + Ti.Filesystem.resourcesDirectory;


  // Root app window arg. Full screen with navigation controller for iOS.
  // On iOS, When opening a new window from this one (and subsequent ones), make sure you add the containingNav property, pointing to a handle of the one you're opening from. (Best done in a CommonJS 'window opener' module.) 
  var _args = {
    top:0,
    left:0,
    right:0,
    bottom:0,
    zIndex:2,
    fullscreen:true,
    navBarHidden:true,
    tabBarHidden:true,
    navTintColor:'#ff9900',
    tintColor:'#ff9900',
    backgroundColor:'#333'
  };
  if(Ti.Platform.osname==='android'){
    _args.windowSoftInputMode = Ti.UI.Android.SOFT_INPUT_STATE_HIDDEN // Stops textfield in scrollview getting immediate focus
  }

  // Our ROOT app window. 
  var self = Ti.UI.createWindow(_args);


  var scroller = Ti.UI.createScrollView({top:20, left:0, right:0, zIndex:1, bottom:0, contentHeight:Ti.UI.SIZE, scrollType:'vertical', layout:'vertical'});

  var label = Ti.UI.createLabel({
    text:'audioplayer-example',
    top:40,
    font:{fontSize:20},
    color:'white',
    height:Ti.UI.SIZE,
    width:Ti.UI.SIZE
  });
  scroller.add(label);


  var msg = Ti.UI.createLabel({
    text:' - ',
    top:10,
    color:'#ddd',
    height:Ti.UI.SIZE,
    width:Ti.UI.SIZE
  });
  scroller.add(msg);

  var prog = Ti.UI.createLabel({
    text:'--:-- / --:--',
    height:Ti.UI.SIZE,
    color:'#ddd',
    top:10,
    width:Ti.UI.SIZE
  });
  scroller.add(prog);


  var label_url = Ti.UI.createLabel({
    text:' - - - ',
    top:5,
    font:{fontSize:10},
    color:'#ffffcc',
    height:Ti.UI.SIZE,
    left:10,
    right:10,
    textAlign:'center'
  });
  scroller.add(label_url);

  var slider = Ti.UI.createSlider({
    top: 10,
    min: 0,
    max: 100,
    left: 20,
    right: 20,
    value: 0
  });
  scroller.add(slider);
  slider.enabled = false; // enabled when player is ready.


  var btn_control = Ti.UI.createButton({
    width:200,
    top:20,
    height:50,
    backgroundColor:'#222',
    borderRadius:25,
    borderColor:'#ccc',
    borderWidth:2,
    color:'#eee',
    tintColor:'#eee',
    title:'play'
  });
  scroller.add(btn_control);
  btn_control.enabled = false; // enabled when player is ready.
  btn_control.opacity = 0.5;



  var btn_load_tune_1 = Ti.UI.createButton({
    width:200,
    top:20,
    height:50,
    backgroundColor:'#222',
    borderRadius:25,
    borderColor:'#ccc',
    borderWidth:2,
    color:'#eee',
    tintColor:'#eee',
    title:'load remote mp3 1'
  });
  scroller.add(btn_load_tune_1);

  var btn_load_tune_2 = Ti.UI.createButton({
    width:200,
    top:20,
    height:50,
    backgroundColor:'#222',
    borderRadius:25,
    borderColor:'#ccc',
    borderWidth:2,
    color:'#eee',
    tintColor:'#eee',
    title:'load remote mp3 2'
  });
  scroller.add(btn_load_tune_2);

  var btn_load_local = Ti.UI.createButton({
    width:200,
    top:20,
    height:50,
    backgroundColor:'#222',
    borderRadius:25,
    borderColor:'#ccc',
    borderWidth:2,
    color:'#eee',
    tintColor:'#eee',
    title:'load local audio'
  });
  scroller.add(btn_load_local);

  var btn_load_live_stream = Ti.UI.createButton({
    width:200,
    top:20,
    height:50,
    backgroundColor:'#222',
    borderRadius:25,
    borderColor:'#ccc',
    borderWidth:2,
    color:'#eee',
    tintColor:'#eee',
    title:'load live stream'
  });
  scroller.add(btn_load_live_stream);

  // Player states 

  var player_player_state = [
    'STATE_BUFFERING',
    'STATE_INITIALIZED',
    'STATE_PAUSED',
    'STATE_PLAYING',
    'STATE_STARTING',
    'STATE_STOPPED',
    'STATE_STOPPING',
    'STATE_WAITING_FOR_DATA',
    'STATE_WAITING_FOR_QUEUE',
    'STATE_FAILED',
    'STATE_INTERRUPTED',
    'STATE_SEEKING',
    'STATE_SEEKING_COMPLETE'
  ];

  var player_status_strings = [
    'STATUS_UNKNOWN',
    'STATUS_READY_TO_PLAY',
    'STATUS_FAILED'
  ];

  var seeking = false;
  var live_stream = false;  

  // Create player ########################################
  var player = audioplayer.createPlayer({
    allowBackground: true,  // Android only.
    lifecycleContainer: self // Android only.
  });

  // Player listeners.
  player.addEventListener('durationavailable', function(e){
    console.log('durationavailable: ', e);
    prog.text = msecsToSecsAndMinutes(e.source.time) + ' / ' + msecsToSecsAndMinutes(e.duration);
  });

  player.addEventListener('error', function(e){
    player.ready = false;
    console.log('player ERROR: ', e);
    msg.text = player_status_strings[e.status];
    prog.text = "--:-- / --:--";
    //alert('error!\n\n'+e.message);
    slider.enabled = false;
  });

  player.addEventListener('complete', function(e){
    console.log('player COMPLETE: ', e);
    msg.text = player_player_state[e.source.state];
    prog.text = '00:00 / '+msecsToSecsAndMinutes(player.duration);
    slider.value = 0;
    
    btn_control.title = 'play';

  });

  player.addEventListener('playerstatuschange', function(e){
    // readiness to play status
    console.log('PLAYER status change: ', e);
    console.log(player_status_strings[e.status]);
    msg.text = player_status_strings[e.status];
    prog.text = '--:-- / --:--';

    switch(e.status) {
      case 1:
        btn_control.enabled = true;
        btn_control.title = 'play';
        btn_control.opacity = 1.0;
        player.ready = true; // Player is ready to use. 
        msg.text = 'ready'; // iOS

        break;
      default:
        slider.enabled = false;
        btn_control.enabled = false;
        btn_control.opacity = 0.5;
    }


  });

  player.addEventListener('change', function(e){
    // playback state
    console.log('PLAYBACK state change: ', e);
    console.log(player_player_state[e.state])
    msg.text = player_player_state[e.state];

    
    switch(e.state) {
      case 1:
        slider.enabled = false;
        msg.text = 'ready';
        break;
      case 2:
        slider.enabled = false;
        btn_control.title = 'play';
        break;
      case 3:
        if(e.streaming){
          live_stream = true;
          slider.enabled = false;
          btn_control.title = 'stop';
        } else {
          slider.enabled = true;
          btn_control.title = 'pause';

        }
        break;
      case 4:
        slider.enabled = true;
        msg.text = 'wait ...';
        break;
      case 5: // stopped
        if(e.streaming){
          slider.enabled = false;        
        } else {
          slider.enabled = true;
        }
        break;
      default:
        // 
    }


  });

  player.addEventListener('seekcomplete', function(e){
    console.log('seekcomplete: ', e);
    // seek has completed
    seeking = false;
    //pause.title = 'pause';
    player.play();
  });

  player.addEventListener('progress', function(e){
      // console.log('progress: '+e.progress+' / '+e.source.duration);
      // e.progress
      // e.source.duration
      if(seeking===true){
        return;
      }
      prog.text = msecsToSecsAndMinutes(e.progress) + ' / ' + msecsToSecsAndMinutes(e.source.duration);
      slider.value = Math.round((e.progress / e.source.duration)*100);
  });

  // Slider listeners #################################################
  slider.addEventListener('change', function(e){
    if(seeking===true){
      prog.text = msecsToSecsAndMinutes(Math.round((e.value / 100) * player.duration)) + ' / ' + msecsToSecsAndMinutes(player.duration); 
    }
  });
  slider.addEventListener('start', function(e){
    seeking = true;
  });
  slider.addEventListener('stop', function(e){
    //seeking = false; will be set by the seekcomplete event
    var new_time = Math.round(player.duration * (e.value / 100) );
    console.log('seek to new time : '+new_time);
    player.seek(new_time);
  });


  // Button listeners.
  btn_load_tune_1.addEventListener('click', function(e){
    loadAudio('https://kosso.co.uk/stuff/kozcar1.mp3');
  });

  btn_load_tune_2.addEventListener('click', function(e){
    loadAudio('https://ia601406.us.archive.org/3/items/gd1978-12-16.sonyecm250-no-dolby.walker-scotton.miller.82212.sbeok.flac16/gd78-12-16d1t01.mp3');
  });

  btn_load_local.addEventListener('click', function(e){
    loadAudio(resourcesDirectory + 'test.m4a');
  });

  // Radio.
  btn_load_live_stream.addEventListener('click', function(e){
    loadAudio('http://stream.amazingradio.com:8000/');
  });


  function loadAudio(url){
    msg.text = 'loading ... ';
    if(player.playing || player.paused){
      player.stop();
    }
    player.destroy(); 
    player.setUrl(url);
    player.addEventListener('playerstatuschange', ready);
    // Play when ready
    function ready(e){
      player.removeEventListener('playerstatuschange', ready);
      player.play();
    }

    slider.value = 0;
    slider.enabled = false;
    btn_control.enabled = false; // enabled when player is ready.
    btn_control.opacity = 0.5;

    label_url.text = url;
  }


  btn_control.addEventListener('click', function(e){
    if(!player.ready){
      console.log('player is not ready');
      return;
    }

    if(player.url===undefined){
      console.log('no audio loaded!');
      return;
    }

    if(live_stream===true){
      console.log('LIVE!');
      if(player.playing){
        console.log('STOP STREAM');  // It's actually better to destroy streams and reload via the url. 
        player.stop();
      } else {
        console.log('PLAY STREAM');
        player.play();
      }
      return;
    }

    if(player.playing){
      player.pause();
      
    } else {
      player.play();
    }


  });



  // Set an initial audio url  #####################################
  // player.setUrl('');

  // With the first load of the player in the app, we need to wait for the player to be initialized by its first url before attempting to play it.
  // Wait for e.status===1 in the 'playerstatuschange event'
  // This is probably a bug to look at ;)


  self.add(scroller);


  // BONUS Android pointss !!!

  if(Ti.Platform.osname==='android'){
    self.addEventListener('android:back', function(e){
      if(!player){
        console.log('no player');
        return;
      }
      var playing_warning_msg = 'Audio is still playing.\n\nAre you sure you want to exit the app?\n\nIf you want to still play music, put the app in the background.';
      var close_warning_opts = {
        cancel: 0,
        buttonNames: ['CANCEL', 'EXIT'],
        destructive: 1,
        message: playing_warning_msg
      };
      
      if(player.playing === true){
        var cexitDlg = Ti.UI.createAlertDialog(close_warning_opts);
        cexitDlg.addEventListener('click', function(e){
          if(e.index===1){
           Titanium.Android.currentActivity.finish();
          }
        });
        cexitDlg.show();
      } else {
        Ti.Android.currentActivity.finish();
      }
    });
  }


  // return the root app window
  return self;

}
exports.createWindow = createWindow;

function msecsToSecsAndMinutes(msecs, show_msecs, show_hours, show_long){
  show_msecs = show_msecs || false;
  show_hours = show_hours || false;
  show_long = show_long || false;
  var msSecs = (1000);
  var msMins = (msSecs * 60);
  var msHours = (msMins * 60);
  var numHours = Math.floor(msecs/msHours);
  var numMins = Math.floor((msecs - (numHours * msHours)) / msMins);
  var numSecs = Math.floor((msecs - (numHours * msHours) - (numMins * msMins))/ msSecs);
  var numMillisecs = ((msecs - (numHours * msHours) - (numMins * msMins) - (numSecs * msSecs)) / 10).toFixed();
  if(numMillisecs==100){
    numMillisecs = 0;
  }
  var longString = "";
  if (numHours > 0){;
    if (numHours < 10 && !show_long){;
      numHours = "0" + numHours;
    }
    var hs = 's';
    if(numHours==1 || numHours=='01'){hs='';}
    longString = numHours + " hour"+hs;
    numHours = numHours + ":";
  } else {
    numHours = "";
    longString = "";
    if(show_hours){
      numHours = "00:";
    }
  }
  if (numMins < 10 && !show_long){
    numMins = "0" + numMins;
  }
  if(numMins > 0){
    if(longString!=''){
     longString += ', ';
    }
    var ms = 's';
    if(show_long && numSecs > 30){
      numMins++;
    }
    if(numMins==1 || numMins=='01'){ms='';}

    longString += numMins + ' minute'+ms;
  }
  if (numSecs < 10 && !show_long){;
    numSecs = "0" + numSecs;
  }
  if (numMillisecs < 10 && !show_long){
    numMillisecs = "0" + numMillisecs;
  } 
  var msec = '';
  if(show_msecs){
      msec = '.'+numMillisecs;
  }
  var resultString = numHours + numMins + ":" + numSecs  + msec;
  if(show_long){
    return longString;
  } else {
    return resultString;
  }
}
