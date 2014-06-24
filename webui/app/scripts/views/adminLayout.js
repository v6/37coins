define([
	'backbone',
	'hbs!tmpl/adminLayout_tmpl'
],
function(Backbone, AdminLayout) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
	    template: AdminLayout,
	    regions: {
	        account: '#accountView'
	    }
    });
});