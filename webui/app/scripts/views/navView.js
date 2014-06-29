define([
	'backbone',
	'communicator' ,
	'hbs!tmpl/navView_tmpl',
	'i18n!nls/labels'
],
function(Backbone, Communicator, NavTmpl, myLabels) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
		initialize: function() {
			var vent = Communicator.mediator;
			var self = this;
			vent.on('app:login', function(){
				self.setButton();
			});
		},
		templateHelpers: function(){
			return window.helpers(myLabels);
		},
        template: NavTmpl,
        className: 'navbar navbar-37',
        tagName: 'div role="navigation"',
        events: {
			'click #aLogout':'handleLogout'
		},
        setButton: function(){
			if (sessionStorage.getItem('roles')){
				this.$('#liLogin').hide();
				this.$('#liLogout').show();
			}else{
				this.$('#liLogout').hide();
				this.$('#liLogin').show();
			}
		},
		handleLogout: function(e){
			e.preventDefault();
			this.$('#liLogout').hide();
			this.$('#liLogin').show();
			Communicator.mediator.trigger('app:logout');
		},
		onShow: function() {
			this.$('.dropdown-toggle').dropdown();
        }
    });
});