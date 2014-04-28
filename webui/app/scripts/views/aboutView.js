define([
	'backbone',
	'hbs!tmpl/aboutView_tmpl'
],
function(Backbone, AboutTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: AboutTmpl,
        className: 'static'
    });
});