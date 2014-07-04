define([
    'backbone',
    'hbs!tmpl/commandsView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, CommandsTmpl, myLabels, myWebLabels) {
    'use strict';

    return Backbone.Marionette.ItemView.extend({

        template : CommandsTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },

        className: 'static'
    });
});