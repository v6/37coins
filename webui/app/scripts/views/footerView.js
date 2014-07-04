define([
    'backbone',
    'hbs!tmpl/footerView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, FooterTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({

        template: FooterTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },

        className: 'container'
    });
});