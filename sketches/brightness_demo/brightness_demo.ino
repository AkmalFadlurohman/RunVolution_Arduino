#define potPin A2
#define ledPin 3

int val, x;

void setup() {
  // put your setup code here, to run once:
  pinMode(ledPin, OUTPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  val = analogRead(potPin);
  x = val;
  x = map(x, 0, 1023, 0, 255);
  analogWrite(ledPin, x);
}
