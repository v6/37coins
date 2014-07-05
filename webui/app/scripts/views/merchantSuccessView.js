define([
    'backbone',
    'hbs!tmpl/merchantSuccessView_tmpl',
        'i18n!nls/labels',
        'i18n!nls/webLabels',
    'jqueryValidation'
],
function(Backbone, MerchantSuccessTmpl, myLabels, myWebLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantSuccessTmpl,
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },
        className: 'gwLayout',
        initialize: function(opt) {
            this.model.on('sync', this.onSuccess, this);
            this.model.on('error', this.onError, this);
            this.delivery = opt.delivery;
            this.deliveryParam = opt.deliveryParam;
            this.apiToken = opt.apiToken;
            this.apiSecret = opt.apiSecret;
            this.displayName = opt.displayName;
            if (this.delivery === 'javascript'){
                window.parent.window.postMessage(
                    {'func':this.deliveryParam,'params':[{'apiToken':this.apiToken,'apiSecret':this.apiSecret}]},'*');
            }
        },
        onError: function(){
            this.$('.alert').css('display','');
            this.$('.alert').addClass('in');
            this.$('button.btn-lg').button('reset');
        },
        onSuccess: function(){
            this.$('#instructions').html('Display Name applied.');
            this.$('button.btn-lg').button('reset');
            this.$('.alert').css('display', 'none');
            var displayName = this.$('input[name="displayName"]').val();
            window.parent.window.postMessage(
                {'func':'setDisplayName','params':[{'displayName':displayName}]},'*');
        },
        handleRegister: function() {
            this.$('button.btn-lg').button('loading');
            var displayName = this.$('input[name="displayName"]').val();
            this.model.set('displayName',displayName);
            this.model.save();
        },
        onShow:function () {
            this.$('input[name="displayName"]').val(this.displayName);
            if (this.delivery==='display'){
                this.$('#instructions').html('Please copy and paste those parameters into your POS: <br> API-Token: '+this.apiToken+'<br> API-Secret: '+this.apiSecret);
            }else{
                this.$('#instructions').html('Configuration has been applied to POS automatically.');
            }
            this.$('.alert').css('display', 'none');
            var jForm = this.$('form');
            var self = this;
            jForm.validate({
                rules: {
                    displayName: {
                        required: true,
                        minlength: 3,
                        onkeyup: false,
                        remote: window.opt.basePath + '/merchant/check'
                    }
                },
                messages: {
                    displayName: {
                        remote: 'DisplayName already taken or not valid.',
                        minlength: 'DisplayName should have at least 3 characters.'
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
                submitHandler: function() {
                    self.handleRegister();
                },
                errorPlacement: function(error, element) {
                    error.insertAfter(element);
                }
            });

        }

    });
});