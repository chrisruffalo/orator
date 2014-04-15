orator.controller('SessionViewController', function ($scope, $state, $stateParams, $http, $upload, $timeout, Sessions) {

	$scope.fileStatus = [];
	
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
		
		for(var index = 0; index < fromBook.bookTracks.length; index++) {
			// get track from track list
			var track = fromBook.bookTracks[index];
			
			// construct playlist item
			var item = {};
			item.src = "./services/secured/orate?sessionId=" + fromSession.id + "&trackId=" + track.id; 
			item.type = track.contentType;
			
			// push into playlist
			playlist.push(item);
		}
		
		// create playlist
		$scope.playlist = playlist;
		
		// jump to track item and offset in track item
	};
	
	// load
	$scope.load();
});