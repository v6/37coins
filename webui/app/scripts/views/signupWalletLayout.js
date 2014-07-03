define([
    'backbone',
    'hbs!tmpl/signupWalletLayout_tmpl',
    'i18n!nls/labels',
     'i18n!nls/webLabels'
],
function(Backbone, SignupTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: SignupTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },
        className: 'static',
        regions: {
            mobileInput: '#mobileInput'
        }
    });
});