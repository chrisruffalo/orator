// resource (REST Service) for use in other parts of the application
orator.factory("Sessions", function ($resource) {
    return $resource('services/secured/reading/sessions', [],
    		{ 
				// get single session
    			'get':    {method:'GET', url: 'services/secured/reading/:sessionId', params: {sessionId:"@id"}},
				
    			// start a new session
    			'start': {method:'GET', url: 'services/secured/reading/:bookId/start', params: {bookId:"@id"}},
    			
				// query/info (no param)
				'query':  {method:'GET', isArray:true},
				
				// delete
    			'deleteSession':  {method:'DELETE', isArray:true, url: 'services/secured/reading/:sessionId/delete', params: {sessionId:"@id"}}
			}
    );    
});