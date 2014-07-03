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
		className: 'static',
        templateHelpers: function(){
            console.log ( "Loading template helpers for index page header" ) ; // DEBUGGING CODE
            return window.helpers(myLabels, webLabels);
        },
	    regions: {
	        mobileInput: '#mobileInput'
	    }
	});
});
