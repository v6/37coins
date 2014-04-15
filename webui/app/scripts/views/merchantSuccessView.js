define([
    'backbone',
    'hbs!tmpl/merchantSuccessView_tmpl'
],
function(Backbone, MerchantSuccessTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantSuccessTmpl,
        className: 'gwLayout',
        initialize: function(opt) {
            this.delivery = opt.delivery;
            this.deliveryParam = opt.deliveryParam;
            this.apiToken = opt.apiToken;
            this.apiSecret = opt.apiSecret;
            if (this.delivery === 'javascript'){
                window.parent.window.postMessage(
                    {'func':this.deliveryParam,'params':[{'apiToken':this.apiToken,'apiSecret':this.apiSecret}]},'*');
            }
        },

        onShow:function () {
            if (this.delivery==='display'){
                this.$('#instructions').html('Please copy and paste those parameters into your POS: <br> API-Token: '+this.apiToken+'<br> API-Secret: '+this.apiSecret);
            }else{
                this.$('#instructions').html('Configuration has been applied to POS automatically.');
            }
        }

    });
});