define([
	'backbone',
	'hbs!tmpl/privacyView_tmpl',
	'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, PrivacyTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: PrivacyTmpl,
        templateHelpers: function(){
            console.log ("loading helpers for privacy view"); // DEBUGGING CODE
            return window.helpers(myLabels, myWebLabels);
        },
        className: 'static'
    });
});