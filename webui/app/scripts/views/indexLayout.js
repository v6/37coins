define([
    'backbone',
    'communicator',
    'hbs!tmpl/indexLayout_tmpl'
],
function(Backbone, Communicator, IndexLayout) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
	regions: {
	    commands: '#smsCommands',
	    gateways: '#gwTable'
	},
	template: IndexLayout,
	className: 'main',
	initialize: function() {
	}
    });
});