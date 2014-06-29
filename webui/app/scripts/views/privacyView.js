define([
	'backbone',
	'hbs!tmpl/privacyView_tmpl',
	'i18n!nls/labels'
],
function(Backbone, PrivacyTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: PrivacyTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
        className: 'static'
    });
});