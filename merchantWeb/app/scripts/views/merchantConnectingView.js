define([
    'backbone',
    'hbs!tmpl/merchantConnectingView_tmpl'
],
function(Backbone, MerchantConnectingTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantConnectingTmpl,
        className: 'gwLayout',
        initialize: function() {
            //init
        },
    
        events: {
            'click .close': 'handleClose',
        },
        onShow:function () {
            console.log('Web Merchant Connecting from view.');
        }
    });
});