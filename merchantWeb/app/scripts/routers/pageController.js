define(['backbone',
    'communicator',
    'GA',
    'views/merchantFrontView',
    'views/merchantBackView',
    'views/merchantLoginView',
    'routeFilter',
    'socketio'
    ], function(Backbone, Communicator, GA, MerchantFrontView, MerchantBackView, MerchantLoginView) {
    'use strict';

    var Controller = {};

    // private module/app router  capture route and call start method of our controller
    Controller.Router = Backbone.Marionette.AppRouter.extend({
        initialize: function(opt){
            this.app = opt.app;
        },
        appRoutes: {
	    '': 'showMerchantFront',
	    'front': 'showMerchantFront',
	    'back': 'showMerchantBack'
        },
        before:{
            '*any': function(fragment, args, next){
                //set title
                if (fragment){
                    $(document).attr('title', '37 Coins - ' + fragment);
                }else {
                    $(document).attr('title', '37 Coins');
                }
                //set meta tag
                $('meta[name=description]').remove();
                $('head').append( '<meta name="description" content="this is new">' );
                //track page visit
                GA.view(fragment);
                next();
            }
        }
    });

    Controller.showMerchantFront = function() {
        var contentView = new MerchantFrontView();
        Communicator.mediator.trigger('app:show',contentView);
    };

    Controller.showMerchantBack = function() {
	var contentView = new MerchantBackView();
	Communicator.mediator.trigger('app:show',contentView);
    };

    return Controller;
});