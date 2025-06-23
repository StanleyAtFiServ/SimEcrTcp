SimEcrTcp - A simulator of ECR to send application message on top of TCP
Run on window command prompt
1. After building, we have SimEcrTcp.jar, 
2. Install jdk 1.7, example jdk1.7.0_80
3. Use relative path java path to run ..\jdk1.7.0_80\bin\java -jar SimEcrTcp.jar payment.txt localhost 8081 com3
   where
     -payment.txt is request application message to be sent out from SimEcrTcp.
     -localhost is target host ip
     -8081 target host tcp port
     -com3 RS232 port
