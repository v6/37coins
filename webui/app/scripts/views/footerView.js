define([
    'backbone',
    'hbs!tmpl/footerView_tmpl',
    'i18n!nls/labels'
],
function(Backbone, FooterTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: FooterTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
        className: 'container'
    });
});