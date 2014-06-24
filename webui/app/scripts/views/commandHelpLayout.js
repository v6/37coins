define([
    'backbone',
    'hbs!tmpl/commandHelpLayout_tmpl',
    'i18n!nls/labels'
],
function(Backbone, HelpTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: HelpTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
        regions: {
            commands: '#smsCommands'
        },
        className: 'static',
        onShow:function () {
            this.$('.collapse').collapse({
                parent: '#accordion',
                toggle: true
            });
            this.$('.collapse2').collapse({
                parent: '#accordion2',
                toggle: true
            });
            var self = this;
            this.$('.panel-collapse').on('hidden.bs.collapse', function () {
                if (location.hash){
                    self.$(location.hash).collapse('show');
                }
            });
        }
    });
});