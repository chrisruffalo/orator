orator.controller('BookViewController', function ($scope, $state, $stateParams, $http, $upload, $timeout, Books) {
	// state variables
	$scope.state = {};
	$scope.editing = false;
	
	// status of files being uploaded
	$scope.fileStatus = [];
	
	// handle loading/finding of book
	$scope.load = function(id) {
		// set initial value for scope object
		$scope.book = null;

		// bootstrap from existing configuration
		if(id) {
			var book = Books.get({bookId: id}, function(){
				$scope.book = book;
			});
		} else {
			var book = Books.save({
				title: 'New Book Title',
				author: 'New Book Author'
			},
			function() {
				// save book to local scope
				$scope.book = book;
				// update location bar/state with new book id
				$state.go('book', {bookId: book.id}, {location: "replace"});
			});
		}		
	};
	
	// handle file uploads when files are selected
	$scope.onFileSelect = function($files) {
	    //$files: an array of files selected, each file has name, size, and type.
	    for (var i = 0; i < $files.length; i++) {
	      var file = $files[i];
	      var currentUploadStatus = {
	    	  'index': i,	
	    	  'file': file, 
	    	  'state': 'starting', 
	    	  'show': true,
	    	  'errorMessage': null
	      };
	      // create IIFE ... todo: make this it's own function
	      (function(localUploadStatus) {
		      $scope.fileStatus.push(localUploadStatus); 
		      localUploadStatus.upload = $upload.upload({
		    	  // call book upload with bookId as parameter (tried using the data block but it made the servlet MUCH more complicated)
				  url: 'services/secured/audioBookUpload?bookId=' + $scope.book.id,
				  // send book id as value
				  // data: {bookId: $scope.book.id},
	  			  file: file, // or list of files: $files for html5 only
		      }).progress(function(evt) {
		    	  console.log("updating percent status on index: " + localUploadStatus.index);
		    	  console.log("i value: " + i);
		    	  localUploadStatus.percent = parseInt(100.0 * evt.loaded / evt.total);
				  if(localUploadStatus.percent >= 100) {
					  localUploadStatus.state = 'complete';
				  } else {
					  localUploadStatus.state = 'uploading';
				  }
			  }).success(function(data, status, headers, config) {
		    	localUploadStatus.percent = 100;
		    	localUploadStatus.state = 'complete';
	        	$scope.deferedLoad($scope.book.id);	    	
		    	localUploadStatus.removal = $timeout(function() {
		        	$scope.removeFileStatus(localUploadStatus);	  
		        }, 180000); // wait 3 minutes until removal
		      })
		      .error(function(){
		    	 localUploadStatus.state = 'error';
		    	 localUploadStatus.errorMessage = 'Upload ended unexpectedly';
		      })
		      //.then(success, error, progress); 
		      //.xhr(function(xhr){xhr.upload.addEventListener(...)})// access and attach any event listener to XMLHttpRequest.
		      ;
	      })(currentUploadStatus);
	    }
	};
	
	// acknowledge/remove status line
	$scope.removeFileStatus = function(uploadStatus) {
		if(uploadStatus.removal) {
			$timeout.cancel(uploadStatus.removal);
		}
		uploadStatus.show = false;
	};
	
	// abort individual file upload
	$scope.abortUpload = function(uploadStatus) {
		if(uploadStatus.upload) {
			uploadStatus.upload.abort();
		}
		uploadStatus.state = 'canceled';
		currentUploadStatus.errorMessage = 'Upload canceled by user';
	};
	
	$scope.deferedLoad = function(id) {
		// cancel timer
		if($scope.deferLoadTimer) {
			$timeout.cancel($scope.deferLoadTimer);
		}
		
		// try and load
		$scope.deferLoadTimer = $timeout(function(){
			$scope.load(id);
		}, 500); // wait 500ms to see if something else changes before doing it
	};
	
	// read
	$scope.read = function() {
		$state.go('start-session', {bookId: $scope.book.id});
	};
	
	// save
	$scope.saveMetadata = function() {
		// if already counting down to save, cancel countdown in favor of
		// counting down for new model
		if($scope.saveMetadataTimer) {
			$timeout.cancel($scope.saveMetadataTimer);
		}
		
		// start countdown to save
		$scope.saveMetadataTimer = $timeout(function() {
			// save (and update state)
			$scope.state.saving = true;
			var saveBook = new Books($scope.book);
			saveBook.$save(function() {
				$scope.state.saving = false;
			});
		}, 200); // only save if 200ms have passed since the last time a save has been requested
	};
	
	// delete track
	$scope.deleteTrack = function(track) {
		var updatedBook = Books.deleteTrack({bookId: $scope.book.id, trackId: track.id}, function() {
			$scope.book = updatedBook;
		});
	};	

	// sortable table
	$scope.sortableOptions = {
		// options
		disabled: true, // always starts disabled
		axis: "y", // only up/down  (no need to go side/side)
		containment: "#trackTable", // stay in table
		handle: ".grabber",
		forcePlaceholderSize: true,
		placeholder: "sortable-placeholder",
		tolerance: "pointer",
		
		// callbacks
		update : function(e, ui) {
			//nothing
		},
		stop : function(e, ui) {
			//nothing
		}
	};
	
	// editing controls
	$scope.editTracks = function() {
		// enable sorting
		$scope.sortableOptions.disabled = false;
		
		// copy tracks for reset
		$scope.backupTracks = angular.copy($scope.book.bookTracks);
		
		// switch to editing mode
		$scope.editing = true;
	};
	
	$scope.resetTracks = function() {
		// reset tracks
		if($scope.backupTracks != null) {
			$scope.book.bookTracks = $scope.backupTracks;
			$scope.backupTracks = null;
		}
	};
	
	$scope.cancelEditTracks = function() {
		// enable sorting
		$scope.sortableOptions.disabled = true;
		
		// reset on cancel
		$scope.resetTracks();
		
		// switch to editing mode
		$scope.editing = false;
	}
	
	$scope.saveTracks = function() {
		// enable sorting
		$scope.sortableOptions.disabled = true;
		
		// todo: call to save tracks
		
		// switch from editing mode
		$scope.editing = false;
	};
	
	// load from route params
	$scope.load($stateParams.bookId);	
});