define([
	'backbone',
	'hbs!tmpl/headerView_tmpl'
],
function(Backbone, HeaderTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: HeaderTmpl,
        className: 'static'
    });
});