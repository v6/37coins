require([
    'backbone',
    'application',
    'routers/pageController',
    'basicauth',
    'regionManager',
    'isoCountries'
],
function ( Backbone, App, PageController) {
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
                if (!str) {
                    console.log ( 'error, nothing to uppercase. That\'s why your view isn\'t loading.');
                } else {
                    return str.toUpperCase();
                }
            },
            link : function(text, options) {
                var holder;
                if (text instanceof Array && text.length === 2){
                    holder = text[0];
                    text = text[1];
                }
                var attrs = [];
                for(var prop in options.hash) {
                    if (prop==='href' && options.hash[prop].indexOf('http') !== 0){
                        attrs.push(prop + '="/' + window.getLocale() + options.hash[prop] + '"');
                    }else{
                        attrs.push(prop + '="' + options.hash[prop] + '"');
                    }
                }
                var rv = '<a ' + attrs.join(' ') + '>' + text + '</a>';
                if (holder){
                    return window.helpers().printf(holder,rv,{});
                }else{
                    return rv;
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
            printf: function(){
                var options = arguments[arguments.length-1];
                var arg = arguments[1];
                if (arg && arg !== options && typeof arg === 'string' && arg.indexOf('.')!==-1 && arg.indexOf(':')===-1){
                    var obj = webLabels;
                    var tokens = arg.split('.');
                    for (var i = 0; i < tokens.length; i++){
                        obj = obj[tokens[i]];
                    }
                    var tmp = arguments[0];
                    arguments[0] = obj;
                    arguments[1] = tmp;
                }
                var args = Array.prototype.slice.call(arguments, 1);
                return arguments[0].replace(/{(\d+)}/g, function(match, number) {
                    return (args[number] && args[number] !== options) ? args[number] : match;
                });
            },
            resPath : window.opt.resPath,
            l : window.getLocale(),
            unitName: window.opt.unitName
        };
    };
    var options = {
        pageController: PageController
    };
    App.start(options);
});
