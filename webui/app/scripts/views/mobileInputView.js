define([
    'backbone',
    'communicator',
    'hbs!tmpl/mobileInputView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels',
    'intlTelInput'
],
function(Backbone, Communicator, MobileInputTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MobileInputTmpl,
        className: 'static',
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },
        handleJoin: function(mobile){
            Backbone.history.navigate('account/'+mobile, {trigger: true});
        },
        onShow: function(){
            if (window.lpnLoaded){
                this.$('[name="search"]').attr('disabled', false);
                var self = this;
                var country = (!window.opt.country||window.opt.country==='undefined')?'de':window.opt.country.toLowerCase();
                var telInput = this.$('[name="search"]');
                var errorMsg = $('#error-msg');
                var validMsg = $('#valid-msg');
                var phoneUtil = window.i18n.phonenumbers.PhoneNumberUtil.getInstance();
                telInput.intlTelInput({preferredCountries:[],defaultCountry:country});
                telInput.blur(function(){
                    if ($.trim(telInput.val())) {
                        if (telInput.intlTelInput('isValidNumber')) {
                            validMsg.removeClass('hide');
                            self.$('input.inside').attr('disabled', false);
                        }else{
                            telInput.parent().addClass('has-error');
                            errorMsg.removeClass('hide');
                            validMsg.addClass('hide');
                        }
                    }
                });
                telInput.keydown(function(){
                    telInput.parent().removeClass('has-error');
                    errorMsg.addClass('hide');
                    validMsg.addClass('hide');
                });
                telInput.keyup(function(){
                    if ($.trim(telInput.val())) {
                        if (telInput.intlTelInput('isValidNumber')) {
                            telInput.parent().removeClass('has-error');
                            telInput.parent().addClass('has-success');
                            validMsg.removeClass('hide');
                            self.$('input.inside').attr('disabled', false);
                        }else{
                            telInput.parent().addClass('has-error');
                            telInput.parent().removeClass('has-success');
                            errorMsg.removeClass('hide');
                            validMsg.addClass('hide');
                        }
                    }
                });
                this.$('div.intl-tel-input.inside').append('<input class="inside btn-primary" type="submit" value="Go!" disabled="disabled">');
                this.$('input.inside').on('click',function(e){
                    e.preventDefault();
                    if ($.trim(telInput.val())) {
                        var ok = false;
                        if (telInput.intlTelInput('isValidNumber')) {
                            var countryData = telInput.intlTelInput('getSelectedCountryData');
                            var number = phoneUtil.parseAndKeepRawInput(telInput.val(), countryData.iso2);
                            var type = phoneUtil.getNumberType(number);
                            var pnt = window.i18n.phonenumbers.PhoneNumberType;
                            if (type===pnt.MOBILE || type===pnt.FIXED_LINE_OR_MOBILE){
                                ok = true;
                                var string = phoneUtil.format(number, window.i18n.phonenumbers.PhoneNumberFormat.E164);
                                self.handleJoin(string.replace('+',''));
                            }
                        }
                        if (!ok){
                            telInput.parent().addClass('has-error');
                            errorMsg.removeClass('hide');
                            validMsg.addClass('hide');
                        }
                    }
                });
                var example = phoneUtil.format(phoneUtil.getExampleNumberForType(country,window.i18n.phonenumbers.PhoneNumberType.MOBILE),window.i18n.phonenumbers.PhoneNumberFormat.E164);
                this.$('[name="search"]').attr('placeholder',example);
                this.$('[name="search"]').focus();
            }
        },
        initialize: function() {
            var self = this;
            Communicator.mediator.on('app:init', function() {
                window.lpnLoaded = true;
                self.onShow();
            });
        }
    });
});