#include <SoftwareSerial.h>
#include <Servo.h>

Servo servo1;
Servo servo2;
int servo1angle = 90;
int servo2angle = 90;
int value;
int TX = 13;
int RX = 12;
// Center servos
int servo1Angle = 90;


SoftwareSerial HC_06(TX, RX);  //Bluetooth TX to 10 and Bluetooth RX to 11.

void setup()
{
  servo1.attach(6);
  servo2.attach(8);
  servo1.write(90);
  servo2.write(90);
  Serial.begin(9600);
  HC_06.begin(9600);

}

void loop()
{
  HC_06.flush();
  while (HC_06.available() > 0 )
  {
    value = HC_06.read();
    Serial.println(value);
    if (value == 50 )
    {
      Serial.println(value);
      if (servo1angle < 110)
      {
        servo1angle += 2;
        Serial.println(servo1angle);
        servo1.write(servo1angle);
      }
      else
      {
        //else is necessary //
      }
    }
    else if (value == 52) {
      Serial.println(value);

      if (servo1angle > 75)
      {

        servo1angle -= 2;
        Serial.println(servo1angle);
        servo1.write(servo1angle);
      }

      else
      {
        //else is necessary //
      }

    }

    else if (value == 51) {
      Serial.println(value);



      if (servo2angle < 105)
      {
        servo2angle += 2;
        Serial.println(servo2angle);
        servo2.write(servo2angle);

      }
      else
      {
        //else is necessary //
      }

    }

    else if (value == 49 ) {
      Serial.println(value);

      if (servo2angle > 70)
      {
        servo2angle -= 2;
        Serial.println(servo2angle);
        servo2.write(servo2angle);
      }

      else
      {
        //else is necessary
      }


    }
  }

}
