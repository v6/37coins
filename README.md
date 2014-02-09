# 37Coins - SMS Bitcoin Wallet

A product aimed for international remittence market. 

## Server

### Build
   * download [aws sdk for java](http://aws.amazon.com/sdkforjava/)
   * `mvn install:install-file 
    -Dfile=aws-java-sdk-flow-build-tools-<version>.jar 
    -DgroupId=com.amazonaws 
    -DartifactId=aws-java-sdk-flow-build-tools 
    -Dversion=<version> 
    -Dpackaging=jar`
   * `mvn clean package 
	-DldapUrl=... 
	-DldapUser=... 
	-DldapPw=... 
	-DldapBaseDn=... 
	-DimapUser=... 
	-DbasePath=...`

### Run Local
   * `cd server`
   * `mvn jetty:run -Denvironment=test -DswfDomain=... -DaccessKey=... -DsecretKey=... -Dendpoint=... -DsenderMail=... -DbasePath=... -DqueueUri=... -DplivoKey=... -DplivoSecret=... -DimapHost=... -DimapPassword=... -DresPath=... -DldapUrl=... -DldapUser=... -DldapPw=... -DldapBaseDn=... -DcaptchaPubKey=... -DcaptchaSecKey=... -DsrvcPath=... -DamqpUser=... -DamqpPassword=... -DamqpHost=...`

## Web

### Build
   * `npm install`
   * `bower install`
   * `grunt build`

### Run Local

   * `grunt`

## License

GPL3, see LICENSE.txt