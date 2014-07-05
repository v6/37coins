define([
    'backbone',
    'hbs!tmpl/signupView_tmpl',
    'hbs!tmpl/signupCompletedView_tmpl',
    'recaptcha',
    'i18n!nls/labels',
    'i18n!nls/webLabels',
    'jqueryValidation'
],
function(Backbone, SignupTmpl, SignupCompleteTmpl, Recaptcha, myLabels, myWebLabels) {
    'use strict';

    /* Return a ItemView class definition */
    return Backbone.Marionette.ItemView.extend({
        initialize: function() {
            this.model.on('sync', this.onSuccess, this);
            this.model.on('error', this.onError, this);
        },
        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },
        onError: function(model, response){
            if (response.status===417){
                location.reload();
            }
            this.$('.alert').css('display','');
            this.$('.alert').addClass('in');
            this.$('button.btn-primary').button('reset');
        },
        onSuccess: function(){
            this.fetched = true;
            this.render();
        },
        getTemplate: function(){
            if (this.fetched){
                return SignupCompleteTmpl;
            } else {
                return SignupTmpl;
            }
        },
        handleRegister: function() {
            this.$('button.btn-primary').button('loading');
            var email = this.$('input[name="email"]').val();
            var pw1 = this.$('input[name="password1"]').val();
            this.model.set('email',email);
            this.model.set('password',pw1);
            this.model.save();
        },
        onShow: function(){
            if (!this.fetched){
                this.$('.alert').css('display', 'none');
                var jForm = this.$('form');
                var self = this;
                jForm.validate({
                    rules: {
                        email: {
                            required: true,
                            email: true,
                            onkeyup: false,
                            remote: window.opt.basePath + '/accounts/check'
                        },
                        password1: {
                            minlength: 6,
                            maxlength: 40,
                            required: true
                        },
                        password2: {
                            equalTo: 'input[name="password1"]'
                        }
                    },
                    messages: {
                        email: {
                            remote: 'Email already taken or not valid.'
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
        }
    });

});
