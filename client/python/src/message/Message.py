#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-5-31

@author: jiugao
'''
from thrift.TSerialization import serialize, deserialize
from generated.message.ttypes import MessageStruct
from common.util import Compress

map_key = {"COMPRESSD":"c", "TIMESTAMP":"ts", "COMPRESSALGO":"algo"}
map_v = {"YES":"y", "NO":"n"}

class Message:
    def __init__(self, topic=None, id=None, content=None, ipAddress=None, createdTime=None, props=None):
        self.message = MessageStruct(topic, content, createdTime, id, ipAddress, props)
    
    def getTopic(self):
        return self.message.topic
    
    def getConent(self):
        return self.message.content
    
    def getId(self):
        return self.message.id
    
    def getIpAddress(self):
        return self.message.ipAddress
    
    def getCreatedTime(self):
        return self.message.createdTime
    
    def getProps(self):
        return self.message.props
    
    def toBytes(self):
        return serialize(self.message)
    
    def fromBytes(self, bytes):
        deserialize(self.message, bytes)
        
    def isCompress(self):
        if self.message.props.has_key(map_key["COMPRESSD"]) is False:
            return False
        return map_v["YES"] == self.message.props[map_key["COMPRESSD"]]
    
    def compress(self):
        if self.isCompress():
            return
        self.message.content = Compress.compress(self.message.content)
        self.__setCompress(True)
        self.__setCompressAlgo("ZLIB")
        
    def __setCompress(self, flag):
        if flag:
            self.message.props[map_key["COMPRESSD"]] = map_v["YES"]
        else:
            self.message.props[map_key["COMPRESSD"]] = map_v["NO"]
            
    def __setCompressAlgo(self, algo):
        self.message.props[map_key["COMPRESSDALGO"]] = algo
    
    def decompress(self):
        if self.isCompress() is False:
            return
        self.message.content = Compress.decompress(self.message.content)
        self.__setCompress(False)
        
    def __eq__(self, other):
        return isinstance(other, self.__class__) and self.__dict__ == other.__dict__
       
    def __ne__(self, other):
        return not (self == other)
 
    def __str__(self):
        return self.getId()
    
    def __repr__(self):
        L = ['%s=%r' % (key, value) for key, value in self.__dict__.iteritems()]
        return '%s(%s)' % (self.__class__.__name__, ', '.join(L))
    
    def __cmp__(self, other):
        return cmp(self.getId(), other.getId())
    
    def __hash__(self):
        return hash(self.getId())
        
        
