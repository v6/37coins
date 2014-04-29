define([
	'backbone',
	'hbs!tmpl/accountHeadlineView_tmpl'
],
function(Backbone, HeadlineTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
	template: HeadlineTmpl,
	className: 'static'
    });
});