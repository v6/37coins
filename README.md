# 37Coins - SMS Bitcoin Wallet

A product aimed for international remittence market. 

## Server

### Build

mvn clean package -DldapUrl=... -DldapUser=... -DldapPw=... -DldapBaseDn=... -DimapUser=... -DbasePath=...

### Run Local

messanger/mvn jetty:run -Denvironment=test -DswfDomain=... -DaccessKey=... -DsecretKey=... -Dendpoint=... -DsenderMail=... -DbasePath=... -DqueueUri=... -DplivoKey=... -DplivoSecret=... -DimapHost=... -DimapPassword=... -DresPath=... -DldapUrl=... -DldapUser=... -DldapPw=... -DldapBaseDn=... -DcaptchaPubKey=... -DcaptchaSecKey=... -DsrvcPath=... -DamqpUser=... -DamqpPassword=... -DamqpHost=...

## Web

### Build

npm install
bower install
grunt build

### Run Local

grunt

## License

GPL3, see LICENSE.txt