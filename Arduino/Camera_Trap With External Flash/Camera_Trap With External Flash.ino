#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
 
AndroidAccessory acc("Manufacturer",
		     "Model",
		     "Description",
		     "1.0",
		     "http://yoursite.com",
		     "0000000012345678");

int pirPin = 2;
int pirVal = 0;
boolean isTriggered = false;

int btnPin = 12;
int btnVal = 0;
boolean triggerEnabled = false;

//addingExternal flash
int flashPin = 13; 

void setup()
{
  pinMode(pirPin, INPUT);
  pinMode(btnPin, INPUT);
  pinMode(flashPin,OUTPUT);
 
  Serial.begin(115200);
  //Serial.begin(9600);
  
  acc.powerOn();
}// setup

void loop()
{
  byte err;
  byte idle;
  static byte count = 0;
  byte androidMsg[3];
  long touchcount;
  
  if (acc.isConnected())
  {
    int androidMsg = acc.read();
        
    if (androidMsg == 1)
    {
      triggerEnabled = !triggerEnabled;  
    }
    
    btnVal = digitalRead(btnPin);
    if (btnVal == HIGH)
    {
      if (triggerEnabled == false)
      {
        triggerEnabled = true;
        Serial.println("Ready to trigger.");  
      }
    }// btnVal == HIGH
    
    if (triggerEnabled == true)
    {
      pirVal = digitalRead(pirPin);
      if (pirVal == HIGH)
      {
      //ExternalFlash
          digitalWrite(flashPin, HIGH);
        if (isTriggered == false)
        {
          Serial.println("Motion detected!");

          sendMessage(1);
          delay(10);
        }
        sendMessage(0);
        delay(10);
        isTriggered = true;
      }
      else
      {
        digitalWrite(flashPin, LOW);
        sendMessage(0);
        delay(10);
        isTriggered = false;
      }
    }
    else
    {
      sendMessage(0);
      delay(10);
    }// triggerEnabled
    
  }// acc.isConnected()    
    
}// loop

void sendMessage(int msgValue)
{
  byte msg[1];
  msg[0] = msgValue;
  acc.write(msg, 1);
}// sendMessage
