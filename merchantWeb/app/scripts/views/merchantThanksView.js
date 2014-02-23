define([
    'backbone',
    'hbs!tmpl/merchantThanksView_tmpl'
],
function(Backbone, MerchantThanksTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantThanksTmpl,
        className: 'gwLayout',
        initialize: function() {
            //init
        },
    
        events: {
            'click .close': 'handleClose',
        },
        onShow:function () {
            console.log('Web Merchant Thanks view.');
        }
    });
});