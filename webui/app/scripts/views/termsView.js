define([
	'backbone',
	'hbs!tmpl/termsView_tmpl',
	'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, TermsTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: TermsTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },
        className: 'static'
    });
});