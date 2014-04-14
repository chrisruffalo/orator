// calls utility function for converting a byte value into
// a "pretty" byte value
orator.filter('orator.bytes', function() {
   return function(bytes) {
	   return pretty.bytes(bytes);
   };
});

orator.filter('orator.bps', function() {
   return function(bitsPerSecond) {
	   return pretty.bps(bitsPerSecond);
   };
});

orator.filter('orator.seconds', function() {
   return function(seconds) {
	   return pretty.seconds(seconds);
   };
});