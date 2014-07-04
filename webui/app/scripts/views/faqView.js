define([
    'backbone',
    'hbs!tmpl/faqView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, FaqTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({

        className: 'static',

        template: FaqTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },

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