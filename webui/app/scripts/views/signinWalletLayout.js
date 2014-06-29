define([
    'backbone',
    'hbs!tmpl/signinWalletLayout_tmpl',
    'i18n!nls/labels',
],
function(Backbone, SigninTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: SigninTmpl,
        className: 'static',
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
        regions: {
            mobileInput: '#mobileInput'
        }
    });
});