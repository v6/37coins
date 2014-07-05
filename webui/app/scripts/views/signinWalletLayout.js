define([
    'backbone',
    'hbs!tmpl/signinWalletLayout_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels',
],
function(Backbone, SigninTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: SigninTmpl,
        className: 'static',
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },
        regions: {
            mobileInput: '#mobileInput'
        }
    });
});