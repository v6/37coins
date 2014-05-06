define(['backbone',
    'communicator',
    'GA',
    'models/loginModel',
    'models/accountRequest',
    'models/resetRequest',
    'models/resetConf',
    'models/signupConf',
    'models/balanceModel',
    'models/feeModel',
    'collections/gatewayCollection',
    'views/indexLayout',
    'views/indexHeaderLayout',
    'views/loginView',
    'views/gatewayView',
    'views/gatewayCollectionView',
    'views/faqView',
    'views/aboutView',
    'views/accountLayout',
    'views/accountHeaderView',
    'views/commandsView',
    'views/verifyView',
    'views/validateView',
    'views/captchaView',
    'views/logoutView',
    'views/signupView',
    'views/signupWalletLayout',
    'views/signinWalletLayout',
    'views/resetView',
    'views/headerSendView',
    'views/commandSendView',
    'views/commandHelpLayout',
    'views/resetConfView',
    'views/signupConfView',
    'views/balanceView',
    'views/feeView',
    'views/mobileInputView',
    'views/gatewayLayout',
    'views/notFoundView',
    'routeFilter',
    'views/merchantView',
], function(Backbone, Communicator, GA, LoginModel, AccountRequest, ResetRequest, ResetConf, SignupConf, BalanceModel, FeeModel, GatewayCollection, IndexLayout, IndexHeaderLayout, LoginView, GatewayView, GatewayCollectionView, FaqView, AboutView, AccountLayout, AccountHeaderView, CommandsView, VerifyView, ValidateView, CaptchaView, LogoutView, SignupView, SignupWalletLayout, SigninWalletLayout, ResetView, HeaderSendView, CommandSendView, CommandHelpLayout, ResetConfView, SignupConfView, BalanceView, FeeView, MobileInputView, GatewayLayout, NotFoundView, io, MerchantView) {
    'use strict';

    var Controller = {};

    // private module/app router  capture route and call start method of our controller
    Controller.Router = Backbone.Marionette.AppRouter.extend({
        initialize: function(opt){
            this.app = opt.app;
            Controller.app = opt.app;
        },
        appRoutes: {
            '': 'showIndex',
            'gateways': 'showGateway',
            'balance': 'showBalance',
            'faq': 'showFaq',
            'confSignup/:token': 'confirmSignUp',
            'confReset/:token': 'confirmReset',
            'commands/send': 'showCommandSend',
            'commands/help': 'showCommandHelp',
            'account/:mobile': 'showAccount',
            'reset': 'showReset',
            'about': 'showAbout',
            'signUp': 'showSignUp',
            'signupWallet':'showSignupWallet',
            'signinWallet':'showSigninWallet',
            'logout': 'showLogout',
            'merchant': 'showMerchant',
            'notFound': 'showNotFound'
        },
        before:{
            '': 'loadLibPhone',
            'account/:mobile': 'loadLibPhone',
            'signupWallet': 'loadLibPhone',
            'signinWallet': 'loadLibPhone',
            'signUp': 'getTicket',
            'reset': 'getTicket',
            'gateways': 'showLogin',
            'balance': 'showLogin',
            'merchant': 'getTicket',
            '*any': function(fragment, args, next){
                //set title
                if (fragment){
                    $(document).attr('title', '37 Coins - ' + fragment);
                }else {
                    $(document).attr('title', '37 Coins');
                }
                var items = $('.navbar .nav li a');
                _.each(items, function(item){
                    if ($(item).attr('href')){
                        var href = $(item).attr('href').replace('#','');
                        if (href===fragment){
                            $(item).parent().addClass('active');
                        }else{
                            $(item).parent().removeClass('active');
                        }
                    }
                    if (!fragment){
                        $('#products').parent().addClass('active');
                    }
                });
                //set meta tag
                $('meta[name=description]').remove();
                $('head').append( '<meta name="description" content="this is new">' );
                //track page visit
                GA.view(fragment);
                next();
            }
        },
        loadLibPhone: function(fragment, args, next){
            var ctl = this.options.controller;
            if (!ctl.gateways){
                ctl.gateways = new GatewayCollection();
            }
            if (ctl.gateways.length<1){
                //load dependency manually
                var script = document.createElement('script');
                script.type = 'text/javascript';
                script.onload = function(){
                    Communicator.mediator.trigger('app:init');
                    ctl.gateways.fetch({reset: true});
                };
                script.src = window.opt.resPath + '/scripts/vendor/libphonenumbers.js';
                document.getElementsByTagName('head')[0].appendChild(script);
            }
            next();
        },
        getTicket: function(fragment, args, next) {
            if (!this.options.controller.ticket){
                // var view = new MerchantConnectingView();
                // Communicator.mediator.trigger('app:show', view);
                var self = this;
                $.post( window.opt.basePath + '/ticket', function( data ) {
                    self.options.controller.ticket = data.value;
                    next();
                },'json').fail(function() {
                    var view = new CaptchaView({next:next,controller:self.options.controller});
                    Communicator.mediator.trigger('app:show', view);
                });
            }else{
                next();
            }
        },
        showLogin: function(fragment, args, next) {
            if (!this.options.controller.loginStatus){
                this.options.controller.loginStatus = new LoginModel();
            }
            var view;
            var model = this.options.controller.loginStatus;
            if (model.get('roles')){
                next();
            }else{
                view = new LoginView({model:model,next:next});
                Communicator.mediator.trigger('app:show', view);
            }
        }
    });

    Communicator.mediator.on('app:verify', function() {
        var view;
        if (Controller.loginStatus.get('mobile') && Controller.loginStatus.get('fee')){
            var layout = new GatewayLayout();
            Communicator.mediator.trigger('app:show', layout);
            if (!window.Android){
                var configView = new GatewayView({model:Controller.loginStatus});
                layout.conf.show(configView);
            }else{
                window.Android.setConfig(Controller.loginStatus.get('basePath'),
                Controller.loginStatus.get('cn'),
                Controller.loginStatus.get('mobile'),
                Controller.loginStatus.get('envayaToken'),
                Controller.loginStatus.get('srvcPath'),
                Controller.loginStatus.credentials.password);
            }
            var balance = new BalanceModel();
            var balanceView = new BalanceView({model:balance});
            layout.bal.show(balanceView);
            var feeModel = new FeeModel({fee:sessionStorage.getItem('fee')});
            var feeView = new FeeView({model:feeModel});
            layout.fee.show(feeView);
        }else if (Controller.loginStatus.get('mobile')){
            view = new ValidateView({model:Controller.loginStatus});
            Communicator.mediator.trigger('app:show', view);
        }else {
            view = new VerifyView({model:Controller.loginStatus});
            Communicator.mediator.trigger('app:show', view);
        }
    });

    Controller.showIndex = function() {
        var header = new IndexHeaderLayout({model:new Backbone.Model({resPath:window.opt.resPath})});
        var layout = new IndexLayout({model:new Backbone.Model({resPath:window.opt.resPath})});
        Communicator.mediator.trigger('app:show', layout, header);
        var mobileInput = new MobileInputView({model:new Backbone.Model({resPath:window.opt.resPath})});
        header.mobileInput.show(mobileInput);
        layout.gateways.show(new GatewayCollectionView({collection:this.gateways}));
        var commands = new CommandsView();
        layout.commands.show(commands);
    };

    Controller.showAccount = function(mobile) {
        var header = new AccountHeaderView({mobile:mobile,gateways:this.gateways});
        var layout = new AccountLayout({mobile:mobile});
        Communicator.mediator.trigger('app:show', layout, header);
        var commands = new CommandsView();
        layout.commands.show(commands);
    };

    Controller.showGateway = function() {
        Communicator.mediator.trigger('app:verify');
    };

    Controller.showFaq = function() {
        var view = new FaqView();
        Communicator.mediator.trigger('app:show', view);
    };

    Controller.showAbout = function() {
        var view = new AboutView({model:new Backbone.Model({resPath:window.opt.resPath})});
        Communicator.mediator.trigger('app:show', view);
    };

    Controller.showBalance = function() {
        var balance = new BalanceModel();
        var view = new BalanceView({model:balance});
        Communicator.mediator.trigger('app:show', view);
        balance.fetch();
    };
    Controller.showLogout = function() {
        var contentView = new LogoutView();
        Communicator.mediator.trigger('app:show',contentView);
    };
    Controller.showMerchant = function() {
        var contentView = new MerchantView({ticket:Controller.ticket,app:Controller.app});
        Communicator.mediator.trigger('app:show',contentView);
        if (!window.lpnLoaded){
            //load dependency manually
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.onload = function(){
                console.log('fetched');
                Communicator.mediator.trigger('app:init');
            };
            script.src = window.opt.resPath + '/scripts/vendor/libphonenumbers.js';
            document.getElementsByTagName('head')[0].appendChild(script);
        }
    };
    Controller.showCommandSend = function() {
        var headerView = new HeaderSendView({model:new Backbone.Model({resPath:window.opt.resPath})});
        var contentView = new CommandSendView();
        Communicator.mediator.trigger('app:show',contentView, headerView);
    };
    Controller.showCommandHelp = function() {
        var layout = new CommandHelpLayout({model:new Backbone.Model({resPath:window.opt.resPath})});
        Communicator.mediator.trigger('app:show',layout);
        var commands = new CommandsView();
        layout.commands.show(commands);
    };
    Controller.showNotFound = function() {
        var contentView = new NotFoundView();
        Communicator.mediator.trigger('app:show',contentView);
    };
    Controller.showSignUp = function() {
        var accountRequest = new AccountRequest({ticket:Controller.ticket});
        var contentView = new SignupView({model:accountRequest});
        Communicator.mediator.trigger('app:show',contentView);
    };
    Controller.showSignupWallet = function() {
        var layout = new SignupWalletLayout();
        Communicator.mediator.trigger('app:show',layout);
        var mobileInput = new MobileInputView({model:new Backbone.Model({resPath:window.opt.resPath})});
        layout.mobileInput.show(mobileInput);
    };
    Controller.showSigninWallet = function() {
        var layout = new SigninWalletLayout();
        Communicator.mediator.trigger('app:show',layout);
        var mobileInput = new MobileInputView({model:new Backbone.Model({resPath:window.opt.resPath})});
        layout.mobileInput.show(mobileInput);
    };
    Controller.confirmSignUp = function(token) {
        var model = new SignupConf({token:token});
        var contentView = new SignupConfView({model: model});
        Communicator.mediator.trigger('app:show',contentView);
        model.save();
    };
    Controller.showReset = function() {
        var model = new ResetRequest({ticket:Controller.ticket});
        var contentView = new ResetView({model:model});
        Communicator.mediator.trigger('app:show',contentView);
    };
    Controller.confirmReset = function(token) {
        var model = new ResetConf({token:token});
        var contentView = new ResetConfView({model: model});
        Communicator.mediator.trigger('app:show',contentView);
    };

    return Controller;
});