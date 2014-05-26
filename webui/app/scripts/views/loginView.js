define([
    'backbone',
    'communicator',
    'hbs!tmpl/loginView_tmpl',
    'jqueryValidation'
],
function(Backbone, Communicator, LoginTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: LoginTmpl,
        className: 'container',
        initialize: function(opt) {
            this.next = opt.next;
			this.model.on('change:roles', this.onRolesChange, this);
            this.model.on('error', this.onError, this);
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
        handleLogin: function(e) {
            e.preventDefault();
            this.$('#loginBtn').button('loading');
            var user = $('input:text').val();
            var pw = $('input:password').val();
            var cred = {
                username: user,
                password: pw
            };
            this.model.set({
                locale: window.opt.lng,
                basePath: window.opt.basePath,
                srvcPath: window.opt.srvcPath.split('://')[1]
            });
            sessionStorage.setItem('credentials',JSON.stringify(cred));
            this.model.credentials = cred;
            this.model.fetch();
        },
        onRolesChange: function(){
            if (this.model.get('roles')){
                Communicator.mediator.trigger('app:login');
                this.next();
            }
        },
        onError: function(model, response){
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
                    email: {
                        required: true,
                        email: true
                    },
                    password: {
                        minlength: 6,
                        maxlength: 40,
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
                submitHandler: function(a,e) {
                    self.handleLogin(e);
                },
                errorPlacement: function(error, element) {
                    error.insertAfter(element);
                }
            });
	    //for display in android
	    if (window.Android){
		window.Android.loadComplete();
	    }
        }
    });
});