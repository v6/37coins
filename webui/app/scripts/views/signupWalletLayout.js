define([
    'backbone',
    'hbs!tmpl/signupWalletLayout_tmpl'
],
function(Backbone, SignupTmpl) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: SignupTmpl,
        className: 'static',
        regions: {
            mobileInput: '#mobileInput'
        }
    });
});