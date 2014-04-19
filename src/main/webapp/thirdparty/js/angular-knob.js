angular.module('ui.knob', [])
  .directive('knob', function () {
    return {
      restrict: 'EACM',
      template: function(elem, attrs){

        return '<input value="{{ knob }}">';

      },
      replace: true,
      scope: true,
      link: function (scope, elem, attrs) {

        scope.knob = scope.$eval(attrs.knobData);

        var renderKnob = function(){

          scope.knob = scope.$eval(attrs.knobData);

          var opts = {}; 
          if(!angular.isUndefined(attrs.knobOptions)){
            opts = scope.$eval(attrs.knobOptions);
          }

          if(!angular.isUndefined(attrs.knobMax)){
            var max = scope.$eval(attrs.knobMax);
            if(!angular.isUndefined(max)){

              opts.max = max;
            
            }
          }
          
          // set values
          $elem = $(elem);
          $elem.val(scope.knob);
          $elem.change();
          $elem.knob(opts);
          
          // add a class to key things off of
          if($elem.parent()) {
        	  var parent = $elem.parent();
        	  
        	  parent.addClass('angular-knob-container');
        	  parent.css('display', ''); // this is causing a bug

        	  // bind resize events so that the (grand)parent height commutes to the child
              if(!scope.resizeEvent) {
            	  scope.resizeEvent = parent.resize(function(){
            		  if(parent.parent()) {
            			  var grandparent = parent.parent();
            			  
            			  // find the smaller direction
            			  var radius = grandparent.height();
            			  if(radius > grandparent.width()) {
            				  radius = grandparent.width();
            			  }
            			  
            			  // leave 15% around the edges to make it look good
            			  radius = radius * 0.70;
            			  
            			  // bind heights
            			  parent.width(radius);
            			  parent.height(radius);
            		  
                		  // inner canvas too
            			  var canvas = parent.find("canvas");
            			  canvas.width(radius);
            			  canvas.height(radius);
            			  
            			  // wrap canvas in plain div
            			  var wrapper = canvas.wrap('');
            			  wrapper.css('margin-top', ((grandparent.height() - radius) * 0.5) + "px");
            			  wrapper.css('margin-left', ((grandparent.width() - radius) * 0.5) + "px");
            			  
            			  // update width options
            			  opts.width = radius;
            		  }            		  
            	  }); 
              }
              
              // pop resize
              parent.resize();
          }
          
          // add a class for local handling
          $elem.addClass("angular-knob-input");
          
          // hide?
          if($elem.parent() && opts.hidden) {
        	  parent.hide();
          } else if($elem.parent()){
        	  parent.show();
          }
        };

        scope.$watch(attrs.knobData, function () {
        	renderKnob();
        });

        scope.$watch(attrs.knobOptions, function () {
          renderKnob();
        }, true);

      }
    };
  });