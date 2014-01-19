define([
	'backbone'
],
function( Backbone) {
    'use strict';

	/* Return a model class definition */
	return Backbone.Model.extend({
		initialize: function() {
			console.log('initialize a Gateway model');
		},
		parse: function(response) {
            if (response){
				var lc = response.locale.substring(1,3).toLowerCase();
				response.mobile = window.formatLocal(lc,response.mobile);
				response.locale = lc;
				response.resPath = window.opt.resPath;
				response.lName = window.countryCodeToName(lc);
				response.fee = (response.fee * 1000).toFixed(2);
            }
            return response;
        }
    });
});
