define([
    'backbone',
    'hbs!tmpl/merchantFrontView_tmpl'
],
function(Backbone, MerchantFrontTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantFrontTmpl,
        className: 'gwLayout',
        initialize: function() {
            //init
        },
    
        events: {
            'click .close': 'handleClose',
        },
        onShow:function () {
            console.log('Web Merchant from view.');
        }
    });
});