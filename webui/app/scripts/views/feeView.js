define([
    'backbone',
    'hbs!tmpl/feeView_tmpl',
],
function(Backbone, FeeTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: FeeTmpl,
        className: 'gwLayout',
        initialize: function() {
            this.firstRun = true;
            this.model.on('error', this.onError, this);
            this.model.on('sync', this.onSuccess, this);
        },
        onError: function(){
            this.$('#errorAlert').css('display','');
            this.$('#errorAlert').addClass('in');
            this.$('button').button('reset');
        },
        onSuccess: function(){
            if (this.firstRun){
                this.firstRun = false;
            }else{
                this.$('#successAlert').css('display','');
                this.$('#successAlert').addClass('in');
            }
            this.$('button').button('reset');
            //update form
            var fee = this.model.get('fee');
            if (fee){
                this.$('#feeInput').val(fee);
            }
            var welcomeMsg = this.model.get('welcomeMsg');
            if (welcomeMsg){
                this.$('#msgInput').val(welcomeMsg);
            }
            var companyName = this.model.get('companyName');
            if (companyName){
                this.$('#urlInput').val(companyName);
            }
            var callbackUrl = this.model.get('callbackUrl');
            if (callbackUrl){
                this.$('#callbackInput').val(callbackUrl);
            }
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
        handleClick: function(e){
            e.preventDefault();
            this.$('button').button('loading');
            this.handleClose({target:this.$('#successAlert:first-child')[0]});
            this.handleClose({target:this.$('#errorAlert:first-child')[0]});
            var fee = this.$('#feeInput').val();
            var welcomeMsg = this.$('#msgInput').val();
            var companyName = this.$('#urlInput').val();
            var callbackUrl = this.$('#callbackInput').val();
            var modified = false;
            if (fee !== this.model.get('fee')){
                sessionStorage.setItem('fee',fee);
                this.model.set('fee',fee);
                modified = true;
            }
            if (welcomeMsg !== this.model.get('welcomeMsg')){
                sessionStorage.setItem('welcomeMsg',welcomeMsg);
                this.model.set('welcomeMsg',welcomeMsg);
                modified = true;
            }
            if (companyName !== this.model.get('companyName')){
                sessionStorage.setItem('companyName',companyName);
                this.model.set('companyName',companyName);
                modified = true;
            }
            if (callbackUrl !== this.model.get('callbackUrl')){
                sessionStorage.setItem('callbackUrl',callbackUrl);
                this.model.set('callbackUrl',callbackUrl);
                modified = true;
            }
            if(modified){
                this.model.save();
            }else{
                this.$('button').button('reset');
            }
        },
        onShow:function () {
            this.$('#feeInput').val(this.model.get('fee'));
            this.$('.alert').css('display', 'none');
            var jForm = this.$('form');
            var self = this;
            jForm.validate({
                rules: {
                    fee: {
                        required: true,
                        number: true
                    },
                    msg: {
                        required: false,
                        maxlength: 140
                    },
                    url: {
                        required: false
                    },
                    callback: {
                        required: false
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
                    self.handleClick(e);
                },
                errorPlacement: function(error, element) {
                    error.insertAfter(element);
                }
            });
        }
    });
});