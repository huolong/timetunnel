#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-5-31

@author: jiugao
'''

from common.exception.ConnectionException import ConnectionException
from common.exception.PublishException import PublishException
from common.exception.SessionClosedException import SessionClosedException
from common.exception.URLFormatException import URLFormatException
from string import atoi, split
from thrift.protocol import TBinaryProtocol
from thrift.transport import TSocket, TTransport
from thrift.transport.TTransport import TException
from time import sleep
import logging
import traceback
from generated.services import ExternalService
from common.util import SleepUtils


MAX_FAILURE_RETRIES = 3

logger = logging.getLogger("tt2-client.PerSessionClient")

class PerSessionClient:
    
    def __init__(self, url, token, name, conf):
        self.url = url[0]
        self.__parserUrl()
        self.close = False
        self.transport = None
        self.token = token
        self.name = name
        self.conf = conf
    
    def __parserUrl(self):
        urlList = split(self.url, ':')
        if urlList.__len__ < 2:
            raise URLFormatException("the url format is incorrect: " + self.url)
        self.host = urlList[0]
        self.port = atoi(urlList[1])
        
    def connect(self):
        SleepUtils.sleepRandom(3)
        logger.info("connecting to url " + self.url)
        self.close = False
        for i in range(MAX_FAILURE_RETRIES):
            try:
                self.transport = TSocket.TSocket(self.host, self.port)
                self.transport.setTimeout(self.conf.getTimeout())
                self.transport = TTransport.TBufferedTransport(self.transport)
                self.transport = TTransport.TFramedTransport(self.transport)
                protocol = TBinaryProtocol.TBinaryProtocol(self.transport)
                self.client = ExternalService.Client(protocol)
                self.transport.open()
                logger.info("connection has been established")
                return
            except TException:
                logger.info("can not connect to url: " + self.url + " and try " + str(i) + " times " + str(traceback.format_exc()))
                self.transport = None
                sleep(3)
                continue
            
        logger.error("can not connect to url: " + self.url)
        raise ConnectionException("can not connect to url: " + self.url)
            
            
    def __cleanup(self):
        if self.transport is not None:
            try:
                self.transport.close()
                self.transport = None
            finally:
                self.transport = None
    
    def destroy(self):
        logger.error("close client: " + self.url)
        if self.close is True:
            return
        self.__cleanup()
        self.close = True
            
    def publish(self, message):
        logger.info("publish messages")
        if self.close is True:
            raise SessionClosedException("session has been closed")
        if self.transport is None:
            self.connect()
        try:
            self.client.post(self.name, self.token, message.toBytes())
            logger.info("publish successful")
            return
        except Exception, e:
            self.__cleanup()
            logger.error(repr(e) + "\n" + traceback.format_exc())
            raise PublishException("publish failed")


if __name__ == '__main__':
    for i in range(MAX_FAILURE_RETRIES):
            print i
