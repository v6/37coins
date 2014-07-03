define({
    "siteNav" : {
        "root" : {
            "products" : "products",
            "help" : "help",
            "about" : "about",
            "terms" : "terms",
            "privacy" : "privacy",
            "bf" : "Bitfinger [coming soon]",
            "signIn" : "sign in",
            "signUp" : "sign up"
        },
        "notFound" : {
            "h1" : "Not found",
            "face" : ":(",
            "sorry" : "Sorry, but the page you were trying to view does not exist.",
            "cause" : {
                "expln" :"It looks like this was the result of either:",
                "addr" : "a mistyped address",
                "link" : "an out-of-date link"
            },
            "backToHome" : "Continue to Index"
        },
        "terms" : "Terms of Use",
        "termsH1" : "37coins Terms of Service",
        "privTerms" : "Privacy Policy"
    },
    "privacy" : {
        "h1" : "Privacy Policy",
        "h2" : "Personal Information"
    },
    "social" : {
        "heading" : "follow us on"
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
    "accountLayout" : {
        "expl" : "Commands are not case-sensitive and are sent to the gateways via SMS/text. To learn more, visit the SMSwallet "
    },
    "configView" : {
        "dlH2" : "Download App",
        "dlInstP1" : "To operate the gateway, download  <a href=\"https://play.google.com/store/apps/details?id=org.envaya.sms\">EnvayaSMS</a> and configure it with the following parameters.",
        "cfgH2" : "Configure App",
        "fieldHeader" : "Field",
        "valueHeader" : "Value"
    },
    "balanceView" : {
        "balH2" : "Account Balance",
        "bal" : "Your account balance is",
        "amountLbl" : "Withdrawal amount:",
        "amountPh" : "Enter amount",
        "addressLbl" : "Withdrawal address:",
        "addressPh" : "Enter withdrawal address",
        "withdrawalBtn" : "Withdrawal",
        "error" : "Communication Error!",
        "success" : "Request Submitted."
    },
    "captchaView" : {
        "captchaH2" : "Verify Captcha!",
        "captcha" : "Your IP has made multiple requests. Please type both words displayed by the captcha to verify that you are not a bot.",
        "error" : "Error!",
        "errorMsg" : "Incorrect Solution! Try again.",
        "captchaBtn" : "Done!"
    },
    "commandSendView" : {
        "messageType" : "command",
        "syntax" : "SEND <amount> <address> [comment]",
        "p1" : "The SEND command sends bitcoins from your SMSwallet account to another SMSwallet or bitcoin wallet.",
        "p2" : "The recipient does not need to have an account with us. If you send bitcoins to a phone number that does not have a SMSwallet account, one will be created for them and they will be notiﬁed.",
        "table2" : {
            "column1" : "Variable",
            "column2" : "Description",
            "td1" : "<amount>",
            "td2" : "The amount to send to the recipient. Use <em>12000</em> to indicate 12,000 bits, or use <em>5USD</em> to indicate sending BTC in the value of USD 5.",
            "td3" : "<address>",
            "td4" : "The destination address to send the bitcoins. Can be either a phone number or a bitcoin address.",
            "td5" : "[comment]",
            "td6" : "Optional comment, up to 40 characters"
        }
    },
    "cmdHelpL" : {
        "h1" : "Help",
        "refH2" : "Quick Reference",
        "cmdH2" : "Commands",
        "faqH3": "FAQ",
        "cmd" : "Commands are not case-sensitve and are sent to the gateways via SMS/text.",
        "w" : "Recognized shorthand or misspellings, English (EN): ",
        "addr" : {
            "h4" : "ADDRESS",
            "p1" : "The ADDRESS command returns the most recent bitcoin address associated to your SMSwallet account.",
            "p2" : "With this bitcoin address, you can deposit bitcoins from another bitcoin wallet. If you don’t have any bitcoins, you can purchase them from  <a href=\"https://localbitcoins.com/buy-bitcoins-online/?ch=1zol\">Localbitcoins</a> or <a href=\"https://coinbase.com/?r=5367c0e6c9f6f52297000082&utm_campaign=user-referral&src=referral-link\">Coinbase</a>"
        },
        "bal" : {
            "column1" : "System Response",
            "column2" : "Implication",
            "systemResponse1" : "Balance 12,048bit ($5.24)<br/>Reply ADDR, for bitcoin address",
            "implication1" : "You can SEND up to 12,048 bits worth of bitcoins to another phone number or bitcoin address",
            "label1" : "*bitcoin to US Dollar values, as of 5/4/2014",
            "p1" : "The BALANCE command returns the current bitcoin value of your SMSwallet.",
            "p2" : "The balance reported already reflects the potential transaction fee. Therefore it reflects the maximum amount of bitcoins that you can transfer to another SMSwallet."
        },
        "send": {
            "anchor" : "SEND",
            "syntax" : "SEND command syntax",
            "caption" : "SEND <amount> <address> [comment]",
            "p1": "The SEND command sends bitcoins from your SMSwallet account to another SMSwallet or bitcoin wallet.",
            "p2": "The recipient does not need to have an account with us. If you send bitcoins to a phone number that does not have a SMSwallet account, one will be created for them and they will be notified.",
            "p3": "<strong>Ensure that you typed the recipients phone number or bitcoin address correctly. Once sent, the transaction cannot be reversed.</strong>",
            "address" : {
                "td1" : "<address>",
                "td2" : "The destination address to send the bitcoins. Can be either a phone number or a bitcoin address. Phone numbers may not have spaces. If sending internationally, include the country code and the plus (+) sign. I.e. +63 for a Philippines number."
            },
            "amount" : {
                "td1" : "<amount>",
                "td2" : "The amount to send to the recipient. Use <em>12000</em> to indicate 12,000 bits, use <em>5USD</em> to indicate sending bitcoins in the value of 5 US dollars. (You can use any of the 3 character currency code codes, such as PHP, EUR, …)"
            },
            "comment" : {
                "td1": "[comment]",
                "td2": "Optional comment, up to 40 characters. Messages that are too long will be shortened."
            },
            "example" : {
                "header" : "SEND command examples",
                "column1" : "Example",
                "column2" : "Command explanation",
                "ex1" : {
                     "caption" : "Sending bitcoin domestically (from a US SMSwallet user)",
                     "td1" : "SEND 1USD 6165551234",
                     "td2" : "This requests the system to send 1 US dollar worth of bitcoin to the US phone number 6165551234."
                },
                "ex2" : {
                    "caption" : "Sending bitcoins overseas (from a US SMSwallet user)",
                    "td1" : "SEND 44PHP +639125551234",
                    "td2" : "This requests the system to send 44 Philippine Pesos worth of bitcoin to the PH phone number +639125551234."
                },
                "ex3" : {
                    "caption" : "Sending with shorthand (from a US SMSwallet user)",
                    "td1" : "6300 6165551234",
                    "td2" : "This requests the system to send 6,300 bits to the phone number 6165551234."
                },
                "ex4" : {
                    "caption" : "Sending to a bitcoin address",
                    "td1" : "SEND 12USD 19xeDDxhahx4f32WtBbPwFMWBq28rrYVoh",
                    "td2" : "This requests the system to send 12USD worth of bitcoins to the bitcoin address 19xeDDxhahx4f32WtBbPwFMWBq28rrYVoh."
                 }
             }
        },
        "transaction": {
            "anchor" : "TRANSACTION",
            "syntax" : "TRANSACTION command syntax",
            "caption" : "TRANSACTION",
            "p1" : "The TRANSACTION command returns the history of your last three transactions.",
            "p2" : "If you have no transactions yet, this will show nothing, or you will receive an error message.",
            "p3" : "If an expected transaction is missing, make sure that the transaction has received a confirmation message, and ensure that you typed the recipient\\'s phone number or bitcoin address correctly.",
            "p4" : "Transactions on your transactions list are final. Transactions cannot be reversed.",
            "address" : {
                "td1" : "<address>",
                "td2" : "The destination address to send the bitcoins. Can be either a phone number or a bitcoin address. Phone numbers may not have spaces. If sending internationally, include the country code and the plus (+) sign. I.e. +63 for a Philippines number."
            },
            "amount" : {
                "td1" : "<amount>",
                "td2" : "The amount to send to the recipient. Use <em>12000</em> to indicate 12,000 bits, use <em>5USD</em> to indicate sending bitcoins in the value of 5 US dollars. (You can use any of the 3 character currency code codes, such as PHP, EUR, …)"
            },
            "comment" : {
                "td1": "[comment]",
                "td2": "Optional comment, up to 40 characters. Messages that are too long will be shortened."
            },
            "example" : {
                "header" : "TRANSACTION command examples",
                "column1" : "System Response",
                "column2" : "Implication",
                "ex1" : {
                    "caption" : "",
                    "td1" : "",
                    "td2" : ""
                }
            }
        },
        "pin": {
            "anchor": "PIN",
            "syntax": "PIN command syntax",
            "caption": "PIN",
            "p1": "The PIN command sets up the Security PIN for your account.",
            "p2": "The Security PIN is a 4-digit code that you assign. It protects your account from unauthorized spending.",
            "p3": "If a Security PIN is not already set up, the system will call you and guide you through the process. <!--If the Security PIN is already set up, the system will call you, confirm your existing Security PIN, and then guide you through setting up a different security PIN.-->  When you receive the voice call to setup your PIN, the system will prompt you to dial your 4-digit code, followed by the hash key (#).",
            "p4": "Currently the Security PIN is required if you try to SEND more than 12USD in one day. In the future, you will be able to adjust this amount."
        },
        "price": {
            "anchor" : "PRICE",
            "syntax" : "PRICE command syntax",
            "caption" : "PRICE",
            "p1" : "The PRICE command returns the current bitcoin exchange rate for your region’s common fiat currency. The exchange rate is calculated from <a href=\"https://bitcoinaverage.com\">Bitcoinaverage.com</a>",
            "p2" : "This may not reflect the price available locally, or through your country's merchants and gateways."
        },
        "faq" : {
            "question1" : "What is",
            "answer1" : {
                "p1" : "is a simple way to send bitcoin to anyone's mobile phone number, even international phone numbers. Gateways connect a country's SMS-network to the Bitcoin network. This means that you do not need a smartphone or even a web-able phone to use bitcoin! You can send your bitcoin to anyone who has a phone number no matter where they are!"
            },
            "question2" : "Is there a fee for using the SMSwallet system?",
            "answer2" : {
                "title1" : "Sending between SMSwallet users",
                "p1" : "Right now there is only a fee for transactions that are initiated with the SEND command. These fees are set by the SMSgateway operators and reflect the cost of operating in their country (and market competition). Currently there is no fee for use of the Security PIN feature.",
                "p2" : "The sender always pays the transaction fee and is assessed from the sender's gateway operator. So, the fee is the same if you send bitcoins to a domestic phone number or an international phone number.",
                "title2" : "Sending to a bitcoin address",
                "p3" : "If sending to an external bitcoin address, there will be an additional Blockchain transaction fee, in addition to the gateway fee. The Blockchain transaction fee depends on the size of the transaction."
            },
            "question3" : "After depositing bitcoins to my SMSwallet, why is my my balance less?",
            "answer3" : {
                "p1" : "The system only shows the amount of bitcoins that you can send. To send bitcoins through 37coins, there is a small transaction fee. Therefore, if the transaction fee of your gateway is 100 bits (2014/05/04: $0.04) per transaction, then your account can only send [deposit amount] - [trans fee] = [available balance].",
                "p2" : "Your \'available balance\' already calculates the future transaction fee."
            },
            "question4" : "I want to open an SMSwallet, do I need to run an SMSgateway?",
            "answer4" : {
                "p1" : "No, if you just want to open a wallet, just choose a gateway from your country and send a text message to it. Opening an SMSgateway will not give you an SMSwallet.",
                "p2" : "If no gateway exists in your country, think about opening one. It will allow people to use your gateway's phone number to open wallets and send to international phone number or bitcoin address. Also, you will earn the fees for transactions through your gateway. Once again, if you open a gateway, and you want to open an SMSwallet, you will need another phone. To find out more about gateways, check out the SMSgateway FAQ, or <a href=\"/{{l}}/gateways\">sign up</a>."
            },
            "question5" : "How is this secure? Haven’t you heard of IMSI-catcher? What if I get hacked?",
            "answer5" : {
                "p1" : "SMSwallet operates on a shared responsibility model and multi-factor authentication for security. To protect the SMSwallets from spoofing, the SMSgateways sends a challenge back to the phone to verify the request. Then a transaction is constructed and signed with the SMSgateway’s private key. The input(s) to this transaction is not spendable without the signature of both, the gateway and the web-service. For large value transactions, the web-service initiates a voice-call with the client to verify the client's spending PIN. Instead of confirming the message via SMS/text, you enter a Secret PIN with dial-tones via a voice call. Once successful, the web-service adds its part of the signature and the transaction can be spent. Throughout the process, the client is required to verify the possession of the phone as well as the knowledge of the spending PIN, giving him two factors for authentication. In other words, IMSI-catcher would be ineffective in Security PIN protected transactions. ",
                "p2" : "However, in the current MVP implementation no shared responsibility model is implemented yet. The gateway is dumb and the web-service has to be fully trusted."
            }
        }
    },
    "indexLayout" : {
        "mobileTag" : "Money without borders or barriers, as easy as sending an SMS. Send and receive bitcoin anywhere in the World. No internet or smartphone required.",
        "header" : {
            "_comment" : "This may be deprecated.",
            "mobileTag" : "<br>Without borders or barriers,<br/> as easy as sending an SMS",
            "tag" : "Send and receive bitcoin<br/> anywhere in the world via SMS.<br/> No internet or smartphone required.",
            "walletInv" : "Sign-up to be an SMSwallet beta user.",
            "terms" : {
                "prompt": "By clicking Go!, you agree to the",
                "label": "terms of use"
            }
        },
        "wallet" : "After setting up your account, you will receive an automated text message from a gateway. To control your account, send any of these commands to that phone number via SMS.",
        "walletInv" : "Become an SMSwallet beta user",
        "walletBtn" : "Open a Wallet",
        "commands" : "Commands are not case-sensitve and are sent to the gateways via SMS/text. To learn more, visit the SMSwallet",
        "link" : "help page",
        "gw1" : "A decentralized, open-source Android app that bridges between SMS and Bitcoin. Designed for low-end Android phones. In other words, you can connect your country to the global economy via Bitcoin (and earn a little for your time and effort, too. ;-).",
        "gw2" : "Gateways are operated by partners who earn transaction fees. To become an SMSgateway partner, the following are required:",
        "gwLi" : ["Dedicated Android phone","Text messaging plan","Internet connection"],
        "gwInv" : "Become an SMSgateway partner",
        "gwBtn" : "Open a Gateway"
    },
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
            "signUp" : {
                "SMS" : "SMS",
                "titleH1" : "wallet Signup",
                "titleH2" : "Please Sign up",
                "promptBeta" : "Join the SMSwallet beta.",
                "happy" : "You have successfully submitted a registration request.",
                "alert" : "Attention!",
                "alertMsg" : "To complete registration please check your email inbox and validate you email address."
            },
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
        },
        "signup" : {
            "titleH2" : "Please Sign up",
            "InputEmail1" : {
                "label" : "Email Address:",
                "placeholder" : "Enter email"
            },
            "password1" : {
                "label" : "Password:",
                "placeholder" : "Enter password"
            },
            "password2" : {
                "label" : "Password:",
                "placeholder" : "Repeat password"
            },
            "signUpBtn" : "Sign up",
            "sad" : "Error!",
            "sadLinkPrompt" : "Please",
            "sadLinkLabel" : "contact support"
        }
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
        "BuyCmd" : ["buy"],
        "DepositReqCmd1" : "ADDR",
        "VoiceCmd1" : "PIN",
        "BalanceCmd1" : "BAL",
        "WithdrawalReqCmd1" : "SEND",
        "TransactionsCmd1" : "TRAN",
        "HelpCmd1" : "HELP",
        "smlHlp" : {
            "column1" : "Command",
            "column2" : "Description",
            "SignupCmd" : "Signup",
            "HelpCmd" : "List available commands",
            "DepositReqCmd" : "Get bitcoin deposit address",
            "BalanceCmd" : "Get balance in bits",
            "TransactionsCmd" : "List the last 3 transactions",
            "WithdrawalReqCmd" : "Send bitcoins to phone number, bitcoin address",
            "VoiceCmd" : "Setup Secret PIN",
            "ChargeCmd" : "Charges",
            "ProductCmd" : "Lists products",
            "PayCmd" : "Pays bitcoin",
            "PriceCmd" : "Gets the price",
            "SellCmd" : "Sets up a sale of bitcoin",
            "BuyCmd" : "Sets up for buying bitcoin"
        }
    },
    "brand" : {
        "SMSwallet" : "SMSwallet"
    },
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
    "faq" : {
        "gw" : {
            "titleH1" : "SMSgateway FAQ:",
            "q1WhyGateway" : "What is ‘SMSgateway’? Why is it needed?",
            "a1WhyGateway" : {
                "p1" : "‘SMSgateway’ is an android app that is a mediator between any local SMS network and the internet. Each gateway has the power to connect a country’s SMS network to the Bitcoin network; and, thus, to the global economy. It can be operated by anyone willing to invest in an Android phone, install the app, and take care of its connectivity. This person is the SMSgateway partner. The partner’s efforts are rewarded with a small reward from any transaction that is initiated through their gateway. The more gateways that are set up, the bigger the reach of '37coins network' and the more resilient it will be to hardware failure or a malicious attack. The SMSgateway is designed as a regular client to the mobile provider's network. No approval, co-operation, or agreement with the network provider, except a paid SIM-card, is necessary for set up.",
                "p2" : "The SMSwallet users do NOT need a smart phone. They can open a wallet with an gateway by simply sending it an SMS from any feature-phone."
            },
            "q2WorkTogether" : "How do SMSgateway and SMSwallet work together?",
            "a2WorkTogether" : {
                "p1" : "'SMSwallet' brings as much security as affordable to allow a client (the user with the simple feature phone) on an untrusted network to control a bitcoin wallet via SMS. The client sends simple text commands via SMS. The gateway verifies those commands, constructs a transaction and forwards it to the web-service. The web-service verifies, independently from the gateway, and then publishes the transaction to the Bitcoin network. It may seem complicated, but for the end-user it’s really as simple as sending an SMS. And it is secure because we use the phone’s SIM identification through the SMS channel; and, if necessary, we use a Secret PIN as another identifier. The Secret PIN is entered using dial tones via a voice call."
            },
            "q3WhatPhone" : "Can I run a gateway on my every-day phone?",
            "a3WhatPhone" : {
                "p1" : "No, that is not a good idea. The SMSgateway app consumes a lot of battery and forwards all arriving SMS —even your private ones— to the internet. Use a dedicated phone (best some obsolete 1st generation Android) to run the gateway in a secure place, connected to internet and electricity at all times. The users of your SMSgateway will thank you."
            },
            "q4Profit" : "I run an SMSgateway, what is the average payout per month?",
            "a4Profit" : {
                "p1" : "If you don't acquire customers and promote your SMSgateway’s number, you will not see any payout at all. Your gateway is like a small business, keep 100% uptime and spread the word."
            },
            "q5WhatNow" : "I opened a new SMSgateway, what now?",
            "a5WhatNow" : {
                "p1" : "Promote your service! Once you have your SMSgateway fully operational, open an SMSwallet from a different phone. In other words, send a text from another phone to your SMSgateway’s number. This will create a new SMSwallet account. You will now have an SMSwallet phone and an SMSgateway phone. From your SMSwallet phone, request your bitcoin address by sending the “ADDRESS” command. Send bitcoins to this address. Once your wallet is charged, you can demonstrate the service and amaze your friends by inviting them by sending them tiny fractions of a bitcoin."
            },
            "q6HowSecure" : "How is this secure? Haven’t you heard of IMSI-catcher? What if I get hacked?",
            "a6HowSecure" : {
                "p1" : "SMSwallet operates on a shared responsibility model and multi-factor authentication for security. To protect the SMSwallets from spoofing, the SMSgateways sends a challenge back to the phone to verify the request. Then a transaction is constructed and signed with the SMSgateway’s private key. The input(s) to this transaction is not spendable without the signature of both, the gateway and the web-service. For large value transactions, the web-service initiates a voice-call with the client to verify the client's spending PIN. Once successful, the web-service adds its part of the signature and the transaction can be spent. Throughout the process, the client is required to verify the possession of the phone as well as the knowledge of the spending PIN, giving him two factors for authentication. In other words, IMSI-catcher would be ineffective in spending-PIN protected transactions.",
                "alert-warning1" : "However, in the current MVP implementation no shared responsibility model is implemented yet. The gateway is dumb and the web-service has to be fully trusted."
            }
        }
    } ,
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
        },
        "gwSet" : {
            "h2" : "Gateway Settings",
            "feeInput" : {
                "label" : "Transaction Fee:",
                "help-block" : "Choose a transaction fee payed to you by the user for each successful transaction."
            },
            "msgInput" : {
                "label" : "Transaction Fee:",
                "help-block" : "Define the welcome message a new user receives."
            },
            "urlInput" : {
                "label" : "Company Url:",
                "help-block" : "Set the name of your company."
            },
            "callbackInput" : {
                "label" : "Signup Callback:",
                "help-block" : "Notify your server about new Users."
            },
            "commErr" : "<strong>Error!</strong> Communication Error! Please try again later, from a different browser, or contact support.",
            "success" : "<strong>Success!</strong> Settings updated."
        }
    } ,
    "gatewayCollectionView" : {
        "numberTh1": "Number",
        "feeTh2": "Fee"
    },
    "logoutView" : {
        "success" : {
            "alert" :"strong>Success!</strong> Logout successfull.",
            "alert-link" :"Continue to Login"
        }
    },
    "merchant" :
    {
        "success" : {
            "alert" : "<strong>Success!</strong> Merchant account successfully verified.",
            "InputName1" : {
                "label": "Display Name:",
                "placeholder": "Enter display name."
            },
            "button" : "Set Name",
            "error" : {
                "prompt" : "<strong>Error!</strong> Please",
                "label" : "contact support"
            },
            "instructions" : "Try out your new account! Sign up some users and let us know what you think!"
        },
        "verify" : {
            "h2" : "Account Verification",
            "desc" : "We are going to call you at your phone number to verify your mobile. Click <b>Call me now</b> below and enter the 4-digit code that will be displayed to you.",
            "searchInput" : {
                "label" : "Phone Number:",
                "placeholder" : "+XXYYYYYYYY"
            },
            "disabledInput" : {
                "label" : "Code:",
                "placeholder" : "4 digit code here..."
            },
            "verBtn" : "Call me now",
            "merchStatus1" : "Please insert your phone number."
        }
    },
    "mobileInput" : {
        "descCountry" : "Country",
        "descNumber" : "Mobile Number",
        "success" : "Valid",
        "error" : "Invalid number"
    },
    "reset" : {
        "happy" : "Success!",
        "submitted" : "Password request submitted. Please check your email inbox and follow the provided instructions.",
        "sad" : "Error!",
        "failMsg" : "Passwords reset URL no longer valid. Please",
        "supp" : "contact support",
        "newPass" : {
            "h2" : "Please Choose a new Password",
            "form" : {
                "password" : {
                    "label" : "Password:",
                    "placeholder" : "Enter password"
                },
                "password2" : {
                    "label" : "Password:",
                    "placeholder" : "Repeat password"
                },
                "saveBtn" : "Save",
                "error" : "Error!",
                "unMatched" : "Passwords have to match!"
            }
        },
        "resetReqForm" : {
            "titleH2": "Identify Account for Reset",
            "exampleInputEmail1" : {
                "label" : "Email address:",
                "placeholder" : "Enter email",
                "help-block" : "Type your account's email address to receive a reset code.",
                "resetBtn" : "Reset Password"
            },
            "ohNoez" : "Error!",
            "notFound" : "Account not found. Try again."
        }
    }
});