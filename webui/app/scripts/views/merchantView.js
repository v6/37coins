define([
    'backbone',
    'hbs!tmpl/merchantView_tmpl',
    'socketio',
    'communicator',
    'views/merchantSuccessView',
    'models/merchantModel',
        'i18n!nls/labels',
        'i18n!nls/webLabels'
],
function(Backbone, MerchantTmpl, io, Communicator, MerchantSuccessView, MerchantModel, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },
        className: 'gwLayout',
        initialize: function(opt) {
            this.ticket = opt.ticket;
            this.app = opt.app;
            var self = this;
            Communicator.mediator.on('app:init', function() {
                window.lpnLoaded = true;
                self.onShow();
            });
            if (!this.socketio){
                var ioPath = (window.opt.srvcPath)?window.opt.srvcPath:window.opt.basePath.split(':8')[0]+':443';
                var socketio = io.connect(ioPath);
                this.socketio = socketio;
                socketio.on('message', function (data) {
                    if (data.action === 'started'){
                        self.$('#disabledInput').val(data.sessionToken);
                        self.$('#merchStatus').html('Press '+data.sessionToken+' during the call.');
                        self.$('#merchStatus').attr('class','bg-success');
                    }
                    if (data.action === 'success'){
                        self.$('#merchStatus').html('Phone successfully verified.');
                        self.$('#merchStatus').attr('class','bg-success');
                        var merchantModel = new MerchantModel({
                            sessionToken:self.ticket
                        });
                        var view = new MerchantSuccessView({
                            model:merchantModel,
                            apiToken:data.apiToken,
                            apiSecret:data.apiSecret,
                            delivery:data.delivery,
                            deliveryParam:data.deliveryParam,
                            displayName:data.displayName
                        });
                        Communicator.mediator.trigger('app:show', view);
                        var obj = { '@class' : 'com._37coins.web.MerchantSession',
                            'sessionToken' : self.ticket,
                            'action' : 'logout'
                        };
                        self.socketio.json.send(obj);
                    }
                    if (data.action === 'error'){
                        self.$('#disabledInput').val('');
                        self.$('button.btn-inverse').button('reset');
                        self.$('#merchStatus').html('Error! Please retry.');
                        self.$('#merchStatus').attr('class','bg-danger');
                    }
                });
                socketio.on('error', function(data) {
                    self.$('#merchStatus').html('Connection lost. Please reload.<br>'+data);
                    self.$('#merchStatus').attr('class','bg-danger');
                    self.$('button.btn-inverse').attr('disabled', true);
                });
                socketio.on('connecting', function () {
                    self.$('#merchStatus').html('Connecting...');
                    self.$('#merchStatus').attr('class','bg-info');
                    self.$('button.btn-inverse').attr('disabled', true);
                });
                socketio.on('reconnecting', function () {
                    self.$('#merchStatus').html('Connecting...');
                    self.$('#merchStatus').attr('class','bg-info');
                    self.$('button.btn-inverse').attr('disabled', true);
                });
                socketio.on('disconnect', function () {
                    self.$('#merchStatus').html('Connection lost. Please reload.');
                    self.$('#merchStatus').attr('class','bg-danger');
                    self.$('button.btn-inverse').attr('disabled', true);
                });
                socketio.on('reconnect', function () {
                    self.$('#merchStatus').html('Connection established.');
                    self.$('#merchStatus').attr('class','bg-success');
                    self.$('button.btn-inverse').button('reset');
                });
                socketio.on('connect', function () {
                    console.log('connect ' + self.ticket);
                    self.$('#merchStatus').html('Please insert your phone number.');
                    self.$('#merchStatus').attr('class','bg-info');
                    self.$('button.btn-inverse').button('reset');
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

        handleJoin: function(e) {
            e.preventDefault();
            self.$('button.btn-inverse').attr('disabled', true);
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
                        'delivery': this.app.getParameterByName('delivery'),
                        'deliveryParam': this.app.getParameterByName('deliveryParam'),
                        'action' : 'verify',
                        'callAction' : this.app.getParameterByName('action'),
                        'phoneNumber' : strIntlNumber
                    };
                    this.socketio.json.send(obj);
                }else{
                    isValid = false;
                }
            }
            if (!isValid){
                this.$('#merchStatus').html('Please enter a valid mobile number.');
                this.$('#merchStatus').attr('class','bg-danger');
                this.$('button.btn-inverse').button('reset');
            }
        },

        onShow:function () {
            if (window.lpnLoaded){
                this.$('[name="search"]').attr('disabled', false);
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