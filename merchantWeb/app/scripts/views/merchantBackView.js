define([
    'backbone',
    'hbs!tmpl/merchantBackView_tmpl'
],
function(Backbone, MerchantBackTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: MerchantBackTmpl,
        className: 'gwLayout',
        initialize: function() {
            //init
        },

        events: {
            'click .close': 'handleClose',
        },
        handleCharge:function (e){
            e.preventDefault();
            console.log('here');
            var setHeader = function (xhr) {
                xhr.setRequestHeader('X-Request-Signature', 'aqLJlmE2rRXBOy***************');
            };
            this.model.fetch({ beforeSend: setHeader });
        },
        onShow:function () {
            this.$('.alert').css('display', 'none');
            var jForm = this.$('form');
            var self = this;
            jForm.validate({
                rules: {
                    fee: {
                        required: true,
                        number: true
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
                submitHandler: function(f,e) {
                    self.handleCharge(e);
                },
                errorPlacement: function(error, element) {
                    error.insertAfter(element);
                }
            });
        }
    });
});