define([
    'backbone',
    'communicator',
    'views/gatewayPreView',
    'hbs!tmpl/gatewayCollectionView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, Communicator, GatewayView, GatewayCollectionTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.CompositeView.extend({
        itemView: GatewayView,
        itemViewContainer: 'tbody',
        templateHelpers: function(){
            console.log ("Loading template helpers and localization for the gateway collection view. If you don't see a list of gateways, this hasn't loaded properly.");
            return window.helpers(myLabels, myWebLabels);
        },
        template: GatewayCollectionTmpl
    });
});