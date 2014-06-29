define([
    'backbone',
    'hbs!tmpl/commandsView_tmpl',
    'i18n!nls/labels'
],
function(Backbone, CommandsTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
        template: CommandsTmpl,
        className: 'static'
    });
});
