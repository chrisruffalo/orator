var pretty = {
		'bytes': function bytesToSize(bytes) {
			var sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
			if (bytes == 0) return '0 B';
			var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
			return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];
		},
		'seconds': function secondsToPretty(seconds) {
			if(seconds < 0 || !util.isNumber(seconds)) {
				seconds = 0;
			}
			
			// from http://www.neowin.net/forum/topic/817666-javascriptconvert-seconds-to-days-hours-minutes-and-seconds/
			var numhours = Math.floor(seconds / 3600);
			var numminutes = Math.floor(((seconds % 86400) % 3600) / 60);
			var numseconds = ((seconds % 86400) % 3600) % 60;

			var output = numseconds + "s";
			if(numseconds < 10) {
				output = "0" + output;
			}
			if(numminutes > 0 || numhours > 0) {
				output = numminutes + "m " + output;
				if(numminutes < 10) {
					output = "0" + output;
				}
			}
			if(numhours > 0) {
				output = numhours + "h " + output;
				if(numhours < 10) {
					output = "0" + output;
				}
			}
			
			return output;
		},
		'secondsWithDays': function secondsToPretty(seconds) {
			if(seconds < 0 || !util.isNumber(seconds)) {
				seconds = 0;
			}
			
			// from http://www.neowin.net/forum/topic/817666-javascriptconvert-seconds-to-days-hours-minutes-and-seconds/
			var numdays = Math.floor(seconds / 86400);
			var numhours = Math.floor((seconds % 86400) / 3600);
			var numminutes = Math.floor(((seconds % 86400) % 3600) / 60);
			var numseconds = ((seconds % 86400) % 3600) % 60;

			var output = numseconds + "s";
			if(numseconds < 10) {
				output = "0" + output;
			}
			if(numminutes > 0 || (numhours + numdays > 0)) {
				output = numminutes + "m " + output;
				if(numminutes < 10) {
					output = "0" + output;
				}
			}
			if(numhours > 0 || numdays > 0) {
				output = numhours + "h " + output;
				if(numhours < 10) {
					output = "0" + output;
				}
			}
			if(numdays > 0) {
				output = numdays + "d " + output;
				if(numdays < 10) {
					output = "0" + output;
				}
			}
			
			return output;
		}, 
		'bps': function bitsPerSecond(bitsPerSecond) {
			var sizes = ['bps', 'kbps', 'mbps', 'gbps', 'tbps'];
			var index = 0;
			var factor = 1024;
			while(bitsPerSecond > factor) {
				bitsPerSecond = bitsPerSecond/factor;
				index++;
			}
			bitsPerSecond = Math.floor(bitsPerSecond);
			return bitsPerSecond + "" + sizes[index];
		}
};
