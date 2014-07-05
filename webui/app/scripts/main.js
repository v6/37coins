require([
	'backbone',
	'application',
	'routers/pageController',
	'basicauth',
	'regionManager',
    'isoCountries'
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
    //prepare exchange rate
    $.get( 'https://api.bitcoinaverage.com/ticker/global/all', function( data ) {
        for (var rc in window.iso.countries){
            var cur = window.iso.countries[rc].currency;
            if (cur && cur!=='ALL' && data[cur]){
                window.iso.countries[rc].last = data[cur].last;
            }else{
                console.log('not found: '+ rc + ' '+cur);
            }
        }
    });
    window.helpers = function(labels, webLabels){
        return {
            s : labels,
            w : webLabels,
            up : function(str){
                if ( str === ('' || null ) ) {
                    console.log ( 'error, nothing to uppercase. That\'s why your view isn\'t loading.');
                } else {
                    return str.toUpperCase();
                }
            },
            sms: function(format){
                var args = Array.prototype.slice.call(arguments, 1);
                var offset = 0;
                var rv = format.replace(/{\d}{(\d+)}{\d}/g, function() {
                    offset += 1;
                    var factor = parseInt(window.opt.unitFactor.replace(/,/g, ''),10);
                    var amount = parseFloat(args[0]);
                    var btc = amount * ((factor)?factor:1000000);
                    var country = window.iso.countries[window.opt.country];
                    if (!country.last){
                        return btc + window.opt.unitName;
                    }
                    var fAmount = (country.last*amount).toFixed(2);
                    var fiat = '('+fAmount+country.currency+')';
                    return btc + window.opt.unitName + fiat;
                });
                rv = rv.replace(/{(\d+)}/g, function(match, number) {
                    var type = typeof args[number-offset];
                    return (type === 'string') ? labels.sms[args[number-offset]] : match;
                });
                return rv;
            },
            printf: function(format){
                var args = Array.prototype.slice.call(arguments, 1);
                return format.replace(/{(\d+)}/g, function(match, number) {
                    var type = typeof args[number];
                    return (type === 'string') ? args[number] : match;
                });
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
