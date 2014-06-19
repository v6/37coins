define([
    'backbone',
    'communicator',
    'hbs!tmpl/indexLayout_tmpl',
    'i18n!nls/labels'
],
function(Backbone, Communicator, IndexLayout, myLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        regions: {
            commands: '#smsCommands',
            gateways: '#gwTable'
        },
        template: IndexLayout,
        templateHelpers: function(){return {s: myLabels};},
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