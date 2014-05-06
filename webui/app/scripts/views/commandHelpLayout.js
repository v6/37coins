define([
    'backbone',
    'hbs!tmpl/commandHelpLayout_tmpl'
],
function(Backbone, HelpTmpl) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
        template: HelpTmpl,
        regions: {
            commands: '#smsCommands'
        },
        className: 'static',
        onShow:function () {
            this.$('.collapse').collapse({
                parent: '#accordion',
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