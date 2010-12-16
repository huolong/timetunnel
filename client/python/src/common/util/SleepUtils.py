#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-11-29

@author: jiugao
'''
from time import sleep
import random

def sleepRandom(max):
    i = random.randint(0, max)
    sleep(i)
