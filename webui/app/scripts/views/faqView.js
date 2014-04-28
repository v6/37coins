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
        }
    });
});