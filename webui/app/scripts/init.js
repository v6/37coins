require.config({

    /* starting point for application */
    deps: ['backbone.marionette', 'bootstrap', 'main'],


    shim: {
        backbone: {
            deps: [
                'underscore',
                'jquery'
            ],
            exports: 'Backbone'
        },
        'socketio': {
            exports: 'io'
        },
        jqueryValidation: {
            deps: [
                'jquery'
            ]
        },
        bootstrap: {
            deps: ['jquery'],
            exports: 'jquery'
        },
        recaptcha: {
            exports: 'Recaptcha'
        },
        webfinger:{
            exports: 'webfinger'
        }
    },

    paths: {
        jquery: '../bower_components/jquery/dist/jquery',
        backbone: '../bower_components/backbone-amd/backbone',
        underscore: '../bower_components/underscore-amd/underscore',

        /* alias all marionette libs */
        'backbone.marionette': '../bower_components/backbone.marionette/lib/core/amd/backbone.marionette',
        'backbone.wreqr': '../bower_components/backbone.wreqr/lib/backbone.wreqr',
        'backbone.babysitter': '../bower_components/backbone.babysitter/lib/backbone.babysitter',
        'backbone.eventbinder': '../bower_components/backbone.eventbinder/lib/amd/backbone.eventbinder', // amd version
        routeFilter: '../bower_components/backbone-route-filter/backbone-route-filter',

        /* alias the bootstrap js lib */
        bootstrap: '../bower_components/sass-bootstrap/dist/js/bootstrap',

        /* Alias text.js for template loading and shortcut the templates dir to tmpl */
        text: '../bower_components/requirejs-text/text',
        tmpl: '../templates',

        /* handlebars from the require handlerbars plugin below */
        handlebars: '../bower_components/require-handlebars-plugin/Handlebars',

        /* require handlebars plugin - Alex Sexton */
        i18n: '../bower_components/requirejs-i18n/i18n',
        json2: '../bower_components/require-handlebars-plugin/hbs/json2',
        hbs: '../bower_components/require-handlebars-plugin/hbs',
        basicauth: '../bower_components/backbone.basicauth/backbone.basicauth',
        recaptcha: 'vendor/recaptcha_ajax',
        jqueryValidation: '../bower_components/jqueryValidation/dist/jquery.validate',
        EventEmitter: '../bower_components/event-emitter/dist/EventEmitter',
        GA: '../bower_components/requirejs-google-analytics/dist/GoogleAnalytics',
        webfinger: 'vendor/webfinger',
        socketio: 'vendor/socket.io',
        intlTelInput: '../bower_components/intl-tel-input/build/js/intlTelInput',
        userVoice: '//widget.uservoice.com/yvssgWHkFiUzGrxQUlrdxA'
    },
    waitSeconds: 0,
    hbs:{
        helpers: false
    }
});
window.getLocale = function(){
    'use strict';
    var dl = 'en';
    var ls = localStorage.getItem('locale');
    var lng = window.opt.lng;
    var locale = ((ls && ls!==null)?ls:((lng && lng.length>1)?lng.substring(0,2):dl));
    if (!locale || locale===null || locale.length!==2 || !(locale in window.activeLocales)){
        locale = dl;
        localStorage.setItem('locale',locale);
    }
    return locale;
};
require.config({
    baseUrl: window.opt.resPath+'/scripts',
    locale: window.getLocale(),
    config: {
        'GA': {
            'id' : window.opt.gaTrackingId
        }
    }
});
