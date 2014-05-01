define([
    'backbone',
    'communicator',
    'hbs!tmpl/indexHeaderLayout_tmpl',
    'intlTelInput'
],
function(Backbone, Communicator, IndexHeaderTmpl) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
		template: IndexHeaderTmpl,
		className: 'static',

	    regions: {
	        mobileInput: '#mobileInput'
	    }
	});
});
