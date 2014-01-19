define([
	'backbone',
	'communicator',
	'hbs!tmpl/gatewayPreView_tmpl'
],
function(Backbone, Communicator, GatewayTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: GatewayTmpl,
        tagName: 'tr itemscope itemtype="http://schema.org/ServiceChannel"',
        initialize: function() {
            
        },
        onShow: function(){
            var self = this;
            $.get(window.opt.basePath+'/data/serviceQuality/'+this.model.get('id'),function(data){
                if (data.availability>0.98 && data.transactionCount>20){
                    self.$('#rating'+data.cn).append('<i class="fa fa-star"></i>');
                }else if(data.availability>0.95 && data.transactionCount>10){
                    self.$('#rating'+data.cn).append('<i class="fa fa-star-half-o"></i>');
                }else{
                    self.$('#rating'+data.cn).append('<i class="fa fa-star-o"></i>');
                }
            },'json');
        }
    });
});