define([
    'backbone',
    'communicator',
    'hbs!tmpl/accountLayout_tmpl',
    'views/commandsView',
    'views/accountHeadlineView',
    'webfinger',
],
function(Backbone, Communicator, AccountLayout, CommandsView, AccountHeadlineView, webfinger) {
    'use strict';
    return Backbone.Marionette.Layout.extend({
	template: AccountLayout,

	regions: {
	    commands: '#smsCommands',
	    header: '#accountHeadline'
	},
	initialize: function(opt){
	    console.log(opt.mobile);
	    var self = this;
	    if (!window.i18n){
		Communicator.mediator.on('app:init', function() {
		    self.handleJoin(opt.mobile);
		});
	    }else{
		this.hasdleJoin(opt.mobile);
	    }
	},
	handleJoin: function(mobile) {
	    this.phoneUtil = window.i18n.phonenumbers.PhoneNumberUtil.getInstance();
	    var val = '+'+mobile;

	    var isValid = false;
	    var pnf = window.i18n.phonenumbers.PhoneNumberFormat;
	    var pnt = window.i18n.phonenumbers.PhoneNumberType;
	    var self = this;
	    var number;
	    try{
		number = this.phoneUtil.parseAndKeepRawInput(val);
		isValid = this.phoneUtil.isValidNumber(number);
		var output = self.phoneUtil.format(number, pnf.NATIONAL);
		var model = new Backbone.Model({mobile:output});
		this.header.show(new AccountHeadlineView({model:model}));
	    }catch(err){
	    }
	    if (isValid) {
		var numberType = this.phoneUtil.getNumberType(number);
		if (numberType === pnt.MOBILE || numberType === pnt.FIXED_LINE_OR_MOBILE) {
		    var strIntlNumber = this.phoneUtil.format(number, pnf.E164);
		    $.ajax({
			type: 'POST',
			contentType: 'application/json',
			url: window.opt.basePath+'/account/invite',
			data: JSON.stringify({mobile:strIntlNumber}),
			complete: function(data){
			    self.attempts = 0;
			    self.number = strIntlNumber.replace('+','');
			    self.submitInvite(data);
			},
			dataType: 'json'
		    });
		}else{
		    isValid = false;
		}
	    }
	    if (!isValid){
		this.$()
		this.$('#donate').empty();
		this.$('#donate').append('<p>Please enter a valid mobile number.</p>');
	    }

	},
	submitInvite: function(data){
	    var cn;
	    this.$('#donate').empty();
	    this.attemts += 1;
	    if (data.status===200 && this.attempts < 7){
		this.$('#donate').append('<p>Wallet created, delivering message...</p>');
		cn = this.number;
	    }else if (data.status===409){
		this.$('#donate').append('<p>Existing Wallet found, retrieving bitcoin address...</p>');
		cn = this.number;
	    }else if (data.status===404){
		this.$('#donate').append('<p>No gateway in this courtry yet, be the first one to <a href="#gateways">start it!</a></p>');
		this.$('button.btn-inverse').button('reset');
		return;
	    }else{
		this.$('#donate').append('<p>Some error occured, please leave a bug report.</p>');
		this.$('button.btn-inverse').button('reset');
		return;
	    }
	    if (cn && this.attempts < 7){
		var self = this;
		webfinger(cn+'@www.37coins.com', {
		    webfist_fallback: false,
		    tls_only: true,
		    uri_fallback: false,
		    debug: false
		}, function(err, p){
		    self.handleAddress(err,p);
		});
	    }
	},
	handleAddress: function(err,p){
	    if (!err) {
		var data = JSON.parse(p.JRD).links[0].href.split(':')[1];
		this.$('#donate').append('<p><img id="'+data+'" width="200px;" src="https://chart.googleapis.com/chart?cht=qr&chs=400x400&chl=bitcoin:'+data+'&chld=H|0" /></p>');
		this.$('#donate').append( '<a href="bitcoin:'+data+'">'+data+'</a>');
		this.attemts = 0;
	    }else{
		this.submitInvite({status:200});
	    }
	}
    });
});