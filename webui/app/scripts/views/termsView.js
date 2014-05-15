define([
	'backbone',
	'hbs!tmpl/termsView_tmpl'
],
function(Backbone, TermsTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: TermsTmpl,
        className: 'static'
    });
});