define([
    'backbone',
    'models/accountModel',
    'communicator'
],
function( Backbone, AccountModel, Communicator) {
    'use strict';
    return Backbone.Collection.extend({
        url: window.opt.basePath+'/api/gateway/admin/accounts',
        model: AccountModel,
        search: function(filter){
            var cred = sessionStorage.getItem('credentials');
            this.credentials = $.parseJSON(cred);
            if (filter.length>2){
                this.url = this.base + '?filter=' + encodeURIComponent(filter);
            }
            var fetch = this.fetch();
            this.url = this.base;
            return fetch;
        },
        initialize: function(){
            this.base = this.url;
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
