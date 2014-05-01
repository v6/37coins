define([
    'backbone',
    'hbs!tmpl/signinWalletLayout_tmpl'
],
function(Backbone, SigninTmpl) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: SigninTmpl,
        className: 'static',
        regions: {
            mobileInput: '#mobileInput'
        }
    });
});