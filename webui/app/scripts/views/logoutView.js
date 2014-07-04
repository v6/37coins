define([
    'backbone',
    'communicator',
    'hbs!tmpl/logoutView_tmpl',
        'i18n!nls/labels',
        'i18n!nls/webLabels'
    ],

    function(Backbone, Communicator, LogoutTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: LogoutTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, webLabels);
        },
        className: 'container',
        initialize: function() {
            
        }
    });
});