define([
	'backbone',
	'hbs!tmpl/signupWalletView_tmpl'
],
function(Backbone, SignupTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: SignupTmpl,
        className: 'static'
    });
});