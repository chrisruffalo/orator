// resource (REST Service) for use in other parts of the application
orator.factory("Session", function ($resource) {
    return $resource('services/secured/reading/sessions', [],
    		{ 
				// get single session
    			'get':    {method:'GET', url: 'services/secured/reading/:sessionId', params: {sessionId:""}},
				
    			// start a new session
    			'start': {method:'GET', url: 'services/secured/reading/:bookId/start', params: {bookId:""}},
    			
				// query/info (no param)
				'query':  {method:'GET', isArray:true}
			}
    );    
});