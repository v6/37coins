define([
    'backbone',
    'hbs!tmpl/merchantDisconnectView_tmpl'
],
function(Backbone, MerchantDisconnectTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantDisconnectTmpl,
        className: 'gwLayout',
        initialize: function() {
            //init
        },
    
        events: {
            'click .close': 'handleClose',
        },
        onShow:function () {
            console.log('Web Merchant Disconnect from view.');
        }
    });
});