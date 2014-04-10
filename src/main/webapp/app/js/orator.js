// define the application
var orator = angular.module('orator', 
	[
	 'ngResource', 				// json/ajax services
	 'ngRoute', 				// application url router
	 'xeditable', 				// edit values in-place
	]
);

orator.run(function(editableOptions, editableThemes) {
	editableThemes.bs3.inputClass = 'input-sm';
	editableThemes.bs3.buttonsClass = 'btn-sm';
	editableOptions.theme = 'bs3'; // bootstrap3 theme
});

// global routes for the application
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