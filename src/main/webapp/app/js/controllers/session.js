orator.controller('SessionViewController', function ($scope, $state, $stateParams, $http, $upload, $timeout, $interval, player, Sessions) {

	$scope.fileStatus = [];
	$scope.syncComplete = false;
	
	// empty starting values to make interpolation stop complaining
	$scope.session = {};
	$scope.session.book = {};
	$scope.session.book.bookTracks = [];

	// make player available to scope
	$scope.player = player;
	
	// handle loading/finding of book
	$scope.load = function() {
		var sessionId = $stateParams.sessionId;
		var bookId = $stateParams.bookId;
		
		// bootstrap from existing configuration
		if(sessionId) {
			var session = Sessions.get({'sessionId': sessionId}, function(){
				$scope.session = session;
				$scope.buildPlaylist();
			});
		} else if(bookId) {
			var session = Sessions.start({'bookId': bookId},
			function() {
				// save book to local scope
				$scope.session = session;
				// update location bar/state with new book id
				$state.go('session', {'sessionId': session.id}, {location: "replace"});
			});
		} else {
			$scope.status.alert = "An error occured while starting the desired session";
		}
	};
	
	$scope.buildPlaylist = function() {
		var currentTrack = 0;
		
		// bulid playlist source elements from tracks
		for(var index = 0; index < $scope.session.book.bookTracks.length; index++) {
			var track = $scope.session.book.bookTracks[index]; 
			
			// determine current track
			if(track.id == $scope.session.currentTrackId) {
				currentTrack = index;
			}
			
			track.session = $scope.session;
			player.playlist.add(track);
		}
		
		// mark first track as active if available
		if(currentTrack < 1 && $scope.session.book.bookTracks &&  $scope.session.book.bookTracks.length > 0) {
			$scope.session.book.bookTracks[0].active = true;
			$scope.session.currentTrackId = $scope.session.book.bookTracks[0].id;
		}
	};
	
	$scope.getPlayingTrackIndex = function() {
		if(!$scope.syncComplete) {
			return $scope.getSessionTrackIndex();			
		}
		
		if(!player) {
			return 0;
		}
	
		return player.index();
	};
	
	// return the index that should be used based on the session value
	$scope.getSessionTrackIndex = function() {
		for(var index = 0; index < $scope.session.book.bookTracks.length; index++) {
			// get track from track list
			var track = $scope.session.book.bookTracks[index];
			
			// determine current track
			if(track.id == $scope.session.currentTrackId) {
				//console.log('session current track at index: ' + index + ' (with session current track id: ' + $scope.session.currentTrackId + ')');
				return index;
			}
		}
		//console.log('no current track found for id ' + $scope.session.currentTrackId);
		return 0;
	};
	
	$scope.getPlayingTrack = function() {
		if(!$scope.syncComplete) {
			return $scope.session.book.bookTracks[$scope.getSessionTrackIndex()];
		}		
		return player.current();
	};
	
	// gets the best guess at the current time
	$scope.getCurrentTime = function(hard) {
		var currentTime = 0;
		
		if(player) {
			currentTime = player.time();
		}
		
		// hard return means a forced query against the player
		if(hard) {
			return currentTime;
		}
		
		// if not playing or the session says otherwise, use session time as current time
		if(!$scope.syncComplete && $scope.session) {
			currentTime = $scope.session.secondsOffset;
		}
		
		// return calculated/best guess time
		return currentTime;
	};
	
	// update the position in the stored session on the server
	$scope.updatePosition = function(force) {
		
		// if not yet synced with session don't update
		if(!$scope.syncComplete) {
			return;
		}
		
		// don't update if no playing is going on
		if(!player || !$scope.session.book.bookTracks || !$scope.session.book.bookTracks.length) {
			//console.log('no audio player, playlist, or playlist length');
			return;
		}
		
		// get current track
		var currentTrack = $scope.getPlayingTrack();
		
		// get current time
		var currentTime = $scope.getCurrentTime(true);
		currentTime = Math.floor(currentTime);
		
		// if the current track and the playing track are the same
		// then check the time.  if it ISN'T then we can skip this part
		// because we've obviously changed tracks.
		if($scope.session.currentTrackId == currentTrack.id) {
			// get session time and check it... if the tracks are the same
			var sessionTime = $scope.session.secondsOffset;
			sessionTime = Math.floor(sessionTime);
			
			// don't sync if session time is 
			// further along than the current time
			// unless it is forced (because of a
			// skip back or something like that)
			if(!force && sessionTime >= currentTime) {
				//console.log('force is: (' + force + ') and session time is ahead of or the same as current play time');
				return;
			}
		} else {
			//console.log('updating with track id: ' + currentTrack.id);
		}
		
		// only send 0 if forced
		if(currentTime > 0 || force) {
			
			// fix for null current time (which would only be sent if forced)
			if(!currentTime) {
				currentTime = "0";
			}
			
			// update session with current time
			var localSession = Sessions.update({sessionId: $scope.session.id, trackId: currentTrack.id}, currentTime, function(){
				// save local session
				$scope.session = localSession;
			}, function() {
				console.log('error while updating session progress');
			});
		}
	};
	
	// ====================== MEDIA CONTROLS ======================
	
	$scope.play = function() {
		// don't do anything if already playing
		if(player.playing) {
			return;
		}
		
		if(!$scope.syncComplete) {
			// get session track index from session
			var trackIndex = $scope.getSessionTrackIndex();
			
			// seek to
			player.seek($scope.session.secondsOffset);

			//console.log('starting from beginning and wanting to set track : ' + trackIndex + ' at time ' + $scope.session.secondsOffset);
			
			// start play (at track index);
			player.play(trackIndex);
	
			// make sync complete
			$scope.syncComplete = true;
		} else {
			// a simple play will suffice
			player.play();
		}
		
	};
	
	$scope.pause = function() {
		player.pause();
		
		// force update after 100ms
		$timeout(function(){
			$scope.updatePosition(true);
		}, 100);
	};
	
	$scope.nextTrack = function() {
		player.next();
	};
	
	$scope.previousTrack = function() {
		player.previous();
	};
	
	$scope.seekForward = function(time) {
		// seek 20 seconds by default
		if(!time) {
			time = 20;
		}
		$scope.seekTo(time, "now");
	};
	
	$scope.seekBackward = function(time) {
		// seek 20 seconds by default
		if(!time) {
			time = 20;
		}
		if(time > 0) {
			time = time * -1;
		}
		$scope.seekTo(time, "now");
	};
	
	$scope.seekTo = function(seconds, from, tries, forcePlay, trackIndex) {
		
		// set defaults
		if(typeof(from) == 'undefined') { from = "start"; }
		if(typeof(tries) == 'undefined') { tries = 1; }
		if(typeof(forcePlay) == 'undefined') { forcePlay = false; }
		
		// abort when tries is too many
		if(tries < 1) {
			//console.log('remaining tries is less than 1');
			return;
		}
		
		// start by getting the position it should start at
		var start = 0;
		if("now" == from) {
			start = $scope.getCurrentTime();
		} else if("end" == from) {
			// measure time from the end
			start = $scope.getCurrentTrack().lengthSeconds;
			
			// time from the end is measured in seconds from the end
			// so make it negative if it isn't already
			if(seconds > 0) {
				seconds = seconds * -1;
			}
		} // "start" is just start = 0 so it's the default case
		
		// calculate
		var seekTime = (start * 1.0) + (seconds * 1.0);
		seekTime = Math.floor(seekTime);
		// log
		//console.log('want to seek to: ' + seekTime);
		
		// do seek
		try {
			player.seek(seekTime);
			//console.log('completed seek to: ' + seekTime + ' from: ' + from);
			
			if(forcePlay && player.playing) {
				// force play on proper track
				if(typeof(trackIndex) != 'undefined') {
					//console.log('playing specified track index');
					player.play(trackIndex);
				} else {
					//console.log('undefined track index given');
					player.play();
				}
			}
			
			// mark sync as complete if the time is changed in any case
			$scope.syncComplete = true;
		} catch (e) {
			// log
			//console.log('there was an error attempting to seek, try again... (' + e + ')');
			
			// seek again, but 50ms later
			$timeout(function(){
				$scope.seekTo(seconds, from, --tries, forcePlay, trackIndex); 
			}, 50);
			// stop doing, and return
			return;
		}
		
		// force update after 100ms
		$timeout(function(){
			$scope.updatePosition(true);
		}, 100);
	};
	
	// ====================== INIT ======================
	
	// load
	$scope.load();
	
	// set up position to update while track is playing
	$interval(function(){
		$scope.updatePosition();
	}, 5000); // every 5 seconds	
	
});