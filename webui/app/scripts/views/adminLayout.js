define([
    'backbone',
    'hbs!tmpl/adminLayout_tmpl',
    'i18n!nls/labels'
],
function(Backbone, AdminLayout, myLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: AdminLayout,
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
        regions: {
            account: '#accountView'
        }
    });
});