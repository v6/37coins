define([
    'backbone',
    'communicator',
    'collections/gatewayCollection',
    'hbs!tmpl/accountHeaderView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function(Backbone, Communicator, GatewayCollection, AccountHeaderTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({

        template: AccountHeaderTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },

        className: 'static',
        initialize: function(opt){
            this.mobileString = opt.mobile;
            var self = this;
            if (!window.i18n){
                Communicator.mediator.on('app:init', function() {
                    self.init2(opt);
                });
            }else{
                this.init2(opt);
            }
        },
        init2: function(opt){
            this.phoneUtil = window.i18n.phonenumbers.PhoneNumberUtil.getInstance();
            var pnt = window.i18n.phonenumbers.PhoneNumberType;
            var type = null;
            try{
                this.mobile = this.phoneUtil.parseAndKeepRawInput('+'+this.mobileString,'ZZ');
                type = this.phoneUtil.getNumberType(this.mobile);
            }catch(err){}
            if (!this.mobile || !this.phoneUtil.isValidNumber(this.mobile)||
                (type!==pnt.MOBILE && type!==pnt.FIXED_LINE_OR_MOBILE)){
                console.dir(opt);
                console.log('error');
                return;
            }
            this.model = new Backbone.Model();
            this.model.bind('change', this.render);
            if (opt.gateways.length<1){
                opt.gateways.on('sync', this.itterate, this);
            }else{
                this.itterate(opt.gateways);
            }
        },
        itterate: function(gateways){
            var pnf = window.i18n.phonenumbers.PhoneNumberFormat;
            var rc = this.phoneUtil.getRegionCodeForNumber(this.mobile).toLowerCase();
            var self = this;
            gateways.each(function(gw){
                if (rc === gw.get('locale')){
                    var gwNumber = self.phoneUtil.parseAndKeepRawInput(gw.get('mobile'), gw.get('locale'));
                    var output = self.phoneUtil.format(gwNumber, pnf.NATIONAL);
                    self.model.set('mobile',output);
                }
            });
        }
    });
});