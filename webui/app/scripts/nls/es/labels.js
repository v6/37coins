define({
    "email" : {
        "ResetSubject" : "Restablecer contraseña",
        "Reset" : "Por favor, haga clic en este enlace para restablecer tu contraseña: {0}",
        "RegisterSubject" : "Mensaje de verificación",
        "Register" : "Mensaje de verificación: {0}",
        "GatewayAlertSubject" : "Puerta de enlace desconectado",
        "GatewayAlert" : "Su {0} Puerta de enlace fue desconectado hace unos minutos. Favor repara operaciones. Vamos a notificar a los clientes después de un paro prolongado.",
        "Byebye" : "Saludos cordiales, adiós\r\n{0}"
    },
    "commands" : {
        "SignupCmd" : ["signupES"],
        "HelpCmd" : ["ayuda"],
        "DepositReqCmd" : ["addr","depósito","dirección"],
        "BalanceCmd" : ["balanza","sal","saldo"],
        "TransactionsCmd" : ["txns","transacciones"],
        "WithdrawalReqCmd" : ["enviar","enviando","enviado"], // Mathan -- Note to self, relabel all SEND commands as either enviar, Enviar, or ENVIAR, as appropriate.
        "VoiceCmd" : ["voice","PIN"],
        "ChargeCmd" : ["pedir","request","cobrar"],
        "ProductCmd" : ["prod","product"],
        "PayCmd" : ["pagar"],
        "PriceCmd" : ["precio"],
        "SellCmd" : ["vender"],
        "BuyCmd" : ["comprar"]
    },
    "sms" : {
        "Signup" : "¡Bienvenido! Bitcoin por SMS. Guardar este número, enviar comandos a esta puerta de enlace. Responder AYUDA para más info {0}",
        "DepositReq" : "{0}",
        "Charge" : "Pagable a través de \"pay {0}\"",
        "Product" : "Payable a través de \"pay {0}\"",
        "DepositNotify" : "{0}mBTC{1} en el camino a su cartera. Gastable en aproximadamente 10 minutos, se notificará cuando se haya completado.",
        "DepositConf" : "Ha recibido {0}mBTC{1}",
        "DepositConfSndr" : "Ha recibido {0}mBTC{1} from {2}",
        "DepositConfSndrMsg" : "Ha recibido {0}mBTC{1} from {2} for \"{3}\"",
        "Balance" : "Tienes {0}mBTC{1} {2}",
        "BalanceInst" : "\r\nReply ADDR, para un dirección Bitcoin",
        "WithdrawalReq" : "Enviar {0}mBTC{1} to {2}?",
        "WithdrawalReqHelp" : "Favor usar ENVIAR <amount> <phone no>\r\nEjemplo: ENVIAR 1.42USD 5558675309\r\n<amount>utilizar XXUSD para EE.UU. valor, o solo XX a indicar mili-bitcoin", 
	// Mathan - I am not sure how to handle this, and whether or not the denominations will be used correctly if they follow the instructions above. 
        "WithdrawalReqPay" : "Paga {0}mBTC{1} a {2} para {3}?",
        "WithdrawalReqIstr" : "Para confirmar, responda con: {0}",
        "WithdrawalConf" : "Hemos transferido {0}mBTC{1} de su cuenta para {2}.",
        "Help" : "ENVIAR límite: 12mBTC antes de PIN disposición\r\nENVIAR comandos: sal, depósito, enviar/solicitar <cantidad> <receptor> [desc]\r\nADDR - Bitcoin depósito address\r\nSAL - mira balanza disponible\r\nPIN - configurar PIN\r\n{0}",
        "Buy" : "Su número se ha añadido como un comprador.",
        "FormatError" : "Tuvimos problemas entendiendo su comando. Por favor, vuelva a enviar en el formato correcto.",
        "InsufficientFunds" : "Usted tiene fondos insuficientes: su balance es: {0} mBTC, y {1} mBTC es requerido para completar la transacción.\r\nPor favor, responda ADDR a obtener su dirección de depósito Bitcoin.", // Had to translate that one with google translate a bit.
        "UnknownCommand" : "Has enviado un comando desconocido. Por favor, responda HELP o {0}",
        "Timeout" : "Ninguna confirmación se ha recibido dentro del tiempo dado. Transaction canceled.",
        "TransactionFailed" : "Transacción falló debido a razones desconocidas.",
        "TransactionCanceled" : "Transacción ha sido cancelado.",
        "Unavailable" : "{0} no está disponible, por favor intente de nuevo en 1 hora. \r\nDisculpa las molestias.",
        "DestinationUnreachable" : "No Puerta de Enlace confiable que se encuentra en el país que está intentando enviar.",
        "BelowFee" : "Disculpe, esta transacción no vale la pena enviar. Por favor, compruebe la cantidad.",
        "AccountBlocked" : "La cuenta está bloqueada",
        "Overuse" : "Disculpe, su mandato se ha encontrado redundante y saltó para evitar un alto costo.",
        "Voice" : "Voce-PIN de seguridad activado correctamente. "
    },
    "voice" : {
        "VoiceHello" : "37 Coins, su billetera mundial.",
        "VoiceSetup" : "Para asegurar sus transacciones grandes, por favor cree un PIN de 4 dígitos.",
        "VoiceCreate" : "Por favor, ingrese un nuevo PIN de 4 dígitos, seguido de la tecla numeral.",
        "VoiceConfirm" : "Por favor, repita su nuevo PIN de 4 dígitos, seguido de la tecla numeral.",
        "VoiceMerchantConfirm" : "Por favor, introduzca el número de 4 dígitos, seguido de la tecla almohadilla.",
        "VoiceMismatch" : "PIN no coincide. Por favor, inténtalo de nuevo.",
        "VoiceSuccess" : "Por favor Recuerde su PIN para transacciones en el futuro.",
        "VoiceEnter" : "Por favor ingrese su PIN de 4 dígitos, seguido de la tecla numeral.",
        "VoiceOk" : "Correcto. Transacción se está ejecutando.",
        "VoiceFail" : "El pasador que has puesto no es correcto, su cuenta será bloqueada después de 3 intentos fallidos.",
        "VoiceRegister" : "Hola desde 37coins. Su código de verificación es $ {payload}. Por favor, ingrese $ {payload} para completar la verificación."
    }
});
