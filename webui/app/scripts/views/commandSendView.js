define([
	'backbone',
	'hbs!tmpl/commandSendView_tmpl'
],
function(Backbone, SendTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: SendTmpl,
        className: 'static'
    });
});