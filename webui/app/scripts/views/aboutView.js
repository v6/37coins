define([
	'backbone',
	'hbs!tmpl/aboutView_tmpl',
	'i18n!nls/labels'
],
function(Backbone, AboutTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: AboutTmpl,
        className: 'static',
        templateHelpers: function(){return {l: myLabels};}
    });
});