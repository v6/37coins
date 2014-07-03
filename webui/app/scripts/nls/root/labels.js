define({
    // MOVED "siteNav" : {
    "privacy" : {
        "h1" : "Privacy Policy",
        "h2" : "Personal Information"
    },
    "aboutView" : {
        "about" : "About 37coins",
        "desc" : "We are committed to making bitcoin easy, secure to use, and accessible to everyone. With a global focus on social equity, we develop down-teched, secure, scalable solutions that are as appropriate for emerging economies as they are for developed ones.",
        "team" : "Team",
        "role1" : "Social Entrepreneur",
        "role2" : "Community and Content Manager",
        "role3" : "Developer",
        "role4" : "Designer"
    },
    // MOVED "accountLayout"
   // MOVED "configView" : {
    // MOVED "balanceView"
    // MOVED "captchaView"
    "commandSendView" : {
        "messageType" : "command",
        "syntax" : "SEND <amount> <address> [comment]",
        "p1" : "The SEND command sends bitcoins from your SMSwallet account to another SMSwallet or bitcoin wallet.",
        "p2" : "The recipient does not need to have an account with us. If you send bitcoins to a phone number that does not have a SMSwallet account, one will be created for them and they will be notiﬁed.",
        "table2" : {
            "column1" : "Variable",
            "column2" : "Description",
            "td1" : "<amount>",
            "td2" : "The amount to send to the recipient. Use <em>12000</em> to indicate 12,000 bits, or use <em>5USD</em> to indicate sending bitcoins in the value of USD 5.",
            "td3" : "<address>",
            "td4" : "The destination address to send the bitcoins. Can be either a phone number or a bitcoin address.",
            "td5" : "[comment]",
            "td6" : "Optional comment, up to 40 characters"
        }
    },
    // MOVED "cmdHelpL"
    // MOVED "indexLayout" : {
    "loginView" : {
        "gwCallToAction" : "Do you have a spare Android phone and want to run an SMSgateway in your country?",
        "signUpLinkLabel" : "Sign Up",
        "registrationExplanation" : "<ul>The registration process will take you through:<li>Setting up an android phone</li><li>Choosing a transaction fee</li><li>Testing your gateway</li></ul>",
        "walletBtn" : "Open a Wallet",
        "commands" : "Commands are not case-sensitve and are sent to the gateways via SMS/text. To learn more, visit the SMSwallet",
        "link" : "help page",
        "gw1" : "A decentralized, open-source Android app that bridges between SMS and Bitcoin. Designed for low-end Android phones. In other words, you can connect your country to the global economy via Bitcoin (and earn a little for your time and effort, too. ;-).",
        "gw2" : "Gateways are operated by partners who earn transaction fees. To become an SMSgateway partner, the following are required:",
        "gwLi" : ["Dedicated Android phone","Text messaging plan","Internet connection"],
        "gwInv" : "Become an SMSgateway partner",
        "inputEmail" : {
            "label" : "Email Address",
            "placeholder" : "Enter email"
        },
        "inputPassword" : {
            "label" : "Password:",
            "placeholder" : "Enter password"
        },
        "form-signin-heading" : "Please Login:",
        "loginBtn" : "Login",
        "gwBtn" : "Open a Gateway",
        "newGwPrompt" : "New Gateway?",
        "reset" : {
            "prompt" : "Forgot password?",
            "linkTitle" : "Reset"
        },
        "error" : {
            "badPass" : "<strong>Error!</strong> Incorrect Email or Password!"
        },
        "wallet" : {
            "login" : {
                "title2" : "wallet Sign in",
                "label" : "Enter the phone number for your existing account.",
                "gwPrompt" : "Wanted to sign in to your SMSgateway account?",
                "gwLink" : "Click here."
            },
            // MOVED "signUp" : {
            "signUpConf" : {
                "signUpH2" : "Sign Up",
                "success" : "Success",
                "created" : "Account created.",
                "linkLabel" : "Get started",
                "linkLabelPunct" : "!",
                "sad" : "Error!",
                "sadMsg" : "Account creation failed. Please",
                "contactSupport" : "contact support"
            }
        }
        // MOVED "signup" :
    },
    "title" : {
        "wildcards" : ["confSignup","confReset","account"],
        "t-index" : "37coins",
        "t-notFound" : "37coins - not Found",
        "t-account-*" : "37coins Account"
    },
    "desc" : {
        "d-index" : "A bitcoin Wallet for the Low-teched and Unbanked. It makes financial transactions as simple as sending a text.",
        "d-notFound" : "This page does not exist",
        "d-account-*" : "An account page"
    },
    "email" : {
        "ResetSubject" : "Password Reset",
        "Reset" : "Please click this link to reset your password: {0}",
        "RegisterSubject" : "Email Verification",
        "Register" : "Please click this link to verify your email: {0}",
        "GatewayAlertSubject" : "Gateway offline",
        "GatewayAlert" : "Your 37coins gateway went offline a few minutes ago. Please restore operations. We will notify customers after an extended downtime.",
        "Byebye" : "Best regards,\r\n{0}"
    },
    "commands" : {
        "SignupCmd" : ["signup"],
        "HelpCmd" : ["help","hlp"],
        "DepositReqCmd" : ["addr","deposit","adr","address","adress"],
        "BalanceCmd" : ["balance","bal","blance","balnce"],
        "TransactionsCmd" : ["txns","transactions","trans","tran"],
        "WithdrawalReqCmd" : ["send","sending","sent"],
        "VoiceCmd" : ["voice","pin"],
        "ChargeCmd" : ["req","request","charge"],
        "ProductCmd" : ["prod","product"],
        "PayCmd" : ["pay"],
        "PriceCmd" : ["price"],
        "SellCmd" : ["sell"],
        "BuyCmd" : ["buy"]
    },
    // MOVED "brand" : {
    "sms" : {
        "Signup" : "Welcome to 37coins BETA! Bitcoin via SMS. Save this number, send commands to this gateway. Reply HELP for more info {0}",
        "DepositReq" : "{0}",
        "Charge" : "Payable through \"pay {0}\"",
        "Product" : "Payable through \"pay {0}\"",
        "DepositNotify" : "{0}{2}{1} on the way to your wallet. Spendable in about 10 min, notify when complete.",
        "DepositConf" : "Received {0}{2}{1}",
        "DepositConfSndr" : "Received {0}{3}{1} from {2}",
        "DepositConfSndrMsg" : "Received {0}{4}{1} from {2} for \"{3}\"",
        "Balance" : "Available balance {0}{3}{1} {2}",
        "BalanceInst" : "\r\nReply ADDR, for bitcoin address",
        "WithdrawalReq" : "Send {0}{3}{1} to {2}?",
        "WithdrawalReqHelp" : "Please use SEND <amount> <phone no>\r\nEx: SEND 1.42USD 5558675309\r\n<amount>use XXUSD for US value, or just XXX to indicate bits",
        "WithdrawalReqPay" : "Pay {0}{4}{1} to {2} for {3}?",
        "WithdrawalReqIstr" : "To confirm transaction, reply {0}",
        "WithdrawalConf" : "Transferred {0}{3}{1} to {2}.",
        "Help" : "SEND limit: 12USD before PIN setup\r\nADDR - bitcoin deposit address\r\nBAL - see available balance\r\nPIN - setup PIN\r\n{0}",
        "Buy" : "Your number has been added as a buyer.",
        "FormatError" : "We had trouble understanding your request. Please resend in the correct format.",
        "InsufficientFunds" : "Insufficient funds:\r\nAvailable balance {0}{4}{1},\r\nrequired {2}{4}{3}.",
        "UnknownCommand" : "You have sent an unknown command. Reply HELP or {0}",
        "Timeout" : "Confirmation response not received in time. Transaction canceled.",
        "TransactionFailed" : "Transaction failed due to an unknown reason.",
        "TransactionCanceled" : "Transaction canceled.",
        "Unavailable" : "{0} is currently unavailable, please try again in 1 hour.\r\nSorry for the inconvenience.",
        "DestinationUnreachable" : "No reliable gateway found in the country you are trying to send to.",
        "BelowFee" : "This transactions is not worth sending. Double-check the amount.",
        "AccountBlocked" : "Account blocked",
        "Overuse" : "Your command was found redundant and skipped to prevent high cost.",
        "Voice" : "Security PIN activated successfully"
    },
    "voice" : {
        "VoiceHello" : "37 Coins, your global wallet.",
        "VoiceSetup" : "To secure large transactions, create a secret 4-digit PIN.",
        "VoiceCreate" : "Please enter a new 4-digit PIN, followed by the hash key.",
        "VoiceConfirm" : "Please reenter your new 4-digit PIN, followed by the hash key.",
        "VoiceMerchantConfirm" : "Please enter the 4-digit number, followed by the hash key.",
        "VoiceMismatch" : "The PIN does not match, please try again.",
        "VoiceSuccess" : "Please remember this PIN for future transactions.",
        "VoiceEnter" : "Please enter your 4-digit PIN, followed by the hash key.",
        "VoiceOk" : "The PIN is correct. Transaction executing.",
        "VoiceFail" : "The PIN is not correct. The account will be blocked after 3 failed attempts.",
        "VoiceRegister" : "Hello from 37 coins. Your verification-code is ${payload}. Please enter ${payload} to complete verification."
    },
    // MOVED "faq"
    "form" : {
        "gwSetup" : {
            "ver" : {
                "titleH2" : "Gateway Ownership Verification",
                "inputCode" : {
                    "label": "Verification Code:",
                    "help-block" : "Type the 5-digit code you received during the verification call.",
                    "confirmBtn" : "Confirm Ownership",
                    "sad" : "Error!",
                    "sadExpln" : "Submitted code not valid."
                },
                "inputTel" : {
                    "label" : "Gateway Mobile Number:",
                    "placeholder" : "+CCXXXXXX...",
                    "help-block" : "Enter your gateway's number in international format. Avoid special signs and spaces. You will receive a call, telling your 5-digit confirmation code.",
                    "startBtn" : "Start Verification",
                    "sad" : "Error!",
                    "sadExpln" : "Communication Error."
                }
            }
        }
        //MOVED "gwSet" : {
    } ,
    // MOVED "gatewayCollectionView" : {
    // MOVED "logoutView" : {
    // MOVED "merchant" :
    // MOVED    "mobileInput" : {
    // MOVED "reset" : {
});