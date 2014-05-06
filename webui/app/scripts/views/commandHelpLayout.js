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
			if (location.hash){
                this.$(location.hash).collapse('show');
            }
        }
    });
});