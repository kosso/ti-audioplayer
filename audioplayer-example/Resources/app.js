// audioplayer-example

Ti.Media.audioSessionCategory = Ti.Media.AUDIO_SESSION_CATEGORY_PLAYBACK;

var win = require('/ui/common/window_home').createWindow();
if(Titanium.Platform.osname==='android'){
  win.open({});
} else {
  var rootNavWin = Titanium.UI.iOS.createNavigationWindow({
    zIndex:1,
    window: win
  });
  win.containingNav = rootNavWin;
  rootNavWin.open();
}
