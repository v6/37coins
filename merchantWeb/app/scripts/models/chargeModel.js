define(['backbone','communicator'], function(Backbone, Communicator) {
    'use strict';

    // private
    var ChargeModel = Backbone.Model.extend({
        url: function(){
            return window.opt.basePath+'/merchant/charge/'+sessionStorage.getItem('token');
        }
    });
    return ChargeModel;

});