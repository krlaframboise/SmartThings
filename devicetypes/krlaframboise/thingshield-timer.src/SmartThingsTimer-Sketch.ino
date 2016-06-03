//*****************************************************************************
/// @file
/// @brief
///   Arduino SmartThings Shield LED Example 
//*****************************************************************************
#include <SoftwareSerial.h>   //TODO need to set due to some weird wire language linker, should we absorb this whole library into smartthings
#include <SmartThings.h>


#define PIN_THING_RX    3
#define PIN_THING_TX    2


SmartThingsCallout_t messageCallout;    // call out function forward decalaration
SmartThings smartthing(PIN_THING_RX, PIN_THING_TX, messageCallout);  // constructor

const int ledPin = 13;

bool isDebugEnabled = true;
bool ledOn = false;
int interval = 1000;
int ledOnTime = 300;
unsigned long previousMillis = 0;

void setup() {  
  pinMode(ledPin, OUTPUT); 
  if (isDebugEnabled) {
    Serial.begin(9600);
    Serial.println("setup..");
  }
}

void loop() {  
  smartthing.run();  
  
  unsigned long currentMillis = millis();  
  if ((unsigned long)(currentMillis - previousMillis) >= interval) {
    smartthing.send("tick");   
    previousMillis = millis();
  turnOnLED();
  }
  else if  (ledOn && (unsigned long)(currentMillis - previousMillis) >= ledOnTime) {
    turnOffLED();
  }
}

void turnOnLED() {
  ledOn = true;
  digitalWrite(ledPin, HIGH);  
  smartthing.shieldSetLED(0, 0, 1);  
}

void turnOffLED() {
  ledOn = false;
  digitalWrite(ledPin, LOW);
  smartthing.shieldSetLED(0, 0, 0);
}

void messageCallout(String message) {
  /*if (isDebugEnabled && message.equals("") == false) {
    Serial.print("Received message: '");
    Serial.print(message);
    Serial.println("' ");
  }*/
}
