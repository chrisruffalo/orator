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
    
    var adjustTime = function(track, time) {
    	var duration = audio.duration;
    	if (duration == 0) {
    		return time;
    	} 
    	
    	// get what the time should be
    	var targetEndTime = track.lengthSeconds;
    	
    	// if duration is equal, return it (yay!)
    	if(duration == targetEndTime) {
    		return time;
    	}
    	
    	// otherwise, seek to adjusted time
    	
    };
   
   // calculate the percent change between expected duration and actual duration
   function calculateAdjustmentFactor(track) {
		var duration = audio.duration;
		
		// if no duration,  use 1 as the factor
		if (duration == 0) {
			return 1;
		} 
		
		// get what the time should be
		var targetEndTime = track.lengthSeconds;
		
		// if duration is equal, then 1 is the factor (1:1)
		if(duration == targetEndTime) {
			return 1;
		}
		
		// now return the ratio of audio duration to target duration
		return (duration * 1.0) / (targetEndTime * 1.0);
    };

    // keep trying to seek until successful
    function seekWhenReady(track) {
    	// don't do anything if sync time is 0
    	if(syncTime < 1) {
    		return;
    	}
    	
		try {
	        // update adjustment factor
			track.adjustment = calculateAdjustmentFactor(track);
			
	        // calculate target time
	        var targetTime = (syncTime * 1.0) * (track.adjustment * 1.0);
	        
	        console.log("target time: " + targetTime);
	        
    		audio.currentTime = targetTime;
    		player.synced = true;
    		syncTime = 0;
		} catch (e) {
			// do nothing
			console.log('fired but got error: ' + e);
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

        // get item
        var item = playlist[current.track];        
        
        // set audio source
        if(!paused) {
        	audio.src = "./services/secured/orate?sessionId=" + item.session.id + "&trackId=" + item.id;
        	audio.type = item.contentType;
        }
        
        // set up event listener
        if(syncTime > 0) {
        	player.synced = false;
        	var listener = function() {
        		seekWhenReady(item);
        		// attempt to remove event
        		if(listener) {
        			// clean up listener
        			audio.removeEventListener('canplay', listener, true);
        		} else {
        			console.log('could not remove event');
        		}
        	};
        	audio.addEventListener('canplay', listener);
        } else {
        	var listener = function() {
        		item.adjustment = calculateAdjustmentFactor(item);
        		// attempt to remove event
        		if(listener) {
        			// clean up listener
        			audio.removeEventListener('canplay', listener, true);
        		} else {
        			console.log('could not remove event');
        		}
        	};
        	// otherwise just set up adjustment factor
        	audio.addEventListener('canplay', listener);
        }

        // start playing (from given position if targeted)
        audio.play();
        
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
    		  var track = player.current();
    		  
    		  // do adjustment
    		  if(track && track.adjustment) {
    			  seconds = (seconds * 1.0) * (track.adjustment * 1.0);
    			  
    		  }
    		  
    		  audio.currentTime = seconds;
    	  }
      },
      
      time: function() {
    	  var time = audio.currentTime; 
    	  
    	  var track = player.current();
    	  if(track && track.adjustment) {
    		  time = time / track.adjustment;
    	  }
    	  
    	  return time;
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
    return new Audio();
});