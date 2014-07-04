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
            return window.helpers(myLabels, myWebLabels);
        },
        className: 'static'
    });
});