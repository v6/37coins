define([
	'backbone',
	'communicator'
],
function(Backbone, Communicator) {
    'use strict';

	/* Return a model class definition */
	return Backbone.Model.extend({
		initialize: function(){
            var cred = sessionStorage.getItem('credentials');
            this.credentials = $.parseJSON(cred);
            this.on('change', function (model){
                sessionStorage.setItem('credentials',JSON.stringify(model.credentials));
            });
            var vent = Communicator.mediator;
            var self = this;
            vent.on('app:logout', function(){
                sessionStorage.clear();
                self.credentials = null;
                self.clear();
            });
        }
    });
});
