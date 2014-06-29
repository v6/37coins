define([
    'backbone',
    'communicator',
    'hbs!tmpl/indexHeaderLayout_tmpl',
    'i18n!nls/labels',
    'intlTelInput'
],
function(Backbone, Communicator, IndexHeaderTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
		template: IndexHeaderTmpl,
		className: 'static',
        templateHelpers: function(){
            return window.helpers(myLabels);
        },
	    regions: {
	        mobileInput: '#mobileInput'
	    }
	});
});
