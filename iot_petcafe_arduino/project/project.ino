#include <AWS_IOT.h>
#include <WiFi.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <ESP32_Servo.h>
#define SEALEVELPRESSURE_HPA (1013.25)



//사물 - 짱이 사이 거리 초음파
const int trigPin = 22; //초음파 센서
const int echoPin = 23; //초음파 센서

//물 거리 초음파
const int trigPin_w = 17; //초음파 센서
const int echoPin_w = 16; //초음파 센서

//사료 거리 초음파
const int trigPin_f = 19; //초음파 센서
const int echoPin_f = 21; //초음파 센서

//조도센서 및 led
const int cdsPin = 35;
int cValue=0;
//본체led
const int led_main = 5;

//초음파 관련 변수
int disi = 0; //초음파 센서 거리 
long duration, distance;
//물통 초음파
long duration_w, distance_w;
//사로툥 초음파
long duration_f, distance_f;
bool boolWater, boolFeed = 0; //1이면 공급

//서보모터 + 워터펌프
Servo servomotor;
static const int pinPump= 15;  
static const int servoPin = 4;
int posDegrees=0;

//----------------------------------------------------------------------------------------

//AWS connection and WIFI connection
AWS_IOT AWS_CLIENT;
//WIFI 연결
//const char* ssid = "House";
//const char* password = "tkanfkdl98";
const char* ssid = "Sk's iptime";
const char* password = "tmdrl1761!";


//사물연결
char HOST_ADDRESS[]="a1g1uldeb5uxbm-ats.iot.ap-northeast-2.amazonaws.com";
char CLIENT_ID[]= "Provider";

char* aws_topic = "$aws/things/Provider/shadow/update";
//주제 구독
char *sTOPIC_NAME[2] = {
  "$aws/things/Provider/shadow/update/accepted",
  "$aws/things/Provider/shadow/get/accepted"
  };

//주제 게시
char pTOPIC_NAME[] = "$aws/things/Provider/shadow/update";

int status = WL_IDLE_STATUS;
int msgCount = 0, msgReceived = 0;
char payload[512];
char rcvdPayload[512];

void mySubCallBackHandler (char *topicName, int payloadLen, char *payLoad)
{
  strncpy(rcvdPayload, payLoad, payloadLen);
  rcvdPayload[payloadLen] = 0;
  msgReceived = 1;
}

void subscribe(){
  for (int i=0; i<2; i++){
      if (0 == AWS_CLIENT.subscribe(sTOPIC_NAME[i], mySubCallBackHandler)) {
      Serial.println("Subscribe Successfull");
      }
      else {
        Serial.println("Subscribe Failed");
        while(1);
      }
    }
}

int motor_state=1;
int pump_state=1;

void water_motor_on(void){
    Serial.println("feed Motor on");
    for(posDegrees = 5; posDegrees <= 80; posDegrees++) {
        servomotor.write(posDegrees); // 모터의 각도를 설정합니다.
        delay(20);
    }
    delay(500);
    for(posDegrees = 0; posDegrees <= 90; posDegrees++) {
        servomotor.write(5); // 모터의 각도를 설정합니다.
        delay(20);
    }
    delay(2000);
}
void pump_on(void){
  Serial.println("pump on");
  digitalWrite(pinPump, HIGH);    
  delay(2500);  
  digitalWrite(pinPump, LOW);
  delay(2000);  

}

void updateMotorState(int desired_motor_state){
  motor_state = desired_motor_state;

  water_motor_on();

  sprintf(payload, "{\"state\":{\"reported\":{\"motor\":%d}}}", motor_state);
  AWS_CLIENT.publish(aws_topic, payload);
}
void updatePumpState(int desired_pump_state){
  pump_state = desired_pump_state;

  pump_on();

  sprintf(payload, "{\"state\":{\"reported\":{\"pump\":%d}}}", pump_state);
  AWS_CLIENT.publish(aws_topic, payload);
}

void messageArrived(){
  char *pch;
  int desired_motor_state;
  int desired_pump_state; 
  

  //모더 제어 parsing
  pch = strstr(rcvdPayload, "\"desired\":{\"motor\":");
  if(pch != NULL){
    pch += strlen("\"desired\":{\"motor\":");
    desired_motor_state = *pch -'0';
    updateMotorState(desired_motor_state);
  }else{;}

  //펌프 제어 paring
  pch = strstr(rcvdPayload, "\"desired\":{\"pump\":");
  if(pch != NULL){
    pch += strlen("\"desired\":{\"pump\":");
    desired_pump_state = *pch -'0';
    updatePumpState(desired_pump_state);
  }else{;}
  
  //안되면 이거 지워보기
  delete rcvdPayload;
}

void setup()
{
  bool status;
  Serial.begin(115200);
  Serial.print("WIFI status = ");
  Serial.println(WiFi.getMode());
  WiFi.disconnect(true);
  delay(1000);
  WiFi.mode(WIFI_STA);
  delay(1000);
  Serial.print("WIFI status = ");
  Serial.println(WiFi.getMode());
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi..");
  }
  Serial.println("Connected to wifi");

  //aws 
  if (AWS_CLIENT.connect(HOST_ADDRESS, CLIENT_ID) == 0) {
    Serial.println("Connected to AWS");
    delay(1000);
    //shadow 주제 구독
    Serial.println("Setup에서 subscribe");
    subscribe();
    
  }
  else {
    Serial.println("AWS connection failed, Check the HOST Address");
    while (1);
  }

  //서보모터 + 워터펌프
  servomotor.attach(servoPin);
  pinMode(pinPump, OUTPUT);  
  digitalWrite(pinPump, LOW); 

  //초음파 센서
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(trigPin_w, OUTPUT);
  pinMode(echoPin_w, INPUT);
  pinMode(trigPin_f, OUTPUT);
  pinMode(echoPin_f, INPUT);

  //led
  pinMode(led_main, OUTPUT);

  Serial.println("motor reset");
    for(posDegrees = 5; posDegrees <= 80; posDegrees++) {
        servomotor.write(0); // 모터의 각도를 설정합니다.
        delay(20);
    }
}

void loop()
{
  //연결 성공
  if (msgReceived == 1)
  {
    msgReceived = 0;
    Serial.print("Received Message:");
    Serial.println(rcvdPayload);
    messageArrived();
  }else{
    //연결이 안 되있으면 재구독
    //Serial.println("재구독");
    //subscribe();
  }

  
  //본체 - 애완동물 초음파 
  digitalWrite(trigPin, LOW); // trig low for 2us
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH); // trig high for 10us
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  distance = duration * 17 / 1000;

  Serial.print("본체 거리 : ");
  Serial.println(distance);

  if(distance <= 25){
      disi = disi +1;
    }else if(distance>25){
      disi=0;
    }
    Serial.print("disi : ");
    Serial.println(disi);

  //본체 - 물통 초음파 
  digitalWrite(trigPin_w, LOW); // trig low for 2us
  delayMicroseconds(2);
  digitalWrite(trigPin_w, HIGH); // trig high for 10us
  delayMicroseconds(10);
  digitalWrite(trigPin_w, LOW);
  duration_w = pulseIn(echoPin_w, HIGH);
  distance_w = duration_w * 17 / 1000;
  Serial.print("물통 거리 : ");
  Serial.println(distance_w);
  
  if(distance_w <= 21){ //특정 거리 이하면 공급 중단
    boolWater = 0;
  }else if(distance_w >21){
    boolWater = 1;  
  }

  //본체 - 사료통 초음파 
  digitalWrite(trigPin_f, LOW); // trig low for 2us
  delayMicroseconds(2);
  digitalWrite(trigPin_f, HIGH); // trig high for 10us
  delayMicroseconds(10);
  digitalWrite(trigPin_f, LOW);
  duration_f = pulseIn(echoPin_f, HIGH);
  distance_f = duration_f * 17 / 1000;
  Serial.print("사료통 거리 : ");
  Serial.println(distance_f);
  
  if(distance_f <= 17){ //특정 거리 이하면 공급 중단
    boolFeed = 0;
  }else if(distance_f > 17){
    boolFeed = 1;  
  }

  // 뮬 or 사료 공급
  if(disi>=5 && boolWater ==1 && boolFeed==1){ //물과 사료를 줌
    pump_on();
    water_motor_on();
    disi =0;
    boolFeed=0;
    boolWater=0;
  }else if(disi>=5 && boolWater == 1 && boolFeed==0){//물만 줌
    pump_on();
    disi =0;
    boolWater=0;
  }else if(disi>=5 && boolWater == 0 && boolFeed==1){//사료만 줌
    water_motor_on();
    disi =0;
    boolFeed=0;
  }

  //조도 센서 측정
  
  cValue = analogRead(cdsPin);
  Serial.print("Cds Value = ");
  Serial.println(cValue);
  Serial.println();
  if(cValue<=1000){
    digitalWrite(led_main,HIGH);  
  }else{
    digitalWrite(led_main,LOW);    
  }
  
  delay(1000);
}
