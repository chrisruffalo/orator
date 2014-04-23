// originally from: http://angular.github.io/peepcode-tunes/public/ 

orator.factory('player', function(audio, $timeout, $rootScope) {
    var player,
    playlist = [],
    paused = false,
    syncTime = 0,
    current = {
      album: 0,
      track: 0
    };
    
    var stubbornSeek = function(toTime) {
		try {
    		audio.currentTime = toTime;
    		console.log('forwarding playhead to: ' + syncTime);
    		player.synced = true;
    		syncTime = 0;
		} catch (e) {
			$timeout(function() {
				stubbornSeek(toTime);
			}, 50);
		}
    };

    // create player
    player = {
      playlist: playlist,

      current: current,

      synced: true,
      
      playing: false,

      play: function(track) {
    	  
        if (!playlist.length) {
        	return;
        }

        if (angular.isDefined(track)) {
        	current.track = track;
        }

        var item = playlist[current.track];
        
        // set audio source
        if(!paused) {
        	audio.src = "./services/secured/orate?sessionId=" + item.session.id + "&trackId=" + item.id;
        	audio.type = item.contentType;
        }

        // start playing (from given position if targeted)
        audio.play();
        if(syncTime > 0) {        	
        	stubbornSeek(syncTime);
        }
        
        // update values
        player.playing = true;        
        paused = false;        
        
        // set all items inactive
        for(var activeIndex = 0; activeIndex < playlist.length; activeIndex++) {
        	playlist[activeIndex].active = false;
        }
        
        // set item active
        item.active = true;
        playlist[current.track].active = true;
      },

      pause: function() {
        if (player.playing) {
          audio.pause();
          player.playing = false;
          paused = true;
        }
      },

      reset: function() {
        player.pause();
        current.track = 0;
      },

      next: function() {
        if (!playlist.length) return;
        paused = false;
        if (playlist.length > (current.track + 1)) {
          current.track++;
        } else {
        	return;
        }
        if (player.playing) {
        	player.play();
        }
      },

      previous: function() {
        if (!playlist.length) {
        	return;
        }
        
        paused = false;
        
        if (current.track > 0) {
          current.track--;
        } else {
        	return;
        }
        
        if (player.playing) {
        	player.play();
        }
      },
      
      seek: function(seconds) {
    	  if(!audio.currentTime || "HAVE_NOTHING" == audio.readyState || paused) {
    		  syncTime = seconds;
    		  player.synced = false;
    	  } else {
    		  audio.currentTime = seconds;
    	  }
      },
      
      time: function() {
    	  return audio.currentTime;
      },
      
      current: function() {
    	  return playlist[current.track];
      },
      
      index: function() {
    	  return current.track;
      }      
      
    };

    playlist.add = function(track) {
      if(!playlist) {
    	  return;
      }
      playlist.push(track);
    };

    playlist.remove = function(album) {
      var index = playlist.indexOf(track);
      playlist.splice(index, 1);
    };

    audio.addEventListener('ended', function() {
      $rootScope.$apply(player.next);
    }, false);

    return player;
});


// extract the audio for making the player easier to test
orator.factory('audio', function($document) {
    var audio = $document[0].createElement('audio');
    
	var container = $document[0].getElementById("audioContainer");
	container.innerHTML = '';
	
	container.appendChild(audio);
	
    return audio;
});