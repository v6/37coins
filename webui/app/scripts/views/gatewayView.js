define([
    'backbone',
    'hbs!tmpl/configView_tmpl',
    'i18n!nls/labels'
],
function(Backbone, ConfigTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
		template: ConfigTmpl,
		templateHelpers: function(){
			return window.helpers(myLabels);
		},
        className: 'gwLayout'
    });
});