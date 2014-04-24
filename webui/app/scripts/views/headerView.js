define(['backbone','communicator' ,'hbs!tmpl/header'], function(Backbone, Communicator, HeaderTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
		initialize: function() {
			var vent = Communicator.mediator;
			var self = this;
			vent.on('app:login', function(){
				self.setButton();
			});
		},
        template: HeaderTmpl,
        className: 'navbar navbar-inverse navbar-fixed-top navbar-absolute',
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
			this.$('ul.nav').tab();
        }
    });
});