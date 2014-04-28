define([
	'backbone',
	'hbs!tmpl/headerSendView_tmpl'
],
function(Backbone, HeaderSendTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: HeaderSendTmpl,
        className: 'static'
    });
});