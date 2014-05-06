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
            this.$('.collapse').on('hidden.bs.collapse', function () {
                console.log('finished');
                if (location.hash){
                    self.$(location.hash).collapse('show');
                }
            });
        }
    });
});