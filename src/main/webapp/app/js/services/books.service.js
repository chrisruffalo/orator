// resource (REST Service) for use in other parts of the application
orator.factory("Session", function ($resource) {
    return $resource('services/secured/books/all', [],
    		{ 
				// query/info (no param)
				'query':  {method:'GET', isArray:true}
			}
    );    
});