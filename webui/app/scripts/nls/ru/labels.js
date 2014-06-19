define({
    "email" : {
        "ResetSubject" : "пароль восстановить",
        "Reset" : "пожалуйста нажмите на эту ссылку для восстановления вашего пароля: {0}",
        "RegisterSubject" : "Email Верификация",
        "Register" : "пожалуйста нажмите на эту ссылку для подтверждения Email: {0}",
        "GatewayAlertSubject" : "Gateway offline",
        "GatewayAlert" : "Your 37coins gateway went offline a few minutes ago. Please restore operations. We will notify customers after an extended downtime.",
        "Byebye" : "С уважением,\r\n{0}"
    },
    "commands" : {
        "SignupCmd" : ["signupRU"],
        "HelpCmd" : ["помощь"],
        "DepositReqCmd" : ["адр","депозит","адрес"],
        "BalanceCmd" : ["бал","баланс"],
        "TransactionsCmd" : ["сделки","обороты"],
        "WithdrawalReqCmd" : ["послать","отправить"], // Mathan: Note to translators: The SEND command appears a lot in the rest of this file. Write down the relationship before translating, so you can refer to it later if there is not a proper translation elsewhere. 
        "VoiceCmd" : ["voice"],
        "ChargeCmd" : ["заряд"],
        "ProductCmd" : ["продукт"],
        "PayCmd" : ["платить"],
        "PriceCmd" : ["цена"],
        "SellCmd" : ["продать"],
        "BuyCmd" : ["купить"]
    },
    "sms" : {
        "Signup" : "добро пожаловать на! Ваш глобальный портмоне. Bitcoin via SMS. Save this number, send commands to this gateway. Reply HELP for more info {0}",
        "DepositReq" : "{0}",
        "Charge" : "Payable through \"pay {0}\"",
        "Product" : "Payable through \"pay {0}\"",
        "DepositNotify" : "{0}mBTC{1} on the way to your wallet. Spendable in about 10 min, notify when complete.",
        "DepositConf" : "Вы получили {0}mBTC{1} в Ваш портмоне",
        "DepositConfSndr" : "Received {0}mBTC{1} from {2}",
        "DepositConfSndrMsg" : "Received {0}mBTC{1} from {2} for \"{3}\"",
        "Balance" : "в Вашем портмоне {0} mBTC{1} {2}",
        "BalanceInst" : "\r\nReply ADDR, for Bitcoin address",
        "WithdrawalReq" : "Вы хотите отправить {0}mBTC{1} с Вашего счёта на {2}?",
        "WithdrawalReqHelp" : "Please use SEND <amount> <phone no>\r\nEx: SEND 1.42USD 5558675309\r\n<amount>use XXUSD for US value, or just XX to indicate mili-bitcoin",
        "WithdrawalReqPay" : "Pay {0}mBTC{1} to {2} for {3}?",
        "WithdrawalReqIstr" : "Для подтверждения просьба ответить: {0}",
        "WithdrawalConf" : "Мы перевели {0}mBTC{1} с Вашего счёта на {2}.",
	// Mathan - TO DO - The line below needs to be retranslated from its mate in our master file.
        "Help" : "команды: бал, адрес, послать/запрос <количество> <получатель> [описание], сделки, цена, купить, продать, <цена>\r\n{0}", 
        "Buy" : "Ваш номер добавлен в список покупатель",
        "FormatError" : "Мы в затруднении понять ваш запрос. Просьба повторить в правильном формате.",
	// Mathan - TO DO - The line below needs to be retranslated from its mate in our master file. 
        "InsufficientFunds" : "недостаточно средств:\r\nВаш баланс {0} mBTC и требуется {1}mBTC для завершения сделки,\r\nrequired {2}mBTC{3}.\r\nReply ADDR for Bitcoin deposit address",
        "UnknownCommand" : "Вы отправили незнакомую команду. Ответ помощь или {0}",
        "Timeout" : "Нету подтверждения в течение данного времени. Сделка отменяется.",
        "TransactionFailed" : "Сделка неудачна по неизвестной причине.",
        "TransactionCanceled" : "Transaction canceled.",
        "Unavailable" : "{0} is currently unavailable, please try again in 1 hour.\r\nSorry for the inconvenience.",
        "DestinationUnreachable" : "Направление был недоступен. No reliable gateway found in the country you are trying to send to.",
        "BelowFee" : "Эту сделку не стоит пересылать. Снова проверьте сумму.",
        "AccountBlocked" : "счёт заблокирован",
        "Overuse" : "Ваша команда оценена чрезмерной и пропущена, чтобы предотвратить высокие расходы.",
        "Voice" : "безопасность PIN activated successfully"
    },
    "voice" : {
        "VoiceHello" : "37 монет, ваш глобальный портмоне.",
        "VoiceSetup" : "Для обеспечения безопасности крупных сделок, создайте 4-значный ПИН-код.",
        "VoiceCreate" : "Пожалуйста введите новый 4-значный PIN-код, и нажмите клавишу «решетка».",
        "VoiceConfirm" : "Пожалуйста повторите Ваш 4-значный PIN-код и нажмите клавишу «решетка».",
        "VoiceMerchantConfirm" : "Please enter the 4-digit number, followed by the hash key.",
        "VoiceMismatch" : "PIN-код не совпадает, попробуйте еще раз.",
        "VoiceSuccess" : "Пожалуйста запомните этот ПИН-код для будущих сделок. ",
        "VoiceEnter" : "Пожалуйста введите Ваш 4-значный PIN-код и нажмите клавишу «решетка».",
        "VoiceOk" : "Правильно. Сделка выполнена.",
        "VoiceFail" : "PIN-код неправильный, счёт будет заблокирован после 3 неудачных попыток.",
        "VoiceRegister" : "Привет от 37 монет. Ваш проверочный код ${payload}. Пожалуйста введите ${payload} для завершения верификации."
    }
});

//37Coins Resource bundle RUSSIAN
// - A quick primer on JavaScript strings: http://www.quirksmode.org/js/strings.html
// - The template from which this file was translated can be found at webui/app/scripts/nls/root/labels.js in this project as of 20140618. 
// - xxxCMD should not overlap with any other languages
//Author: Lidia Barbie
//Editor: Mathan Basanese
