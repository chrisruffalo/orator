orator.controller('BookViewController', function ($scope, $state, $stateParams, $http, $upload, $timeout, Books) {
	// state variables
	$scope.state = {};
	$scope.state.fileUpload = false;
	
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
	    	  'file': file, 
	    	  'state': 'starting', 
	    	  'show': true,
	    	  'errorMessage': null
	      };
	      $scope.fileStatus.push(currentUploadStatus); 
	      currentUploadStatus.upload = $upload.upload({
	    	  // call book upload with bookId as parameter (tried using the data block but it made the servlet MUCH more complicated)
			  url: 'services/secured/audioBookUpload?bookId=' + $scope.book.id,
			  // send book id as value
			  // data: {bookId: $scope.book.id},
  			  file: file, // or list of files: $files for html5 only
	      }).progress(function(evt) {
			  currentUploadStatus.percent = parseInt(100.0 * evt.loaded / evt.total);
			  if(currentUploadStatus.percent >= 100) {
				  currentUploadStatus.state = 'complete';
			  } else {
				  currentUploadStatus.state = 'uploading';
			  }
		  }).success(function(data, status, headers, config) {
	    	currentUploadStatus.percent = 100;
	    	currentUploadStatus.state = 'complete';
        	$scope.deferedLoad($scope.book.id);	    	
	    	currentUploadStatus.removal = $timeout(function() {
	        	$scope.removeFileStatus(currentUploadStatus);	  
	        }, 180000); // wait 3 minutes until removal
	      });
	      //.error(...)
	      //.then(success, error, progress); 
	      //.xhr(function(xhr){xhr.upload.addEventListener(...)})// access and attach any event listener to XMLHttpRequest.
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
		uploadStatus.upload.abort();
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
	}
	
	// load from route params
	$scope.load($stateParams.bookId);	
});