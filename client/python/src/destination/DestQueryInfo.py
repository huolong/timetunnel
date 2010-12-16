#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-21

@author: jiugao
'''
class DestQueryInfo(object):
    
    def __init__(self, auth, topic, only, timeout):
        self.user = auth.username
        self.pwd = auth.password
        self.topic = topic
        if only is False:
            self.only = "0"
        else:
            self.only = "1"
        self.timeout = timeout
    
    def __str__(self):
        return "Authentication: user: " + self.user + " pwd: " + self.pwd + " topic: " + self.topic + " only: " + self.only + " timeout: " + self.timeout
    
    def __repr__(self):
        L = ["%s=%r" % (k, v) for k, v in self.__dict__.iteritems()]
        return "%s(%s)" % (self.__class__.__name__, ",".join(L))
