define([
    'backbone',
    'communicator',
    'hbs!tmpl/indexView_tmpl',
    'views/gatewayPreView'
],
function(Backbone, Communicator, IndexTmpl, GatewayView) {
    'use strict';
    return Backbone.Marionette.CompositeView.extend({
        itemView: GatewayView,
        itemViewContainer: '#gwTable',
        template: IndexTmpl,
        className: 'main',
        initialize: function() {
        }
    });
});