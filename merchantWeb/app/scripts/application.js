define([
	'backbone',
	'communicator',
    'views/merchantFrontView',
    'views/merchantChargeView',
    'views/merchantThanksView',
    'views/merchantLoginView',
    'views/merchantConnectingView',
    'views/merchantDisconnectView',
    'socketio'
],

function( Backbone, Communicator, MerchantFrontView, MerchantChargeView, MerchantThanksView, MerchantLoginView, MerchantConnectingView, MerchantDisconnectView) {
    'use strict';

	var App = new Backbone.Marionette.Application();

    // these regions correspond to #ID's in the index.html 
    App.addRegions({
        header: '#header',
        content: '#content',
        footer: '#footer'
    });

    // marionette app events...
    App.on('initialize:after', function() {
        if (Backbone.history){
            if (!Backbone.history.start({pushState: true})) {
                console.log('notFound');
                App.router.navigate('notFound', {trigger: true});
            }
        }
    });

	Communicator.mediator.on('app:show', function(appView) {
        App.content.show(appView);
    });

    Communicator.mediator.on('app:authenticate', function(phoneNumber, tan, next) {
        var obj = { '@class' : 'com._37coins.web.Subscribe',
            'phoneNumber' : phoneNumber,
            'tan' : tan
        };
        App.next = next;
        App.socketio.json.send(obj);
        console.log('initializing authentication...');
    });

	/* Add initializers here */
	App.addInitializer( function (options) {

        this.router = new options.pageController.Router({
            controller: options.pageController, // wire-up the start method
            app:App
        });

        App.reconnect = function(){
            var sessionToken = (!sessionStorage.getItem('sessionToken')||sessionStorage.getItem('sessionToken')==='undefined')?undefined:sessionStorage.getItem('sessionToken');
            if (!sessionToken){
                var view = new MerchantLoginView();
                Communicator.mediator.trigger('app:show', view);
            }else{
                App.content.show(new MerchantFrontView());
                var obj = { '@class' : 'com._37coins.web.Subscribe',
                    'sessionToken' : sessionToken,
                    'action' : 'getState'
                };
                socket.json.send(obj);
            }
        };
        var self = this;
        var socket = io.connect(window.opt.basePath.split(':8')[0]+':8081');
        App.socketio = socket;
        socket.on('message', function (data) {
            console.dir(data);
            if (data.action==='charge'){
                var chargeView = new MerchantChargeView({model:new Backbone.Model(data)});
                App.content.show(chargeView);
            }
            if (data.action==='address'){
                Communicator.mediator.trigger('app:address',data.address);
            }
            if (data.action==='payed'){
                var thxView = new MerchantThanksView();
                App.content.show(thxView);
                setTimeout(function () {
                    App.content.show(new MerchantFrontView());
                }, 5000);
            }
            if (data.action==='login'){
                sessionStorage.setItem('sessionToken',data.sessionToken);
                var frontView = new MerchantFrontView();
                App.content.show(frontView);
            }
            if (data.action==='failed'){
                sessionStorage.clear();
                Communicator.mediator.trigger('app:failed');
            }
        });
        socket.on('connecting', function () {
            App.content.show(new MerchantConnectingView());
        });
        socket.on('reconnecting', function () {
            App.content.show(new MerchantConnectingView());
        });
        socket.on('disconnect', function () {
            App.content.show(new MerchantDisconnectView());
            socket.socket.reconnect();
        });
        socket.on('error', function () {
            socket.socket.reconnect();
        });
        socket.on('reconnect', function () {
            //self.reconnect();
        });
        socket.on('connect', function () {
            self.reconnect();
        });

        // Use delegation to avoid initial DOM selection and allow all matching elements to bubble
        $(document).delegate('a', 'click', function(evt) {
            // Get the anchor href and protcol
            var href = $(this).attr('href');
            // Ensure the protocol is not part of URL, meaning its relative. Stop the event bubbling to ensure the link will not cause a page refresh.
            if (href.indexOf('http://') === -1 && href.indexOf('https://') === -1 && href.indexOf('bitcoin:') === -1) {
                evt.preventDefault();
                // Note by using Backbone.history.navigate, router events will not be triggered.  If this is a problem, change this to navigate on your router.
                App.router.navigate(href, true);
            }
        });
	});

	return App;
});
