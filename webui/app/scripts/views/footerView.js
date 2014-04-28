define([
	'backbone',
	'hbs!tmpl/footerView_tmpl'
],
function(Backbone, FooterTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: FooterTmpl,
        className: 'container'
    });
});