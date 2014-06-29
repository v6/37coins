define([
    'backbone',
    'hbs!tmpl/faqView_tmpl',
    'i18n!nls/labels'
],
function(Backbone, FaqTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: FaqTmpl,
        className: 'static',
        templateHelpers: function(){
            return window.helpers(myLabels);
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