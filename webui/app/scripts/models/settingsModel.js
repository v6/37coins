define(['backbone','communicator'], function(Backbone, Communicator) {
    'use strict';

    return Backbone.Model.extend({
        url: window.opt.basePath+'/api/gateway/settings',
        initialize: function(){
            var cred = sessionStorage.getItem('credentials');
            this.credentials = $.parseJSON(cred);
            var vent = Communicator.mediator;
            var self = this;
            vent.on('app:logout', function(){
                self.credentials = null;
                self.clear();
            });
        }
    });

});