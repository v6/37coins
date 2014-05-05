define([
    'backbone',
    'hbs!tmpl/commandsView_tmpl'
],
function(Backbone, CommandsTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
	template: CommandsTmpl,
	className: 'static'
    });
});
