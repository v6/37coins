define([
	'backbone',
	'hbs!tmpl/accountHeadlineView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, HeadlineTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({

    template: HeadlineTmpl,
    templateHelpers: function(){
        return window.helpers(myLabels, myWebLabels);
    },

	className: 'static'
    });
});