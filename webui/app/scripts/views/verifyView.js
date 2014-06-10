define([
    'backbone',
    'communicator',
    'views/validateView',
    'hbs!tmpl/verifyView_tmpl'
],
function(Backbone, Communicator, ValidateView, VerifyTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: VerifyTmpl,
        className: 'container',
        initialize: function() {
            this.model.on('sync', this.onSync, this);
            this.model.on('error', this.onError, this);
        },
        onSync: function(){
            Communicator.mediator.trigger('app:verify');
        },
        onError: function(){
            this.$('.alert').css('display','');
            this.$('.alert').addClass('in');
            this.$('button.btn-primary').button('reset');
        },
        handleClose: function(e){
            var alert = $(e.target).parent();
            alert.one(window.transEvent(), function(){
                alert.css('display', 'none');
            });
            alert.removeClass('in');
        },
        events: {
            'click .close': 'handleClose',
        },
        handleValidate: function(e) {
            e.preventDefault();
            this.$('button.btn-primary').button('loading');
            var mobile = this.$('input[name="tel"]').val();
            this.model.set('mobile',mobile);
            this.model.set('locale',window.opt.lng);
            this.model.save();
        },
        onShow:function () {
            this.$('.alert').css('display', 'none');
            var jForm = this.$('form');
            var self = this;
            $.validator.addMethod('phone', function(value) {
                var phoneUtil = window.i18n.phonenumbers.PhoneNumberUtil.getInstance();
                var isValid = false;
                var number;
                try{
                    number = phoneUtil.parseAndKeepRawInput(value);
                    isValid = phoneUtil.isValidNumber(number);
                }catch(err){}
                return isValid;
            }, 'Please enter a mobile number in international format.');
            jForm.validate({
                rules: {
                    tel: {
                        required: true,
                        phone: true
                    }
                },
                highlight: function(element) {
                    $(element).closest('.form-group').addClass('has-error');
                },
                unhighlight: function(element) {
                    $(element).closest('.form-group').removeClass('has-error');
                },
                errorElement: 'span',
                errorClass: 'help-block',
        submitHandler: function(a,e) {
            self.handleValidate(e);
                },
                errorPlacement: function(error, element) {
                    error.insertAfter(element);
                }
            });
        }
    });
});