#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-22

@author: jiugao
'''
import logging
import traceback
from connection.Connection import Connection

logger = logging.getLogger("tt2-client.TunnelConnection")

class TCPool(object):
    def __init__(self, destination):
        self.tcs = {}
        self.destination = destination
        import threading
        self.lock = threading.RLock()
    
    def getTC(self, auth, t, conf):
        self.lock.acquire()
        try:
            if self.tcs.has_key(t):
                tc = self.tcs[t]
                return tc
            tc = Connection(auth, t, self.destination, conf)
            self.tcs[t] = tc
            return tc
        except:
            logger.error(str(traceback.format_exc()))
            return None
        finally:
            self.lock.release()
                
    def destoy(self):
        self.lock.acquire()
        try:
            for c in self.tcs.values():
                c.destory()
        finally:
            self.lock.release()


    
    
        
        
