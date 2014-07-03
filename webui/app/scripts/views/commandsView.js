define([
    'backbone',
    'hbs!tmpl/commandsView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, CommandsTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        templateHelpers: function(){
            console.log ( self , "Loading Helpers and Labels" ); // DEBUGGING CODE
            return window.helpers(myLabels, myWebLabels);
        },
        template: CommandsTmpl,
        className: 'static'
    });
});