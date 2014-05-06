define([
    'backbone',
    'hbs!tmpl/faqView_tmpl'
],
function(Backbone, FaqTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: FaqTmpl,
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