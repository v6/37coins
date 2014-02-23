define([
    'backbone',
    'communicator',
    'hbs!tmpl/merchantLoginView_tmpl',
    'jqueryValidation'
],
function(Backbone, Communicator, LoginTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: LoginTmpl,
        className: 'container',
        initialize: function() {
            var self = this;
            Communicator.mediator.on('app:failed', function() {
                self.onError();
            });
        },
        events: {
            'click .close': 'handleClose',
        },
        handleClose: function(e){
            var alert = $(e.target).parent();
            alert.one(window.transEvent(), function(){
                alert.css('display', 'none');
            });
            alert.removeClass('in');
        },
        handleLogin: function() {
            this.$('#loginBtn').button('loading');
            var phone = $('input:text').val();
            var tan = $('input:password').val();
            Communicator.mediator.trigger('app:authenticate',phone,tan);
        },
        onError: function(){
            this.$('.alert').css('display','');
            this.$('.alert').addClass('in');
            this.$('#loginBtn').button('reset');
        },
        onShow:function () {
            this.$('.alert').css('display', 'none');
            var jForm = this.$('form');
            var self = this;
            jForm.validate({
                rules: {
                    phone: {
                        required: true
                    },
                    password: {
                        minlength: 4,
                        maxlength: 4,
                        required: true
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
                    self.handleLogin();
                },
                errorPlacement: function(error, element) {
                    error.insertAfter(element);
                }
            });

        }
    });
});