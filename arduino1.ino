#include <Servo.h>
#include <SoftwareSerial.h>



int TX = 13;
int RX = 12;
Servo myServo,myServo1;
SoftwareSerial mySerial(TX, RX);

String value,value1;
int angle,angle1;
int prevValue = 0;
int prevValue1 = 0;
int flag = 0;
char command,command1;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(57600);
  Serial.println("Set up complete");
  mySerial.begin(9600);
  myServo.attach(7);
  myServo1.attach(8);
  //myServo.write(90);
  //myServo.write(90);
//
}

void loop() {
  // put your main code here, to run repeatedly:
  // if there's a new command reset the string
  if (mySerial.available()) {
   mySerial.flush();
    value = "";
    value1 = "";
  }

  // Construct the command string fetching the bytes, sent by Android, one by one.
  while (mySerial.available()) {
    command = ((byte)mySerial.read());
   
if (command == ':') {
      flag = 2;
      break;
    }
    else if(command == ';')
    {
      flag = 3;
      break;
    }
    else {
      value += command;
      value1+=command;
    }
    delay(1);
  }

////////////////////////////////////////////
 

////////////////////////////////////////////////////////////
   if(flag ==2)
   { 
    value1 = "";
  int casted = value.toInt();
  if (prevValue != casted) {
    Serial.println(casted);
    angle = map(casted, -20, 20, 25, 90); // Second and Third argument is used to control the sensitivty of servoMotor reaction to Accelerometer angle.
    myServo.write(angle);
    prevValue = casted;
    flag =0;
  } else {
    prevValue = casted;
  }
   }

   else
   {
    ////
   }
///////////////////////////////////////////////////////////////

if(flag ==3)
{
  value = "";
int casted1 = value1.toInt();
  if (prevValue1 != casted1) {
    Serial.println(casted1);
    angle1 = map(casted1, -20, 20, 25, 90); // Second and Third argument is used to control the sensitivty of servoMotor reaction to Accelerometer angle.
    myServo1.write(angle1);
    prevValue1 = casted1;
    flag =0;
  } else {
    prevValue1 = casted1;
  }
}
else
{
  //////
}
  
  
}
