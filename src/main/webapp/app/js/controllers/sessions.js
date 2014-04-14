orator.controller('SessionTableController', function ($rootScope, $scope, $state, $http, $timeout, Sessions) {

	// no sessions
	$scope.sessions = null;
		
	// periodic update
	$scope.load = function() {
		// update
		var sessions = Sessions.query(function(){
			$scope.sessions = sessions;
		});
	};
	
	$scope.deleteSession = function(session) {
		var sessions = Sessions.deleteSession({sessionId: session.id}, function(){
			$scope.sessions = sessions;
		});
	};
	
	$scope.openSession = function(session) {
		$state.go('session', {sessionId: session.id});
	};
	
	// load once
	$scope.load();
	
});