define([
	'backbone',
	'hbs!tmpl/privacyView_tmpl'
],
function(Backbone, PrivacyTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: PrivacyTmpl,
        className: 'static'
    });
});