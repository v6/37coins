define([
    'backbone',
    'hbs!tmpl/gatewayLayout_tmpl'
],
function( Backbone, GatewayLayout) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: GatewayLayout,

        regions: {
            bal: '#balView',
            fee: '#feeView',
            conf: '#confView'
        },
        onShow:function () {
                //for display in android
            if (window.Android){
                window.Android.loadComplete();
                this.$('div#confView').remove();
            }
        }
    });
});