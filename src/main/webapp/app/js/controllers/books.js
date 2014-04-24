orator.controller('BookTableController', function ($rootScope, $scope, $state, $http, $timeout, Books) {
	// no books
	$scope.books = null;
	
	// when state changes, cancel timer
	$rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams){
		if($scope.updateTimer) {
			$timeout.cancel($scope.updateTimer);
		}
	});
		
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

	$scope.read = function(book) {
		$state.go('start-session', {bookId: book.id});
	};
	
	// start update cycle
	$scope.updatePeriodic();
});