import os
import time
import json
import threading
import RPi.GPIO as GPIO
from bluetooth import *
GPIO.setmode(GPIO.BCM)#GPIO Virtual
# https://github.com/pybluez/pybluez/tree/master/examples/simple
gpios = [[17, 27, 22], [5, 6, 13], [26, 18, 23], [24, 25, 16]]

yellow = False
lights = False
light_status = [0, 0, 0, 0]
status = [True, True, True, True]
timer = [6, 2]
connection = False
christmas = False
client_sock = None

for row in gpios:
	for elem in row:
		GPIO.setup(elem, GPIO.OUT)
		GPIO.output(elem, GPIO.LOW)
		
GPIO.output(gpios[3][2], GPIO.HIGH)

wait = 0.001
christmas_timer = 0.05

def intermittent():
	while(not status[0] or not status[1] or not status[2] or not status[3]):
		if(not christmas):
			for i in range(0, 4):
				if(not status[i]):
					GPIO.output(gpios[i][1], GPIO.HIGH)
			time.sleep(1)
			for i in range(0, 4):
				if(not status[i]):
					GPIO.output(gpios[i][1], GPIO.LOW)
			time.sleep(1)

def gpio_update():
	if(not christmas):
		for i in range(0, 4):
			for j in range(0, 3):
				if(status[i]):
					GPIO.output(gpios[i][j], GPIO.LOW)
		
		for i in range(0, 4):
			if(status[i] and light_status[i]):
				GPIO.output(gpios[i][light_status[i]-1], GPIO.HIGH)

def christmas_mode():
	while christmas:
		last = gpios[0][0]
		for row in gpios:
			for elem in row:
				GPIO.output(last, GPIO.LOW)
				time.sleep(christmas_timer)
				GPIO.output(elem, GPIO.HIGH)
				time.sleep(christmas_timer)
				last=elem
			GPIO.output(last, GPIO.LOW)
			if(not christmas):
				break;
				
def christmas_mode2():
	while christmas:
		for i in range(0, 4):
			GPIO.output(gpios[i][0], GPIO.HIGH)
			GPIO.output(gpios[i][1], GPIO.LOW)
			GPIO.output(gpios[i][2], GPIO.HIGH)
		if(not christmas):
			break;
		time.sleep(christmas_timer)
		for i in range(0, 4):
			GPIO.output(gpios[i][0], GPIO.LOW)
			GPIO.output(gpios[i][1], GPIO.HIGH)
			GPIO.output(gpios[i][2], GPIO.LOW)
		if(not christmas):
			break;
		time.sleep(christmas_timer)

def traffic_light():
    global light_status
    while lights:
		light_status=[1, 3, 1, 3]
		gpio_update()
		if(connection):	client_sock.send("%s" % json.dumps(light_status))
		time.sleep(timer[0])
		if(not lights): break
		light_status=[2, 3, 2, 3]
		gpio_update()
		if(connection):	client_sock.send("%s" % json.dumps(light_status))
		time.sleep(timer[1])
		if(not lights): break
		light_status=[3, 1, 3, 1]
		gpio_update()
		if(connection):	client_sock.send("%s" % json.dumps(light_status))
		time.sleep(timer[0])
		if(not lights): break
		light_status=[3, 2, 3, 2]
		gpio_update()
		if(connection):	client_sock.send("%s" % json.dumps(light_status))
		time.sleep(timer[1])

def Convert(string): 
    li = list(string.split(" ")) 
    return li 

def stopAll():
	for row in gpios:
		for elem in row:
			GPIO.output(elem, GPIO.LOW)

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "Semafoare",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ] )
try:
	while True:
		if(connection == False):
			print("Waiting for connection on RFCOMM channel %d" % port)
			client_sock, client_info = server_sock.accept()
			connection = True
			print("Accepted connection from ", client_info)
			if(not lights):
				GPIO.output(gpios[3][2], GPIO.LOW)
				GPIO.output(gpios[3][0], GPIO.HIGH)
			if(connection):
				current_status = ["resume", christmas, light_status, status, timer, lights]
				client_sock.send("%s" % json.dumps(current_status))
		try:
			data = client_sock.recv(1024)
			data = Convert(data)
			print(data)
			if (data[0] == "disconnect"):
				print("Client wanted to disconnect")
				if(not lights):
					GPIO.output(gpios[3][2], GPIO.HIGH)
					GPIO.output(gpios[3][0], GPIO.LOW)
				client_sock.close()
				connection = False
			elif (data[0] == "start"):
				if(christmas):
					christmas=False
				time.sleep(wait)
				if(not lights):
					lights=True
					p1 = threading.Thread(target=traffic_light)
					p1.start()
			elif (data[0] == "update"):
				timer = [int(data[1]), int(data[2])]
			elif (data[0] == "stop"):
				if(christmas):
					christmas=False
				lights=False
				status = [True, True, True, True]
				stopAll()
			elif (data[0] == "switch"):
				if(data[2]=='1'):
					status[int(data[1])] = True
				else:
					yellow = True
					for elem in status:
						if(not elem):
							yellow = False
					status[int(data[1])] = False
					for elem in gpios[int(data[1])]:
						GPIO.output(elem, GPIO.LOW)
					if(yellow):
						p2 = threading.Thread(target=intermittent)
						p2.start()
			elif (data[0] == "christmas"):
				if(not christmas):
					christmas=True
					stopAll()
					if(data[1]=="1"):
						p3 = threading.Thread(target=christmas_mode)
					else:
						p3 = threading.Thread(target=christmas_mode2)
					p3.start()
				else:
					christmas=False
					stopAll()
			elif (data[0] == "halt"):
				if(christmas):
					christmas=False
				stopAll()
				for i in range(0, 4):
					GPIO.output(gpios[i][2], GPIO.HIGH)
				os.system("sudo shutdown -h now")
			elif (data[0] == "reboot"):
				if(christmas):
					christmas=False
				stopAll()
				for i in range(0, 4):
					GPIO.output(gpios[i][1], GPIO.HIGH)
				os.system("sudo reboot now")
		except IOError as e:
			#print(e)
			client_sock.close()
			connection = False
			pass
		except BluetoothError:
			print("Something wrong with bluetooth")
except KeyboardInterrupt:
	GPIO.cleanup()
	print("\nDisconnected")
	client_sock.close()
	server_sock.close()