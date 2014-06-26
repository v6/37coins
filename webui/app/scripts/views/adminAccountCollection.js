define([
    'backbone',
    'communicator',
    'views/adminAccountView',
    'hbs!tmpl/adminAccountCollection_tmpl'
],
function(Backbone, Communicator, AccountView, AccountCollectionTmpl) {
    'use strict';
    return Backbone.Marionette.CompositeView.extend({
        itemView: AccountView,
        itemViewOptions: function(){
            return {
                collection: this.collection,
                credentials: this.collection.credentials
            };
        },
        itemViewContainer: 'tbody',
        template: AccountCollectionTmpl,
        events: {
            'click button': 'handleSearch'
        },
        handleSearch: function(e){
            e.preventDefault();
            var filter = this.$('#InputFilter').val();
            var page = this.$('#InputPage').val();
            var size = this.$('#InputSize').val();
            this.collection.search(filter);
        },
    });
});