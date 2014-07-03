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
    // MOVED "accountLayout"
    "configView" : {
        "dlH2" : "Download App",
        "dlInstP1" : "To operate the gateway, download  <a href=\"https://play.google.com/store/apps/details?id=org.envaya.sms\">EnvayaSMS</a> and configure it with the following parameters.",
        "cfgH2" : "Configure App",
        "fieldHeader" : "Field",
        "valueHeader" : "Value"
    },
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
                "titleH1" : "wallet SignUp",
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