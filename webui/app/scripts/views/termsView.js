define([
	'backbone',
	'hbs!tmpl/termsView_tmpl',
	'i18n!nls/labels'
],
function(Backbone, TermsTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: TermsTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
        className: 'static'
    });
});