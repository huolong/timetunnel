#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-21

@author: jiugao
'''
from client.Authentication import Authentication
from common.Config import Config
from common.exception.ConnectionException import ConnectionException
from common.exception.SessionClosedException import SessionClosedException
from common.exception.URLException import URLException
from destination.DestQueryInfo import DestQueryInfo
from thrift.protocol import TBinaryProtocol
from thrift.transport import TSocket, TTransport
from thrift.transport.TTransport import TException
from time import sleep
import json, os
import logging
import traceback
from generated.routerservice import RouterService, constants
import socket
from generated.routerservice.ttypes import RouterException
from common.util import SleepUtils

logger = logging.getLogger("tt2-client.Destination")

MAX_FAILURE_RETRIES = 3

class Destination(object):
    
    def __init__(self, conf):
        self.conf = conf
        self.close = False
        self.transport = None
    
    def connect(self):
        SleepUtils.sleepRandom(3)
        self.url = self.conf.getRandomUrl()
        logger.info("connecting to router " + str(self.url))
        self.close = False
        for i in range(MAX_FAILURE_RETRIES):
            try:
                self.transport = TTransport.TBufferedTransport(TSocket.TSocket(self.url.ip, self.url.port))
                self.transport = TTransport.TFramedTransport(self.transport)
                protocol = TBinaryProtocol.TBinaryProtocol(self.transport)
                self.client = RouterService.Client(protocol)
                self.transport.open()
                logger.info("router connection has been established")
                return
            except TException:
                logger.info("can not connect to router: " + repr(self.url) + " and try " + str(i) + " times " + str(traceback.format_exc()))
                self.transport = None
                sleep(3)
                continue
        logger.error("can not connect to router: " + repr(self.url))
        raise ConnectionException("can not connect to router: " + str(self.url))
    
    def getDest(self, queryInfo):
        logger.info("get Dest for " + repr(queryInfo))
        if self.close is True:
            raise SessionClosedException("session has been closed")
        if self.transport is None:
            self.connect()
    
        urls = None
        try:
            prop = {}
            prop[constants.LOCAL_HOST] = str(os.getpid()) + "@" + socket.gethostname()
            prop[constants.RECVWINSIZE] = "0"
            prop[constants.TIMEOUT] = str(queryInfo.timeout)
            prop[constants.TYPE] = "PUB"
            urls = self.client.getBroker(queryInfo.user, queryInfo.pwd, queryInfo.topic, queryInfo.only, prop)
#            urls = "{\"sessionId\":\"8045f5cb0521c82598584f3151b1a1d5\",\"brokerserver\":[\"{\\\"main\\\":\\\"localhost:8888\\\",\\\"subordinate\\\":[]}\"]}"
            logger.debug("Urls from router: " + urls)
            return UrlDecoder(urls)
        except TException, e:
            self.__cleanup()
            raise URLException("get url via thrift failed. " + repr(e) + str(traceback.format_exc()))
        except RouterException, e:
            self.__cleanup()
            raise URLException("get url via thrift failed. " + repr(e) + str(traceback.format_exc()))
        except Exception:
            self.__cleanup()
            raise Exception("url: " + urls + " decoder failed. " + str(traceback.format_exc()))
            
    
    def __cleanup(self):
        if self.transport is not None:
            try:
                self.transport.close()
            finally:
                self.transport = None
                
    def destroy(self):
        logger.info("destroy router connection")
        if self.close is True:
            return
        if self.transport is None:
            self.close = True
            return
        self.__cleanup()
        self.close = True

class Dest(object):
    def __init__(self, sid, bs):
        self.sessionId = sid
        self.brokerserver = bs
    
    def __repr__(self):
        L = ["%s=%r" % (k, v) for k, v in self.__dict__.iteritems()]
        return "%s(%s)" % (self.__class__.__name__, ",".join(L))

def UrlDecoderHook(s):
    sid = s.get("sessionId")
    bs = s.get("brokerserver")
    return Dest(sid, bs)

def UrlDecoder(s):
    return json.loads(s, object_hook=UrlDecoderHook)

if __name__ == '__main__':

    print "case0##############"
    conf = Config('../../conf/client.conf')
    qi = DestQueryInfo(Authentication("tt", "2"), "test1", "1", "20000")
    print qi
    dest = Destination(conf);
    url = dest.getDest(qi)
    print url
    dest.destroy()
    
    print "case1##############"
    testUrl = "{\"sessionId\":\"8045f5cb0521c82598584f3151b1a1d5\",\"brokerserver\":[\"dwbasis130001.sqa.cm4:39903\", \"er2\"]}"
    dest = UrlDecoder(testUrl)
    print dest.sessionId
    print dest.brokerserver
    print dest.brokerserver[0]
    print dest.brokerserver[1]
    
    
