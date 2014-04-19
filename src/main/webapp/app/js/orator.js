// define the application
var orator = angular.module('orator', 
	[
	 'ui.router',				// enhanced url router
	 'ngResource', 				// json/ajax services
	 'xeditable', 				// edit values in-place
	 'angularFileUpload',		// file upload
	 'mediaPlayer',				// plays audio (and video, but no need)
	 'ui.sortable',			  	// dragable sortable file list
	 'ui.knob'					// ui visible knob/progress
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
	$urlRouterProvider.otherwise("/sessions");
	
	// set up routes
	$stateProvider
	.state('sessions', {
		url: '/sessions',
		templateUrl: 'app/templates/sessions.html',
		controller: 'SessionTableController'
	})
	.state('session', {
        url: '/session/:sessionId',
        templateUrl: 'app/templates/session.html',
        controller: 'SessionViewController'
    })
    // not sure if we need this state forever... but it's here
    // until we can get the parent/child state to work correctly...
    .state('start-session', {
        url: '/start-session/:bookId',
        templateUrl: 'app/templates/session.html',
        controller: 'SessionViewController'
    })
	.state('books', {
		url: '/books',
		templateUrl: 'app/templates/books.html',
		controller: 'BookTableController'
	})
	.state('book', {
        url: '/book/:bookId',
        templateUrl: 'app/templates/book.html',
        controller: 'BookViewController'
    })
    // not sure if we need this state forever... but it's here
    // until we can get the parent/child state to work correctly...
    .state('new-book', {
        url: '/book',
        templateUrl: 'app/templates/book.html',
        controller: 'BookViewController'
    })
	;
});