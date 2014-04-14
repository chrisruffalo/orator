orator.controller('BookTableController', function ($scope, $state, $http, $timeout, Books) {
	// no books
	$scope.books = null;
		
	// periodic update
	$scope.updatePeriodic = function() {
		// cancel existing update timer
		if($scope.updateTimer) {
			$timeout.cancel($scope.updateTimer);
		}
		
		// update
		var books = Books.query(function(){
			$scope.books = books;
			
			// schedule timeout
			$scope.updateTimer = $timeout(function(){
				$scope.updatePeriodic();
			}, 2000); // update every 2 seconds
		});
	};
	
	$scope.deleteBook = function(book) {
		var books = Books.deleteBook({bookId: book.id}, function(){
			$scope.books = books;
		});
	};
	
	$scope.details = function(book) {
		$state.go('book', {bookId: book.id});
	};
	
	// start update cycle
	$scope.updatePeriodic();
});