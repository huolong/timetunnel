#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-21

@author: jiugao
'''

import ConfigParser
import logging
import sys
import traceback
import locale
from random import Random
import string

logger = logging.getLogger("tt2-client.Config")

class Url(object):
    def __init__(self, url):
        pair = url.split(":")
        self.ip = pair[0]
        self.port = locale.atoi(pair[1])
    
    def __str__(self):
        return "ip: " + self.ip + " port: " + str(self.port)
    
    def __repr__(self):
        L = ["%s=%r" % (k, v) for k, v in self.__dict__.iteritems()]
        return "%s(%s)" % (self.__class__.__name__, ",".join(L))
    
class Config:
    def __init__(self, filename):
        logger.debug("init in configMgr")
        self.confFile = filename
        self.random = Random()
        config = ConfigParser.ConfigParser()
        #default urls
        self.routerUrls = []
        self.timeout = 10000
        try:
            fd = open(filename)
        except:
            logger.info("no config file " + filename) 
            return
        try:
            config.readfp(fd)
        except IOError:
            fd.close()
            logger.error('read configuration error ' + str(traceback.format_exc()))
            sys.exit(1)
        try:
            self.__load(config)
        except:
            logger.error('load configuration error ' + str(traceback.format_exc()))
            sys.exit(1)
        finally:
            fd.close()

    def __load(self, config):
        self.__init_routerUrls(config)
        self.__init_timout(config)
    
    def __init_routerUrls(self, config):
        value = config.get('router', 'url')
        if value is None or value is "":
            logger.error("no router url defined in conf " + self.confFile + " and use default")
            return
        for url in value.split(";"):
            url = url.strip()
            if url is None or url is "":
                continue
            self.routerUrls.append(Url(url))
            
    def __init_timout(self, config):
        value = config.get('rpc_timeout', 'timeout')
        if value is None or value is "":
            logger.error("rpc timeout not defined in conf " + self.confFile + " and use default 10s")
            return
        self.timeout = string.atoi(value)
                
    def getRandomUrl(self):
        if(len(self.routerUrls) == 0):
            logger.error('get url error ' + str(traceback.format_exc()))
            sys.exit(-1)
        i = self.random.randint(0, len(self.routerUrls) - 1)
        return self.routerUrls[i]
    
    def setRouters(self, urls):
        if len(self.routerUrls) > 0:
            return
        
        for url in urls.split(";"):
            url = url.strip()
            if url is None or url is "":
                continue
            self.routerUrls.append(Url(url))
    
    def getTimeout(self):
        return self.timeout
    
    def setTimeout(self, timeout):
        self.timeout = timeout
        
if __name__ == '__main__':
    conf = Config('../../conf/client.conf')
    for i in conf.routerUrls:
        print i;
#    print "%r" % conf.getRandomUrl()
    conf.setRouters("sdfs:90;seerett:944")
    print "%r" % conf.getRandomUrl()
