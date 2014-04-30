define([
	'backbone',
	'hbs!tmpl/signupWallet_tmpl'
],
function(Backbone, AboutTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: AboutTmpl,
        className: 'static'
    });
});