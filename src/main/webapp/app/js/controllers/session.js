orator.controller('SessionViewController', function ($scope, $state, $stateParams, $http, $upload, $timeout, $interval, Sessions) {

	$scope.fileStatus = [];
	$scope.syncComplete = false;
	
	// handle loading/finding of book
	$scope.load = function() {
		// set initial value for scope object
		$scope.session = null;

		var sessionId = $stateParams.sessionId;
		var bookId = $stateParams.bookId;
		
		// bootstrap from existing configuration
		if(sessionId) {
			var session = Sessions.get({'sessionId': sessionId}, function(){
				$scope.session = session;
				$scope.buildPlaylist(session, session.book);
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
	
	$scope.buildPlaylist = function(fromSession, fromBook) {
		var playlist = [];
		
		$scope.currentTrackIndex = 0;
		
		for(var index = 0; index < fromBook.bookTracks.length; index++) {
			// get track from track list
			var track = fromBook.bookTracks[index];
			
			// construct playlist item
			var item = {};
			item.id = track.id;
			item.src = "./services/secured/orate?sessionId=" + fromSession.id + "&trackId=" + track.id; 
			item.type = track.contentType;
			
			// determine current track
			if(track.id == fromSession.currentTrackId) {
				$scope.currentTrackIndex = index;
			}
			
			// push into playlist
			playlist.push(item);
		}
		
		// create playlist
		$scope.playlist = playlist;
		
		// sync
		$timeout($scope.syncToSession, 300); // after 200ms
	};
	
	// sync the playlist with the current session
	$scope.syncToSession = function() {
		try {		
			var index = $scope.getPlayingTrackIndex();
			
			// set track if current track is not set
			if($scope.currentTrackIndex != index) {
				// jump to track item and offset in track item
				$scope.audioPlayer.play($scope.currentTrackIndex);
			}
	
			// seek to offset if available
			if($scope.session.secondsOffset) {
				$scope.audioPlayer.seek($scope.session.secondsOffset);
				$scope.syncComplete = true;
			}
		} catch (e) {
			$timeout($scope.syncToSession, 100); // wait another 100ms
		}
	};
		
	$scope.doJump = function() {
		var ctime = $scope.audioPlayer.currentTime;
		var ftime = ctime + 120;
		$scope.audioPlayer.seek(ftime);
	};
	
	$scope.getPlayingTrackIndex = function() {
		// get current track
		var index = $scope.audioPlayer.currentTrack;
		if(index >= 1) {
			index = index - 1;
		}
		return index;
	};
	
	$scope.updatePosition = function() {
		// don't update if no playing is going on
		if(!$scope.audioPlayer || !$scope.audioPlayer.playing || !$scope.playlist || !$scope.playlist.length) {
			return;
		}
		
		// if not synced then don't update
		if(!$scope.syncComplete) {
			return;
		}
		
		// get current time
		var currentTime = $scope.audioPlayer.currentTime;
		
		// get current track
		var index = $scope.getPlayingTrackIndex();		
		var currentTrack = $scope.playlist[index];
		
		// debug
		//console.dir($scope.playlist);
		//console.dir(currentTrack);
		
		// update session with current time
		var localSession = Sessions.update({sessionId: $scope.session.id, trackId: currentTrack.id}, currentTime, function(){
			$scope.session = localSession;
		});
	};
	
	// load
	$scope.load();
	
	// set up position to update while track is playing
	$interval(function(){
		$scope.updatePosition();
	}, 5000); // every 5 seconds
});