define([
	'backbone',
	'communicator',
	'views/headerView',
	'views/footerView',
    'views/merchantChargeView',
    'views/merchantThanksView'
],

function( Backbone, Communicator, HeaderView, FooterView, MerchantChargeView, MerchantThanksView) {
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

    Communicator.mediator.on('app:logout', function() {
        //renavigate to see if we still have permission
        App.router.navigate('logout',{trigger: true});
    });

    Communicator.mediator.on('app:message', function(data) {
        console.dir(data);
        if (data.action==='charge'){
            var chargeView = new MerchantChargeView({model:new Backbone.Model(data)});
            App.content.show(chargeView);
        }
        if (data.action==='payed'){
            var thxView = new MerchantThanksView();
            App.content.show(thxView);
        }
        if (data.action==='login'){
            App.next();
            App.next = undefined;
        }
        if (data.action==='failed'){
            //tell the login view
        }
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

        App.header.show(new HeaderView({model:new Backbone.Model({resPath:window.opt.resPath})}));
        App.footer.show(new FooterView());
        this.router = new options.pageController.Router({
            controller: options.pageController, // wire-up the start method
            app:App
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
