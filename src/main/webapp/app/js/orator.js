// define the application
var orator = angular.module('orator', 
	[
	 'ui.router',				// enhanced url router
	 'ngResource', 				// json/ajax services
	 //'ngRoute', 				// application url router
	 'xeditable', 				// edit values in-place
	 'angularFileUpload'		// file upload
	]
);

// set options when orator starts
orator.run(function(editableOptions, editableThemes) {
	editableThemes.bs3.inputClass = 'input-sm';
	editableThemes.bs3.buttonsClass = 'btn-sm';
	editableOptions.theme = 'bs3'; // bootstrap3 theme
});

// configure route provider
orator.config(function($stateProvider, $urlRouterProvider){
	
	// set default
	$urlRouterProvider.otherwise("/book");
	
	// set up routes
	$stateProvider
	.state('book', {
        url: "/book/:bookId",
        templateUrl: 'app/templates/book.html',
        controller: 'BookViewController'
    })
    // not sure if we need this state forever... but it's here
    // until we can get the parent/child state to work correctly...
    .state('newBook', {
        url: "/book",
        templateUrl: 'app/templates/book.html',
        controller: 'BookViewController'
    })
	;
});

// global routes for the application
/*
orator.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
	        when('/books', {
	            templateUrl: 'app/templates/books.html',
	            controller: 'BookTableController'
        }).
	        when('/book', {
	            templateUrl: 'app/templates/book.html',
	            controller: 'BookViewController'
        }).
	        when('/book/:id', {
	            templateUrl: 'app/templates/book.html',
	            controller: 'BookViewController'
        }).
            when('/sessions', {
                templateUrl: 'app/templates/sessions.html',
                controller: 'SessionTableController'
        }).
	        when('/session/:id', {
	            templateUrl: 'app/templates/session.html',
	            controller: 'SessionViewController'
	    }).
            otherwise({
                redirectTo: '/book'
        });
}]);
*/