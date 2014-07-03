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
    window.helpers = function(labels, webLabels){
        return {
            s : labels,
            w : webLabels,
            up : function(str){
                if ( str === ("" || null ) ) {
                    console.log ( "error, nothing to uppercase. That's why your view isn't loading.")
                } else {
                    return str.toUpperCase();
                }
            },
            resPath : window.opt.resPath,
            l : window.getLocale()
        };
    };
    var options = {
        pageController: PageController
    };
	App.start(options);
});
