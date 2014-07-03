define([
	'backbone',
	'communicator',
	'hbs!tmpl/notFoundView_tmpl',
	'i18n!nls/labels'
],
function(Backbone, Communicator, NotFoundTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: NotFoundTmpl,
        className: 'container',
		templateHelpers: function(){
			return window.helpers(myLabels);
		},
        initialize: function() {
            
        }
    });
});