define(['backbone',
    'communicator',
    'GA',
    'models/loginModel',
    'models/accountRequest',
    'models/resetRequest',
    'models/resetConf',
    'models/signupConf',
    'models/balanceModel',
    'models/settingsModel',
    'collections/gatewayCollection',
    'collections/accountCollection',
    'views/indexLayout',
    'views/indexHeaderLayout',
    'views/loginView',
    'views/gatewayView',
    'views/gatewayCollectionView',
    'views/adminLayout',
    'views/adminAccountCollection',
    'views/adminAccountView',
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
    'views/commandHelpLayout',
    'views/resetConfView',
    'views/signupConfView',
    'views/balanceView',
    'views/feeView',
    'views/mobileInputView',
    'views/gatewayLayout',
    'views/notFoundView',
    'views/termsView',
    'views/privacyView',
    'routeFilter',
    'views/merchantView',
    'i18n'
], function(Backbone, Communicator, GA, LoginModel, AccountRequest, ResetRequest, ResetConf, SignupConf, BalanceModel, SettingsModel, GatewayCollection, AccountCollection, IndexLayout, IndexHeaderLayout, LoginView, GatewayView, GatewayCollectionView, AdminLayout, AdminAccountCollection, AdminAccountView, FaqView, AboutView, AccountLayout, AccountHeaderView, CommandsView, VerifyView, ValidateView, CaptchaView, LogoutView, SignupView, SignupWalletLayout, SigninWalletLayout, ResetView, CommandHelpLayout, ResetConfView, SignupConfView, BalanceView, FeeView, MobileInputView, GatewayLayout, NotFoundView, TermsView, PrivacyView, io, MerchantView,I18n) {
    'use strict';

    var Controller = {};

    // private module/app router  capture route and call start method of our controller
    // Make sure to pass in a new model object the language attribute to any views that need link localization. 
    Controller.Router = Backbone.Marionette.AppRouter.extend({
        initialize: function(opt){
            this.app = opt.app;
            Controller.app = opt.app;
        },
        appRoutes: {
            '': 'showIndex',
            ':lng/': 'showIndex',
            ':lng/gateways': 'showGateway',
            ':lng/balance': 'showBalance',
            ':lng/faq': 'showFaq',
            ':lng/admin': 'showAdmin',
            ':lng/confSignup/:token': 'confirmSignUp',
            ':lng/confReset/:token': 'confirmReset',
            ':lng/help/SMSgateway': 'showFaq',
            ':lng/help/SMSwallet': 'showCommandHelp',
            ':lng/account/:mobile': 'showAccount',
            ':lng/legal/terms': 'showTerms',
            ':lng/legal/privacy': 'showPrivacy',
            ':lng/reset': 'showReset',
            ':lng/about': 'showAbout',
            ':lng/signUp': 'showSignUp',
            ':lng/signupWallet':'showSignupWallet',
            ':lng/signinWallet':'showSigninWallet',
            ':lng/logout': 'showLogout',
            ':lng/merchant': 'showMerchant',
            ':lng/*notFound': 'showNotFound',
            '*notFound': 'showNotFound'
        },
        before:{
            '': 'loadLibPhone',
            ':lng/': 'loadLibPhone',
            ':lng/admin': 'showLogin',
            ':lng/account/:mobile': 'loadLibPhone',
            ':lng/signupWallet': 'loadLibPhone',
            ':lng/signinWallet': 'loadLibPhone',
            ':lng/signUp': 'getTicket',
            ':lng/reset': 'getTicket',
            ':lng/gateways': 'showLogin',
            ':lng/balance': 'showLogin',
            ':lng/merchant': 'getTicket',
            '*notFound': 'lngRedirect',
            '*any': function(fragment, args, next){
                //capture language
                var locale = window.getLocale();
                if (args.length > 0 && args[0].length===2 && fragment.indexOf(args[0])===0){
                    if(locale !== args[0]) {
                        localStorage.setItem('locale', args[0]);
                        location.reload();
                    }
                }else{
                    if(locale !== 'en') {
                        localStorage.setItem('locale', 'en');
                        location.reload();
                    }
                }
                //do highlights on navigationbar
                var items = $('.navbar .nav li a');
                _.each(items, function(item){
                    var href = ($(item).attr('href'))?$(item).attr('href').replace('#',''):undefined;
                    if (href===fragment){
                        $(item).parent().addClass('active');
                    }else{
                        $(item).parent().removeClass('active');
                    }
                    if ($(item).attr('id')==='products' && !fragment){
                        $(item).parent().addClass('active');
                    }else{
                        if (!href && !$(item).attr('id') && fragment && fragment.indexOf('help')!==-1){
                            $(item).parent().addClass('active');
                        }
                    }
                });
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
        lngRedirect: function(fragment, args, next) {
            if (args.length > 0 && args[0].length===2 && fragment.indexOf(args[0])===0){
                next();
            }else{
                var locale = window.getLocale();
                console.log('redirect: '+locale);
                this.app.router.navigate(locale+'/'+fragment, {trigger: true, replace: true});
                return false;
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
            var settingsModel = new SettingsModel({
                fee:sessionStorage.getItem('fee'),
                companyName:sessionStorage.getItem('companyName'),
                welcomeMsg:sessionStorage.getItem('welcomeMsg'),
                callbackUrl:sessionStorage.getItem('callbackUrl')
            });
            var feeView = new FeeView({model:settingsModel});
            layout.fee.show(feeView);
            settingsModel.fetch();
        }else if (Controller.loginStatus.get('mobile')){
            view = new ValidateView({model:Controller.loginStatus});
            Communicator.mediator.trigger('app:show', view);
        }else {
            view = new VerifyView({model:Controller.loginStatus});
            Communicator.mediator.trigger('app:show', view);
        }
    });

    Controller.showIndex = function() {
        var header = new IndexHeaderLayout({model:new Backbone.Model({resPath:window.opt.resPath,l:window.getLocale()})});
        var layout = new IndexLayout({model:new Backbone.Model({resPath:window.opt.resPath,l:window.getLocale()})});
        Communicator.mediator.trigger('app:show', layout, header);
        var mobileInput = new MobileInputView({model:new Backbone.Model({resPath:window.opt.resPath})});
        header.mobileInput.show(mobileInput);
        layout.gateways.show(new GatewayCollectionView({collection:this.gateways}));
        var commands = new CommandsView();
        layout.commands.show(commands);
    };

    Controller.showAccount = function(lang, mobile) {
        var header = new AccountHeaderView({mobile:mobile,gateways:this.gateways});
        var layout = new AccountLayout({mobile:mobile,model:new Backbone.Model({l:window.getLocale()})});
        Communicator.mediator.trigger('app:show', layout, header);
        var commands = new CommandsView();
        layout.commands.show(commands);
    };

    Controller.showGateway = function() {
        Communicator.mediator.trigger('app:verify');
    };

    Controller.showAdmin = function() {
        var layout = new AdminLayout();
        Communicator.mediator.trigger('app:show', layout);
        var collection = new AccountCollection();
        layout.account.show(new AdminAccountCollection({collection:collection}));
        collection.fetch();
    };

    Controller.showFaq = function() {
        var view = new FaqView();
        Communicator.mediator.trigger('app:show', view);
    };

    Controller.showAbout = function() {
        var view = new AboutView({model:new Backbone.Model({resPath:window.opt.resPath,l:window.getLocale()})});
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
    Controller.showTerms = function() {
        var contentView = new TermsView({model:new Backbone.Model({l:window.getLocale()})});
        Communicator.mediator.trigger('app:show',contentView);
    };
    Controller.showPrivacy = function() {
        var contentView = new PrivacyView({model:new Backbone.Model({l:window.getLocale()})});
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
    Controller.showCommandHelp = function() {
        var layout = new CommandHelpLayout({model:new Backbone.Model({resPath:window.opt.resPath,l:window.getLocale()})});
        Communicator.mediator.trigger('app:show',layout);
        var commands = new CommandsView();
        layout.commands.show(commands);
    };
    Controller.showNotFound = function() {
        var contentView = new NotFoundView();
        Communicator.mediator.trigger('app:show',contentView);
    };
    Controller.showSignUp = function() {
        var accountRequest = new AccountRequest({ticket:Controller.ticket,l:window.getLocale()});
        var contentView = new SignupView({model:accountRequest});
        Communicator.mediator.trigger('app:show',contentView);
    };
    Controller.showSignupWallet = function() {
        var layout = new SignupWalletLayout({model:new Backbone.Model({l:window.getLocale()})});
        Communicator.mediator.trigger('app:show',layout);
        var mobileInput = new MobileInputView({model:new Backbone.Model({resPath:window.opt.resPath,l:window.getLocale()})});
        layout.mobileInput.show(mobileInput);
    };
    Controller.showSigninWallet = function() {
        var layout = new SigninWalletLayout({model:new Backbone.Model({l:window.getLocale()})});
        Communicator.mediator.trigger('app:show',layout);
        var mobileInput = new MobileInputView({model:new Backbone.Model({resPath:window.opt.resPath,l:window.getLocale()})});
        layout.mobileInput.show(mobileInput);
    };
    Controller.confirmSignUp = function(lang, token) {
        var model = new SignupConf({token:token,l:window.getLocale()});
        var contentView = new SignupConfView({model: model});
        Communicator.mediator.trigger('app:show',contentView);
        model.save();
    };
    Controller.showReset = function() {
        var model = new ResetRequest({ticket:Controller.ticket,l:window.getLocale()});
        var contentView = new ResetView({model:model});
        Communicator.mediator.trigger('app:show',contentView);
    };
    Controller.confirmReset = function(lang, token) {
        var model = new ResetConf({token:token,l:window.getLocale()});
        var contentView = new ResetConfView({model: model});
        Communicator.mediator.trigger('app:show',contentView);
    };

    return Controller;
});
