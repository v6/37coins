define([
	'backbone',
	'hbs!tmpl/signupConfView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels'
],
function( Backbone, SignupConfTmpl, myLabels, myWebLabels ) {
    'use strict';

	/* Return a ItemView class definition */
	return Backbone.Marionette.ItemView.extend({

		initialize: function() {
			console.log('initialize a Signup Conf ItemView');
			this.model.on('error', this.onError, this);
			this.model.on('sync', this.onSuccess, this);
		},

		template: SignupConfTmpl,


        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },
        
        onError: function(){
			console.log('failed');
			this.$('#signupSucAlert').hide();
			this.$('#signupErrAlert').addClass('in');
        },

        onSuccess: function(){
			this.$('#signupSucAlert').addClass('in');
			this.$('#signupErrAlert').hide();
        },

		/* ui selector cache */
		ui: {},

		/* Ui events hash */
		events: {
            'click button.btn-primary':'handleDone'
        },

        handleDone: function(e){
			e.preventDefault();
        },

		/* on render callback */
		onRender: function() {
		}
	});

});