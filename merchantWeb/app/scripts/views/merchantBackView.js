define([
    'backbone',
    'hbs!tmpl/merchantBackView_tmpl',
    'hbs!tmpl/transactionView_tmpl'
],
function(Backbone, MerchantBackTmpl, TransactionView) {
    'use strict';
    return Backbone.Marionette.CompositeView.extend({
	itemView: TransactionView,
	itemViewContainer: '#txTable',
	template: MerchantBackTmpl,
	className: 'gwLayout',
	initialize: function() {
	    //init
	},

	events: {
	    'click .close': 'handleClose',
	},
	onShow:function () {
	    console.log('Web Merchant Back view.');
	}
    });
});