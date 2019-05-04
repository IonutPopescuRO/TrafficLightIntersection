import os
import time
import json
import threading
import RPi.GPIO as GPIO
from bluetooth import *
GPIO.setmode(GPIO.BCM)# Folosim GPIO in mod virtual, nu fizic

gpios = [[17, 27, 22], [5, 6, 13], [26, 18, 23], [24, 25, 16]]# codul GPIO al celor 4 semafoare (0=>verde, 1=>galben, 2=>rosu)

yellow = False# boolean ce-l vom folosi la lumina intermitenta
lights = False# boolean ce-l vom folosi la semaforizare
connection = False# boolean ce-l vom folosi sa tinem evidenta unui dispozitiv conectat bluetooth
christmass = False# boolean ce-l vom folosi la activarea modului christmass
light_status = [0, 0, 0, 0]# array unde vom tine evidenta ledurilor active
status = [True, True, True, True]# array in care verificam daca un semafor este functional
timer = [6, 2]# array cu timpul (0=>verde/rosu, 1=>galben)
client_sock = None# variabila folosita la transmiterea datelor catre clientul conectat prin bluetooth

for row in gpios:# Initiam toati GPIO si ii si oprim in acelasi timp
	for elem in row:
		GPIO.setup(elem, GPIO.OUT)
		GPIO.output(elem, GPIO.LOW)
		
GPIO.output(gpios[3][2], GPIO.HIGH)# aprindem un led rosu, va sta activ pana se conecteaza cineva prin bluetooth

def intermittent():# modul intermitent va aprinde si stinge, constant, culoarea galben, pentru semafoarele nefunctionale
	while(not status[0] or not status[1] or not status[2] or not status[3]):
		if(not christmass):
			for i in range(0, 4):
				if(not status[i]):
					GPIO.output(gpios[i][1], GPIO.HIGH)
			time.sleep(1)
			for i in range(0, 4):
				if(not status[i]):
					GPIO.output(gpios[i][1], GPIO.LOW)
			time.sleep(1)

def gpio_update():# aceasta functie opreste toate led-urile de pe semafoarele functionale, iar apoi aprinde ledurile active din light_status
	if(not christmass):
		for i in range(0, 4):
			for j in range(0, 3):
				if(status[i]):
					GPIO.output(gpios[i][j], GPIO.LOW)

		for i in range(0, 4):
			if(status[i] and light_status[i]):
				GPIO.output(gpios[i][light_status[i]-1], GPIO.HIGH)

def christmass_mode():# un mod de amuzament, va aprinde si stinge pe rand toate led-urile =)
	christmass_timer = 0.05
	while christmass:
		last = gpios[0][0]
		for row in gpios:
			for elem in row:
				GPIO.output(last, GPIO.LOW)
				time.sleep(christmass_timer)
				GPIO.output(elem, GPIO.HIGH)
				time.sleep(christmass_timer)
				last=elem
			GPIO.output(last, GPIO.LOW)

def traffic_light():# functia care se ocupa de semaforizare, atata timp cat nu se da comanda stop a clientului
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

def Convert(string):# functie ce trece comanda clientului, cu spatii, intr-un array
    li = list(string.split(" ")) 
    return li 

def stopAll():# functie ce opreste toate led-urile
	for row in gpios:
		for elem in row:
			GPIO.output(elem, GPIO.LOW)

server_sock=BluetoothSocket( RFCOMM )# initiere socket-ului bluetooth
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"# un id unic pe care il va folosi socketul bluetooth si clientul pentru recunoastere

advertise_service( server_sock, "Semafoare",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ] )# initierea serviciului bluetooth
try:
	while True:# acest loop va rula incontinuu permitant primirea comenzilor de la client prin bluetooth
		if(connection == False):
			client_sock, client_info = server_sock.accept()
			connection = True# cand se conecteaza un client schimbam statusul connection
			if(not lights):# si aprindem un led verde
				GPIO.output(gpios[3][2], GPIO.LOW)
				GPIO.output(gpios[3][0], GPIO.HIGH)
		try:
			data = client_sock.recv(1024)# aici se ajunge daca se primesc comenzi de la client
			data = Convert(data)# convertim comenzile in array
			if (data[0] == "disconnect"):# pe prima linie mereu va fi comanda, aceasta comanda este apelata la deconectarea clientului
				if(not lights):
					GPIO.output(gpios[3][2], GPIO.HIGH)
					GPIO.output(gpios[3][0], GPIO.LOW)
				client_sock.close()
				connection = False
			elif (data[0] == "start"):# pornirea semafoarelor din intersectie
				if(not lights):
					lights=True
					p1 = threading.Thread(target=traffic_light)# creeam un Thread pentru a rula in background
					p1.start()# pornim Thread-ul
			elif (data[0] == "update"):# comanda transmisa pentru actualizarea timpilor
				timer = [int(data[1]), int(data[2])]
			elif (data[0] == "stop"):# comanda pentru oprirea semafoarelor
				lights=False# lights devine False pentru a termina Thread-ul initiat la start
				status = [True, True, True, True]
				stopAll()
			elif (data[0] == "switch"):# comanda ce opreste sau porneste un semafor
				if(data[2]=='1'):
					status[int(data[1])] = True# activam semaforul
				else:
					yellow = True
					for elem in status:
						if(not elem):# verificam daca mai exista semafoare oprite
							yellow = False
					status[int(data[1])] = False
					for elem in gpios[int(data[1])]:# oprim led-urile semaforului
						GPIO.output(elem, GPIO.LOW)
					if(yellow):# daca nu mai este niciun semafor oprit, creeam un Thread
						p2 = threading.Thread(target=intermittent)
						p2.start()
			elif (data[0] == "christmass"):# activarea sau dezactivarea modului christmass
				if(not christmass):# daca nu este activ il activam si creem un Thread
					christmass=True
					stopAll()
					p3 = threading.Thread(target=christmass_mode)
					p3.start()
				else:# daca este activ, il oprim
					christmass=False
					stopAll()
			elif (data[0] == "halt"):# cumanda pentru oprirea raspberry pi
				GPIO.output(gpios[3][0], GPIO.LOW)
				for i in range(0, 4):
					GPIO.output(gpios[i][2], GPIO.HIGH)
				os.system("sudo shutdown -h now")
		except IOError as e:# in caz de erori oprim conexiunea cu clientul
			client_sock.close()
			connection = False
			pass
except KeyboardInterrupt:# la inchiderea programului de la tastatura dam cleanup la GPIO
	GPIO.cleanup()
	client_sock.close()#inchidem conexiunea cu clienul
	server_sock.close()#inchidem serverul bluetooth