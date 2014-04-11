orator.controller('BookViewController', function ($scope, $routeParams, $http, $upload, $timeout, Books) {
	// state variables
	$scope.state = {};
	$scope.state.fileUpload = false;
	
	// status of files being uploaded
	$scope.fileStatus = [];
	
	// handle loading/finding of book
	$scope.load = function(id) {
		// set initial values
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
				$scope.book = book;
			});
		}		
	};
	
	// handle file uploads when files are selected
	$scope.onFileSelect = function($files) {
	    //$files: an array of files selected, each file has name, size, and type.
	    for (var i = 0; i < $files.length; i++) {
	      var file = $files[i];
	      var status = {'file': file, 'state': 'starting'};
	      $scope.fileStatus.push(status); 
	      $scope.upload = $upload.upload({
	        url: 'services/secured/bookUpload', //upload.php script, node.js route, or servlet url
	        // send book id as value
	        data: {bookId: $scope.book.id},
	        file: file, // or list of files: $files for html5 only
	      }).progress(function(evt) {
	        status.percent = parseInt(100.0 * evt.loaded / evt.total);
	        status.state = 'uploading';
	        status.event = evt;
	        console.dir(file);
	      }).success(function(data, status, headers, config) {
	        status.percent = 100;
	        status.state = 'complete';
	        // todo: start removal timeout
	      });
	      //.error(...)
	      //.then(success, error, progress); 
	      //.xhr(function(xhr){xhr.upload.addEventListener(...)})// access and attach any event listener to XMLHttpRequest.
	    }
	};
	
	// save
	$scope.saveMetadata = function() {
		// if already counting down to save, cancel countdown in favor of
		// counting down for new model
		if($scope.saveMetadataTimer) {
			clearTimeout($scope.saveMetadataTimer);
		}
		
		// start countdown to save
		$scope.saveMetadataTimer = setTimeout(function() {
			// save (and update state)
			$scope.state.saving = true;
			var saveBook = new Books($scope.book);
			saveBook.$save(function() {
				$scope.state.saving = false;
			});
		}, 400); // only save if 400ms have passed since the last time a save has been requested
	};
	
	// load from route params
	$scope.load($routeParams.id);	
});