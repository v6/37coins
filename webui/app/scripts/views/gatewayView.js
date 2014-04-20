define([
    'backbone',
    'hbs!tmpl/configView_tmpl'
],
function(Backbone, ConfigTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
	template: ConfigTmpl,
        className: 'gwLayout'
    });
});