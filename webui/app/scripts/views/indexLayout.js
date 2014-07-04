define([
    'backbone',
    'communicator',
    'hbs!tmpl/indexLayout_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, Communicator, IndexLayout, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({

        regions: {
            commands: '#smsCommands',
            gateways: '#gwTable'
        },

        template: IndexLayout,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },

        className: 'main',
        initialize: function() {
        },

        events: {
            'click #smsSignup': 'handleJoin',
        },

        handleJoin: function(e){
            e.preventDefault();
            $('html, body').animate({ scrollTop: 0 }, 'slow');
            $('[name="search"]').focus();
        }
    });
});