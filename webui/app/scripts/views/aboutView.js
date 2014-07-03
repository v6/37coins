define([
	'backbone',
	'hbs!tmpl/aboutView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, AboutTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: AboutTmpl,
        className: 'static',
        templateHelpers: function(){
            console.log ( self , "Loading Helpers and Labels" ); // DEBUGGING CODE
            return window.helpers(myLabels, myWebLabels);
        }
    });
});