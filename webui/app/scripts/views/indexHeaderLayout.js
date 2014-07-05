define([
    'backbone',
    'communicator',
    'hbs!tmpl/indexHeaderLayout_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels',
    'intlTelInput'
],
function(Backbone, Communicator, IndexHeaderTmpl, myLabels, webLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({

		template: IndexHeaderTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, webLabels);
        },

		className: 'static',

	    regions: {
	        mobileInput: '#mobileInput'
	    }
	});
});
