define([
    'backbone',
    'communicator',
    'hbs!tmpl/adminAccountView_tmpl'
],
function(Backbone, Communicator, AccountTmpl) {
    'use strict';
    return Backbone.Marionette.ItemView.extend({
        template: AccountTmpl,
        tagName: 'tr',
        events: {
            'click a.del': 'handleDelete',
            'click a.edit': 'handleEdit'
        },
        initialize: function(options){
            this.credentials = options.credentials;
            this.collection = options.collection;
        },
        handleDelete: function(e){
            console.dir(e);
            e.preventDefault();
            var mobile = e.target.dataset.val.replace('+','');
            var self = this;
            var h = Backbone.BasicAuth.getHeader(this.credentials);
            $.ajax({
                url: window.opt.basePath +'/api/gateway/admin/accounts/'+mobile,
                beforeSend: function(request) {
                    var a = Object.keys(h)[0];
                    request.setRequestHeader(a, h[a]);
                },
                type: 'DELETE',
                success: function() {
                    self.collection.fetch();
                }
            });
            return false;
        },
        handleEdit: function(e){
            e.preventDefault();
            //disable all old edits
            //replace values with input fields
            //replace button with save
        }
    });
});