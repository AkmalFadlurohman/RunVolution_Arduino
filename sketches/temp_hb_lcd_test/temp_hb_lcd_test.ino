#include <OneWire.h>
#include <DallasTemperature.h>
#include <Event.h>
#include <Timer.h>

// SEVENSEGMENT Variables
Timer t; // craete a timer object
Timer t2;
long number = 0; //declear the variables
int first_digit = 0;
int second_digit = 0;
int third_digit = 0;
int fourth_digit = 0;
int timer_event = 0;
int CA_1 = 12;
int CA_2 = 11;
int CA_3 = 10;
int CA_4 = 9;
int clk = 6;
int latch = 5;
int data = 4;
int count = 0;
int digits[4] ;
int CAS[4] = {12, 11, 10, 9};
byte numbers[10] {B11111100, B01100000, B11011010, B11110010, B01100110, B10110110, B10111110, B11100000, B11111110, B11110110};

void sevSegSetup();
void sevSegLoop();
void break_number(float num);
void display_number();
void cathode_high();

// TEMPERATURE Variables
#define TEMP_PIN 2
OneWire tempSensor(TEMP_PIN);
DallasTemperature sensors(&tempSensor);
float tempC;

void tempSetup();

// HEARTBEAT Variables
#define HBDEBUG(i)
int ledPin = 13;
int analogPin = 0;
int beatMsec = 0;
int heartRateBPM = 0;
const int delayMsec = 60; // 100msec per sample

bool heartbeatDetected(int IRSensorPin, int delay);
void heartbeatSetup();
void heartbeatLoop();

void setup() {
  Serial.begin(9600);

  // SEVENSEGMENT Setup
  sevSegSetup();
  t.every(1, display_number);
  
  // TEMPERATURE Setup
  tempSetup();
  t.every(5000, tempLoop);

  // HEARTBEAT Setup
  heartbeatSetup();
  t.every(delayMsec, heartbeatLoop);
  
}

void loop() {
  t.update();
}

// SEVENSEGMENT Methods
void sevSegSetup() {
  pinMode(CA_1, OUTPUT);
  pinMode(CA_2, OUTPUT);
  pinMode(CA_3, OUTPUT);
  pinMode(CA_4, OUTPUT);
  pinMode(clk, OUTPUT);
  pinMode(latch, OUTPUT);
  pinMode(data, OUTPUT);
  cathode_high();
}

void sevSegLoop() {
  t.update(); //timer update
  if (Serial.available()) { // read from serial
    t.stop(timer_event); //stop timer if anythign to read
    cathode_high(); // blank the screen
    String s = Serial.readString(); //read the serail value
    number = (long)s.toInt(); //convert it to int
    if (number > 9999) { //check the number is 0-9999
      Serial.println("Please Enter Number Between 0 - 9999");
    } else {
      break_number(number);
      timer_event = t.every(1, display_number); // start timer again
    }

  }
}

void break_number(float num) { // seperate the input number into 4 single digits
  if (num > 99.99) {
    num = 99.99;
  } else {
    num = (int) (num * 100);
  }
  
  first_digit = num / 1000;
  digits[0] = first_digit;

  int first_left = num - (first_digit * 1000);
  second_digit = first_left / 100;
  digits[1] = second_digit;
  int second_left = first_left - (second_digit * 100);
  third_digit = second_left / 10;
  digits[2] = third_digit;
  fourth_digit = second_left - (third_digit * 10);
  digits[3] = fourth_digit;

}

void display_number() { //scanning

  cathode_high(); //black screen
  digitalWrite(latch, LOW); //put the shift register to read
  if (count == 1) {
      shiftOut(data, clk, LSBFIRST, numbers[digits[count]]+B1); //send the data
  } else {
      shiftOut(data, clk, LSBFIRST, numbers[digits[count]]); //send the data
  }
  digitalWrite(CAS[count], LOW); //turn on the relevent digit
  digitalWrite(latch, HIGH); //put the shift register to write mode


  count++; //count up the digit
  if (count == 4) { // keep the count between 0-3
    count = 0;
  }


}

void cathode_high() { //turn off all 4 digits

  digitalWrite(CA_1, HIGH);
  digitalWrite(CA_2, HIGH);
  digitalWrite(CA_3, HIGH);
  digitalWrite(CA_4, HIGH);
}

// TEMPERATURE Methods
void tempSetup() {
  sensors.setResolution(12);
  sensors.begin();
}

void tempLoop() {
  cathode_high();
  sensors.requestTemperatures();
  tempC = sensors.getTempCByIndex(0);
  break_number(tempC);
  Serial.println(tempC);
}


// HEARTBEAT Methods
void heartbeatSetup() {
  pinMode(ledPin, OUTPUT);
}

void heartbeatLoop() {
  if (heartbeatDetected(analogPin, delayMsec)) {
    heartRateBPM = 60000 / beatMsec;
    digitalWrite(ledPin, 1);

    // Print msec/beat and instantaneous heart rate in BPM
    Serial.print(beatMsec);
    Serial.print(", ");
    Serial.println(heartRateBPM);

    beatMsec = 0;
  } else {
    digitalWrite(ledPin, 0);
  }

  beatMsec += delayMsec;
}

bool heartbeatDetected(int IRSensorPin, int delay) {
  static int maxValue = 0;
  static bool isPeak = false;
  int rawValue;
  bool result = false;

  rawValue = analogRead(IRSensorPin);
  // Separated because analogRead() may not return an int
  rawValue *= (1000 / delay);
  HBDEBUG(Serial.print(isPeak); Serial.print("p, "));
  HBDEBUG(Serial.print(rawValue); Serial.print("r, "));
  HBDEBUG(Serial.print(maxValue); Serial.print("m, "));

  // If sensor shifts, then max is out of whack.
  // Just reset max to a new baseline.
  if (rawValue * 4L < maxValue) {
    maxValue = rawValue * 0.8;
    HBDEBUG(Serial.print("RESET, "));
  }

  // Detect new peak
  if (rawValue > maxValue - (1000 / delay)) {
    // Only change peak if we find a higher one.
    if (rawValue > maxValue) {
      maxValue = rawValue;
    }
    // Only return true once per peak.
    if (isPeak == false) {
      result = true;
      HBDEBUG(Serial.print(result); Serial.print(",  *"));
    }
    isPeak = true;
  } else if (rawValue < maxValue - (3000 / delay)) {
    isPeak = false;
    // Decay max value to adjust to sensor shifting
    // Note that it may take a few seconds to re-detect
    // the signal when sensor is pushed on meatier part
    // of the finger. Another way would be to track how
    // long since last beat, and if over 1sec, reset
    // maxValue, or to use derivatives to remove DC bias.
    maxValue -= (1000 / delay);
  }
  HBDEBUG(Serial.print("\n"));
  return result;
}
