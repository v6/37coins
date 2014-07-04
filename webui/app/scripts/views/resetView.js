define([
	'backbone',
	'communicator',
	'hbs!tmpl/resetView_tmpl',
	'hbs!tmpl/resetCompletedView_tmpl',
    'i18n!nls/labels',
    'i18n!nls/webLabels',
	'jqueryValidation'
],
function( Backbone, Communicator, ResetTmpl, ResetCompleteTmpl, myLabels, myWebLabels ) {
    'use strict';

	/* Return a ItemView class definition */
	return Backbone.Marionette.ItemView.extend({

		initialize: function() {
			console.log('initialize a Reset ItemView');
			this.model.on('error', this.onError, this);
			this.model.on('sync', this.onSuccess, this);
		},

		getTemplate: function(){
		    if (this.fetched){
		        return ResetCompleteTmpl;
		    } else {
		        return ResetTmpl;
		    }
		},

        templateHelpers: function(){
            return window.helpers(myLabels, myWebLabels);
        },

		onSuccess: function(){
			this.fetched = true;
			this.render();
		},

        onError: function(model, response){
			if (response.status===400){
				location.reload();
			}
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

		/* Ui events hash */
		events: {
			'click .close': 'handleClose',
		},

		handleReset: function(e){
			e.preventDefault();
			this.$('button.btn-primary').button('loading');
			var user = this.$('#exampleInputEmail1').val();
			this.model.set('email',user);
			this.model.save();
		},

		/* on render callback */
		onShow: function() {
			if (!this.fetched){
				this.$('.alert').css('display', 'none');
				var jForm = this.$('form');
				var self = this;
				jForm.validate({
			        rules: {
			            email: {
							required: true,
							email: true
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
						self.handleReset(e);
			        },
			        errorPlacement: function(error, element) {
			            error.insertAfter(element);
			        }
			    });
			}
		}
	});

});
