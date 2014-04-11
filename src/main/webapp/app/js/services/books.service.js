// resource (REST Service) for use in other parts of the application
orator.factory("Books", function ($resource) {
    return $resource('services/secured/books/all', [],
    		{ 
				// query/info (no param)
				'query':  {method:'GET', isArray:true},
    
    			// get and create
    			'save': {method:'PUT', url: "services/secured/books/save"},
    			'get': {method:'GET', url: "services/secured/books/:bookId", params: {bookId:"@id"}}
			}
    );    
});