define([
    'backbone',
    'communicator',
    'views/gatewayPreView',
    'hbs!tmpl/gatewayCollectionView_tmpl',
    'i18n!nls/labels'
],
function(Backbone, Communicator, GatewayView, GatewayCollectionTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.CompositeView.extend({
        itemView: GatewayView,
        itemViewContainer: 'tbody',
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
        template: GatewayCollectionTmpl
    });
});