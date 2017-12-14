# ti-audioplayer


btw: I have not documented any of all this fully yet. 
 
I built these out of sheer and utter frustration at how old and 'different to each other' the current versions are in Titanium.  I also built them to provide some parity between the APIs.

The current Titanium AudioPlayer is _still_ based on Matt Gallagher's AudioStreamer which is nearly a decade old!! And also AVPlayer since arrived on iOS since then, for a long time! (The Java version is very similar to the Titanium version. ) 

I used them for the AmazingRadio apps. These have live streaming and remote url tunes, playlists etc, with seeking:

- iOS: http://itunes.apple.com/us/app/amazingradio/id476404037?ls=1&mt=8

- Android : https://market.android.com/details?id=com.amazingmedia.radio1

Soon these two modules will have the same module ID. (Studio is a pain when trying to create dual platform modules.) 

NOTE: The iOS version also has some extra features, such as responding to the lockscreen remote controller and the ability to set the lockscreen now playing info (NB: Image MUST be cached locally first for lockscreen use). 

