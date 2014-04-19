// resource (REST Service) for use in other parts of the application
orator.factory("Books", function ($resource) {
    return $resource('services/secured/books/all', [],
    		{ 
				// query/info (no param)
				'query':  {method:'GET', isArray:true},
    
    			// get and create
    			'save': {method:'PUT', url: "services/secured/books/save?updateTracks=:updateTracks", params: {updateTracks: true}},
    			'get': {method:'GET', url: "services/secured/books/:bookId", params: {bookId:"@id"}},
    			
    			// delete
    			'deleteBook':  {method: 'DELETE', isArray:true, url: "services/secured/books/:bookId/delete", params: {bookId: "@id"}},
    			'deleteTrack': {method: 'DELETE', url: "services/secured/books/:bookId/deleteTrack/:trackId", params: {bookId: "", trackId: ""}},
    			'deleteCover': {method: 'DELETE', url: "services/secured/books/:bookId/cover/delete", params: {bookId: "@id"}}
			}
    );    
});