#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-26

@author: jiugao
'''
import threading
import time

class ThreadQueue(object):
    def __init__(self, max):
        self.lock = threading.RLock()
        self.noEmpty = threading.Condition(self.lock)
        self.noFull = threading.Condition(self.lock)
        self.len = 0
        self.close=False
        self.max = max
        self.buf = []
        
    def get(self):
        self.lock.acquire()
        try:
            if self.len == 0:
                self.noEmpty.wait()
            if self.close is True:
                return None
            pop = self.buf.pop(0)
            self.len -= 1
            self.noFull.notifyAll()
            return pop
        finally:
            self.lock.release()
    
    def put(self, o):
        self.lock.acquire()
        try:
            if self.len == self.max:
                self.noFull.wait()
            if self.close is True:
                return
            self.buf.append(o)
            self.len += 1
            self.noEmpty.notifyAll()

        finally:
            self.lock.release()
    
    def interupt(self):
        self.lock.acquire()
        try:
            self.noFull.notifyAll()
            self.noEmpty.notifyAll()
            self.close=True
        finally:
            self.lock.release()

class Putter(threading.Thread):
    def __init__(self, q):
        self.q = q
        threading.Thread.__init__(self)
     
    def run(self):
        times = range(10)
        for i in times:
            print "put " + str(i)
            self.q.put(i)
            print "put " + str(i) + " end"
            time.sleep(0.01)

class Getter(threading.Thread):
    def __init__(self, q):
        self.q = q
        threading.Thread.__init__(self)
     
    def run(self):
        times = range(10)
        for i in times:
            print str(self.q.get())
            time.sleep(0.1)
            

if __name__ == '__main__':
    q=ThreadQueue(2)
    t1 = Getter(q)
    t2=Putter(q)
    t1.start()
    t2.start()
