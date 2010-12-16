#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-22

@author: jiugao
'''
from connection.PerSessionClient import PerSessionClient
import logging
import traceback
import time
from destination.DestQueryInfo import DestQueryInfo

logger = logging.getLogger("tt2-client.Connection")

class Connection(object):
    def __init__(self, auth, t, destination, conf):
        self.auth = auth
        self.conf=conf
        self.t = t
        self.destination = destination
        self.dest = None
        self.valid = False
        self.close = False
        self.client = None
        import threading
        self.lock = threading.RLock()
    
    def __getUrl(self):
        qi = DestQueryInfo(self.auth, self.t.name, self.t.sequence, self.t.timeout)
        while self.close is False:
            d = None
            try:
                d = self.destination.getDest(qi)
                return d
            except:
                logger.error(str(traceback.format_exc()))
                if self.dest is not None:
                    logger.error("due to can not get broker from router, return old invalid connection")
                    return self.dest
                else:
                    logger.error("due to can not get broker from router, loop here")
                    time.sleep(3)
                    continue
        pass
                
    def pub(self, m):
        self.lock.acquire();
        try:
            while self.close is False:
                if self.valid is True:
                    try:
                        self.client.publish(m)
                        return True
                    except Exception:
                        self.valid = False
                        logger.error("publish failed and retry 500ms" + str(traceback.format_exc()))
                        time.sleep(0.5)
                        continue
                else:
                    self.dest = self.__getUrl()
                    if self.dest is None:
                        self.valid = False
                        time.sleep(3)
                        continue
                    self.client = PerSessionClient(self.dest.brokerserver, self.dest.sessionId, self.t.name, self.conf)
                    try:
                        self.client.publish(m)
                        self.valid = True
                        return True
                    except Exception:
                        self.valid = False
                        logger.error("publish failed and retry after 1s " + str(traceback.format_exc()))
                        time.sleep(1)
                        continue
            #mean can not pub and closed
            return False
        finally:
            self.lock.release()
    
    def destory(self):
        self.close = True
        self.lock.acquire();
        try:
            self.client.destroy()
        except Exception:
            logger.error("close failed " + str(traceback.format_exc()))
        finally:
            self.lock.release()
    
            
            
            
        
