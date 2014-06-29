define([
    'backbone',
    'hbs!tmpl/signupWalletLayout_tmpl',
    'i18n!nls/labels',
],
function(Backbone, SignupTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: SignupTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
        className: 'static',
        regions: {
            mobileInput: '#mobileInput'
        }
    });
});