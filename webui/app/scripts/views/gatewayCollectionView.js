define([
    'backbone',
    'communicator',
    'views/gatewayPreView',
    'hbs!tmpl/gatewayCollectionView_tmpl'
],
function(Backbone, Communicator, GatewayView, GatewayCollectionTmpl) {
    'use strict';
    return Backbone.Marionette.CompositeView.extend({
	itemView: GatewayView,
	itemViewContainer: 'tbody',
	template: GatewayCollectionTmpl
    });
});