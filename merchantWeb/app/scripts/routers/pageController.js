define(['backbone',
    'communicator',
    'GA',
    'views/merchantBackView',
    'views/merchantLoginView',
    'models/chargeModel',
    'routeFilter',
    'socketio'
    ], function(Backbone, Communicator, GA, MerchantBackView, MerchantLoginView, ChargeModel) {
    'use strict';

    var Controller = {};

    // private module/app router  capture route and call start method of our controller
    Controller.Router = Backbone.Marionette.AppRouter.extend({
        initialize: function(opt){
            this.app = opt.app;
        },
        appRoutes: {
            '': 'showMerchantBack',
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

    Controller.showMerchantBack = function() {
        var model = new ChargeModel();
        var contentView = new MerchantBackView({model:model});
        Communicator.mediator.trigger('app:show',contentView);
    };

    return Controller;
});