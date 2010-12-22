#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-25

@author: jiugao
'''
from client.TimeTunnel2 import use, passport, tunnel, post, release
import logging.config
import os
import threading
import time


basepath = os.path.dirname(__file__)
logging.config.fileConfig(basepath + "/../conf/log.conf")

class PublishThread(threading.Thread):
    def __init__(self, t):
        self.id = id
        self.m = "aadfaddaffdf"
        self.t = t
        threading.Thread.__init__(self)
     
    def run(self):
        times = range(10)
        for i in times:
            time.sleep(1)
            post(self.t, self.m + str(i))


if __name__ == '__main__':
    use(passport("jiugao", "1111"))
    t = tunnel("t1")
    threads = []
    threadnum = range(10)
    for i in threadnum:
        pt = PublishThread(t)
        pt.start()
        threads.append(pt)
        
    [t.join() for t in threads] 
    release()
