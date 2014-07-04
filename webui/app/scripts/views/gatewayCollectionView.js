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

        template: GatewayCollectionTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        }

    });
});