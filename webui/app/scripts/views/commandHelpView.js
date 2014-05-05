define([
	'backbone',
	'hbs!tmpl/commandHelpView_tmpl'
],
function(Backbone, HelpTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: HelpTmpl,
        className: 'static'
    });
});