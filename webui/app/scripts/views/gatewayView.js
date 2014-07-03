define([
    'backbone',
    'hbs!tmpl/configView_tmpl',
    'i18n!nls/labels',
     'i18n!nls/webLabels'
],
function(Backbone, ConfigTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
		template: ConfigTmpl,
		templateHelpers: function(){
			return window.helpers(myLabels, myWebLabels);
		},
        className: 'gwLayout'
    });
});