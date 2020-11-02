#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <EEPROM.h>
#define A0 D0
#define A1 D1
#define A2 D2
#define A3 D3
#define A4 D4
#define A5 D5
#define A6 D6
#define A7 D7


/************************* Adafruit.io Setup *********************************/

WiFiClient client;

//////////////////////
// WiFi Definitions //
//////////////////////
const char WiFiAPPSK[] = "123456789";


WiFiServer server(80);

void setup() 
{
  Serial.begin(115200);
  setupWiFi();
  server.begin();
      
    pinMode(A0, OUTPUT);
    pinMode(A1, OUTPUT);
    pinMode(A2, OUTPUT);
    pinMode(A3, OUTPUT);
    pinMode(A4, OUTPUT);
    pinMode(A5, OUTPUT);
    pinMode(A6, OUTPUT);
    pinMode(A7, OUTPUT);
  
    digitalWrite(A0, LOW); 
    digitalWrite(A1, LOW); 
    digitalWrite(A2, LOW);
    digitalWrite(A3, LOW);
    digitalWrite(A4, LOW);
    digitalWrite(A5, LOW);
    digitalWrite(A6, LOW);
    digitalWrite(A7, LOW);
  
 
   
  
   
}

void loop() 
{
  
  WiFiClient client = server.available();
  if (!client) {
    return;
  }

  Serial.println("client connected");

  // Read the first line of the request
      String req = client.readStringUntil('\r');
      Serial.println("REQ = "+req);
      client.flush();
     
    
      if(req.indexOf("/status")>=0)
      {
          
          String stat="";
          if(digitalRead(A0) == HIGH)
            stat = stat + "1";
          else
            stat = stat + "0";
          if(digitalRead(A1) == HIGH)
            stat = stat + "1";
          else
            stat = stat + "0";
          if(digitalRead(A2) == HIGH)
            stat = stat + "1";
          else
            stat = stat + "0";
          if(digitalRead(A3) == HIGH)
            stat = stat + "1";
          else
            stat = stat + "0";
          if(digitalRead(A4) == HIGH)
            stat = stat + "1";
          else
            stat  = stat + "0";
          if(digitalRead(A5) == HIGH)
            stat = stat + "1";
          else
            stat  = stat + "0";
          if(digitalRead(A6) == HIGH)
            stat = stat + "1";
          else
            stat  = stat + "0";
          if(digitalRead(A7) == HIGH)
            stat = stat + "1";
          else
            stat  = stat + "0";
            
          String s = "HTTP/1.1 200 OK\r\n";
          s += "Content-Type: text/html\r\n\r\n";
          s +=stat;
          client.print(s);
          stat="";
      }

      if(req.indexOf("sw1/1")>=0)
        digitalWrite(A0,HIGH);
      else if(req.indexOf("sw1/0")>=0)
        digitalWrite(A0,LOW);
      else if(req.indexOf("sw2/1")>=0)
        digitalWrite(A1,HIGH);
      else if(req.indexOf("sw2/0")>=0)
        digitalWrite(A1,LOW);
      else if(req.indexOf("sw3/1")>=0)
        digitalWrite(A2,HIGH);
      else if(req.indexOf("sw3/0")>=0)
        digitalWrite(A2,LOW);
      else if(req.indexOf("sw4/1")>=0)
        digitalWrite(A3,HIGH);
      else if(req.indexOf("sw4/0")>=0)
        digitalWrite(A3,LOW);
      else if(req.indexOf("sw5/1")>=0)
        digitalWrite(A4,HIGH);
      else if(req.indexOf("sw5/0")>=0)
        digitalWrite(A4,LOW);
      else if(req.indexOf("sw6/1")>=0)
        digitalWrite(A5,HIGH);
      else if(req.indexOf("sw6/0")>=0)
        digitalWrite(A5,LOW);
      else if(req.indexOf("sw7/1")>=0)
        digitalWrite(A6,HIGH);
      else if(req.indexOf("sw7/0")>=0)
        digitalWrite(A6,LOW);
      else if(req.indexOf("sw8/1")>=0)
        digitalWrite(A7,HIGH);
      else if(req.indexOf("sw8/0")>=0)
        digitalWrite(A7,LOW);
delay(200);
}



void setupWiFi()
{

  WiFi.mode(WIFI_AP);
  

  WiFi.softAP("NODEMCU", WiFiAPPSK);
  IPAddress myIP = WiFi.softAPIP(); //Get IP address
  Serial.print("HotSpt IP:");
  Serial.println(myIP);
  Serial.println("here");
}


