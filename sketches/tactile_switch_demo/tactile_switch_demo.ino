// constants won't change. They're used here to set pin numbers:
#define buttonPin A1
#define ledPin 13
#define OFF 0
#define ON 1

// variables will change:
int buttonState = 0;         // variable for reading the pushbutton status
int ledState = ON;
int ledBefore = OFF;

void setup() {
  // initialize the LED pin as an output:
  pinMode(ledPin, OUTPUT);
  // initialize the pushbutton pin as an input:
  pinMode(buttonPin, INPUT);
}

void loop() {
  // read the state of the pushbutton value:
  buttonState = digitalRead(buttonPin);

  if (buttonState == HIGH) {
    ledBefore = ledState;
  } else {
    ledState = !ledBefore;
  }

  if (ledState == ON) {
    digitalWrite(ledPin, HIGH);
  } else {
    digitalWrite(ledPin, LOW);
  }


}
