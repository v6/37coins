define([
    'backbone',
    'communicator',
    'hbs!tmpl/merchantChargeView_tmpl',
    'qrcode'
],
function(Backbone, Communicator, MerchantChargeTmpl, QRCode) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantChargeTmpl,
        className: 'gwLayout',
        initialize: function() {
            var self = this;
            Communicator.mediator.on('app:address', function(address) {
                self.onAddress(address);
            });
        },
        onAddress: function(address){
            new QRCode(this.$('#qrcode')[0], 'bitcoin:'+address+'?amount='+this.model.get('amount'));
        },
        onShow:function () {
            if (this.model.get('address')){
                this.onAddress(this.model.get('address'));
            }
            console.log('Web Merchant Charge view.');
        }
    });
});