define([
	'backbone'
],
function( Backbone) {
    'use strict';

	/* Return a model class definition */
	return Backbone.Model.extend({
		parse: function(response) {
            if (response){
				var lc = response.locale.substring(1,3).toLowerCase();
				response.mobile = window.formatLocal(lc,response.mobile);
				response.locale = lc;
				response.resPath = window.opt.resPath;
				response.lName = window.countryCodeToName(lc);
				response.fee = (response.fee * 1000000).toFixed(2); //Send fee in bitcoin from server to interface in Bits. Each bitcoin is one MIIIIIIILLILLLLLION Bits.
            }
            return response;
        }
    });
});
