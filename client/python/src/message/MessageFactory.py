#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-6-1

@author: jiugao
'''


from message.Message import Message
import socket
import time
import uuid

class MessageFactory:
    
    @staticmethod
    def createMessage(topic, content, ipAddress=socket.gethostname(), createTime=long(time.time()*1000), props={}):
        return Message(topic, str(uuid.uuid4()), content ,ipAddress, createTime, props)
    
    @staticmethod
    def createMessageViaBytes(bytes):
        message=Message()
        message.fromBytes(bytes)
        return message

if __name__ == '__main__':
    m=MessageFactory.createMessage("t1", "hello", props={"source":"t1.txt"})
    print m
    print m.getProps()["source"]
    print m.getConent()
    print m.getCreatedTime()
    print m.getIpAddress()
    print m.getTopic()
    print m.isCompress()
    m.compress()
    print m.isCompress()
    print m.getConent()
    m.decompress()
    print m.isCompress()
    print m.getConent()
    
    b=m.toBytes()
    print b
    print MessageFactory.createMessageViaBytes(b).getConent()
    