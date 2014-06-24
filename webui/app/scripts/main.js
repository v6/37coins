require([
	'backbone',
	'application',
	'routers/pageController',
	'basicauth',
	'regionManager'
	
],
function ( Backbone, App, PageController ) {
    'use strict';
    window.transEvent = function(){
        var t;
        var el = document.createElement('fakeelement');
        var transitions = {
            'transition':'transitionend',
            'OTransition':'oTransitionEnd',
            'MozTransition':'transitionend',
            'WebkitTransition':'webkitTransitionEnd'
        };

        for(t in transitions){
            if( el.style[t] !== undefined ){
                return transitions[t];
            }
        }
    };
    window.helpers = function(labels){
        return {
            s: labels,
            up: function(str){
                return str.toUpperCase();
            }
        };
    };
    var options = {
        pageController: PageController
    };
	App.start(options);
});
