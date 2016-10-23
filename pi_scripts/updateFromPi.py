#!/usr/bin/python3

import sys
import glob
import re
from io import BytesIO
import json
import pprint
import requests
import time
import subprocess

#client id and client secret
client = '<put client id here>'
access_token='<put access token here>'

base_dir = '/sys/bus/w1/devices/'
device_folder = glob.glob(base_dir + '28*')[0]
device_file = device_folder + '/w1_slave'

niters = 60
interval = 15
 
def read_temp_raw_old():
    f = open(device_file, 'r')
    lines = f.readlines()
    f.close()
    return lines

def read_temp_raw():
	catdata = subprocess.Popen(['cat',device_file], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	out,err = catdata.communicate()
	out_decode = out.decode('utf-8')
	lines = out_decode.split('\n')
	return lines
 
def read_temp():
    lines = read_temp_raw()
    while lines[0].strip()[-3:] != 'YES':
        time.sleep(0.2)
        lines = read_temp_raw()
    equals_pos = lines[1].find('t=')
    if equals_pos != -1:
        temp_string = lines[1][equals_pos+2:]
        temp_c = float(temp_string) / 1000.0
        temp_f = temp_c * 9.0 / 5.0 + 32.0
        return temp_c, temp_f

def main():
   old_temp = -999999999.99
   endpoints_url = "https://graph.api.smartthings.com/api/smartapps/endpoints/%s?access_token=%s" % (client, access_token)
   r = requests.get(endpoints_url)
   if (r.status_code != 200):
      print("Error: " + r.status_code)
   else:
      theendpoints = json.loads( r.text )
      for counter in range(niters, 0, -1):
         (temp_c, temp_f) = read_temp()
         for endp in theendpoints:
            uri = endp['uri']
            temp_url = uri + ("/update/%.2f/F" % temp_f)
            headers = { 'Authorization' : 'Bearer ' + access_token }
            if ( round(temp_f, 2) != round(old_temp, 2) ):
               r = requests.put(temp_url, headers=headers)
         old_temp = temp_f
         time.sleep(interval)

main()

