#include <SoftwareSerial.h>
#include <Servo.h>

Servo servo1;
Servo servo2;
int servo1angle = 90;
int servo2angle = 90;

int TX = 10;
int RX = 11;

SoftwareSerial HC_06(TX, RX);  //Bluetooth TX to 10 and Bluetooth RX to 11..

void setup()
{
  servo1.attach(7);
  servo2.attach(8);
  Serial.begin(9600);
  HC_06.begin(9600);
}
//
void loop()
{
  while (HC_06.available() > 0 )
  {
    int value = HC_06.read();
    Serial.println(value);
    if (value == 49 )
    {
      HC_06.write(servo1angle + 5);

      if (servo1angle < 110)
      {
        servo1angle += 5;
        Serial.println(servo1angle);
        servo1.write(servo1angle);
        //HC_06.write(output);
      }
      else
      {
        //else is necessary //
      }
    }
    else if (value == 50) {
      Serial.println(value);

      HC_06.write(servo1angle - 5);
      if (servo1angle > 60)
      {

        servo1angle -= 5;
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


      HC_06.write(servo2angle + 5);


      if (servo2angle < 110)
      {
        servo2angle += 5;
        Serial.println(servo2angle);
        servo2.write(servo2angle);

      }
      else
      {
        //else is necessary //
      }

    }

    else if (value == 52 ) {
      Serial.println(value);

      HC_06.write(servo2angle - 5);
      if (servo2angle > 60)
      {
        servo2angle -= 5;
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
