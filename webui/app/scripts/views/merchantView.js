define([
    'backbone',
    'hbs!tmpl/merchantView_tmpl',
    'socketio',
    'communicator',
    'views/merchantSuccessView'
],
function(Backbone, MerchantTmpl, io, Communicator, MerchantSuccessView) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantTmpl,
        className: 'gwLayout',
        initialize: function(opt) {
            this.ticket = opt.ticket;
            this.app = opt.app;
            var self = this;
            Communicator.mediator.on('app:init', function() {
                window.lpnLoaded = true;
                self.onShow();
            });
            Communicator.mediator.on('app:message', function(data) {
                console.dir(data);
                if (data.action === 'started'){
                    self.$('#disabledInput').val(data.sessionToken);
                }
                if (data.action === 'success'){
                    console.log('token ' + data.apiToken + ', secret ' + data.apiSecret);
                    var view = new MerchantSuccessView({'apiToken':data.apiToken,'apiSecret':data.apiSecret,'delivery':self.getParameterByName('delivery'),'deliveryParam':self.getParameterByName('deliveryParam')});
                    Communicator.mediator.trigger('app:show', view);
                }
                if (data.action === 'error'){
                    self.$('#disabledInput').val('');
                    this.$('button.btn-inverse').button('reset');
                }
            });
            if (!this.socketio){
                var socketio = io.connect(window.opt.basePath.split(':8')[0]+':443');
                this.socketio = socketio;
                socketio.on('message', function (data) {
                    Communicator.mediator.trigger('app:message', data);
                    //new events: charge  pay
                    //data return for txns
                });
                socketio.on('connecting', function () {
                    console.log('connecting');
                });
                socketio.on('reconnecting', function () {
                    console.log('reconnecting');
                });
                socketio.on('disconnect', function () {
                    console.log('disconnect');
                });
                socketio.on('reconnect', function () {
                    console.log('reconnect');
                });
                socketio.on('connect', function () {
                    console.log('connect ' + self.ticket);
                    var obj = { '@class' : 'com._37coins.web.MerchantSession',
                        'sessionToken' : self.ticket,
                        'action' : 'subscribe'
                    };
                    self.socketio.json.send(obj);
                });
            }
        },

        events: {
            'click button.btn-inverse': 'handleJoin',
        },
        getParameterByName: function(name) {
            name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
            var regex = new RegExp('[\\?&]' + name + '=([^&#]*)'),
                results = regex.exec(location.search);
            return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
        },
        handleJoin: function(e) {
            e.preventDefault();
            this.$('button').button('loading');
            var val = this.$('#searchInput').val();
            var phoneUtil = window.i18n.phonenumbers.PhoneNumberUtil.getInstance();
            var isValid = false;
            var number;
            try{
                number = phoneUtil.parseAndKeepRawInput(val);
                isValid = phoneUtil.isValidNumber(number);
            }catch(err){
            }
            if (isValid) {
                var PNT = window.i18n.phonenumbers.PhoneNumberType;
                var numberType = phoneUtil.getNumberType(number);
                if (numberType === PNT.MOBILE || numberType === PNT.FIXED_LINE_OR_MOBILE) {
                    var PNF = window.i18n.phonenumbers.PhoneNumberFormat;
                    var strIntlNumber = phoneUtil.format(number, PNF.E164);
                    var obj = { '@class' : 'com._37coins.web.MerchantSession',
                        'sessionToken' : this.ticket,
                        'delivery': this.getParameterByName('delivery'),
                        'deliveryParam': this.getParameterByName('deliveryParam'),
                        'action' : 'verify',
                        'callAction' : this.getParameterByName('action'),
                        'phoneNumber' : strIntlNumber
                    };
                    this.socketio.json.send(obj);
                }else{
                    isValid = false;
                }
            }
            if (!isValid){
                this.$('#donate').empty();
                this.$('#donate').append('<p>Please enter a valid mobile number.</p>');
                this.$('button.btn-inverse').button('reset');
            }
        },

        onShow:function () {
            if (window.lpnLoaded){
                this.$('[name="search"]').attr('disabled', false);
                this.$('button.btn-inverse').attr('disabled', false);
                var country = (!window.opt.country||window.opt.country==='undefined')?'de':window.opt.country.toLowerCase();
                this.$('[name="search"]').intlTelInput({preferredCountries:[],defaultCountry:country});
                var phoneUtil = window.i18n.phonenumbers.PhoneNumberUtil.getInstance();
                var example = phoneUtil.format(phoneUtil.getExampleNumberForType(country,window.i18n.phonenumbers.PhoneNumberType.MOBILE),window.i18n.phonenumbers.PhoneNumberFormat.E164);
                this.$('[name="search"]').attr('placeholder',example);
                this.$('[name="search"]').focus();
            }
        }

    });
});