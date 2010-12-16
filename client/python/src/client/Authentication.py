#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-21

@author: jiugao
'''

class Authentication(object):
    def __init__(self, user, pwd):
        self.username = user
        self.password = pwd
        
    def __str__(self):
        return "Authentication: user: " + self.username + " pwd: " + self.password
    
    def __repr__(self):
        L = ["%s=%r" % (k, v) for k, v in self.__dict__.iteritems()]
        return "%s(%s)" % (self.__class__.__name__, ",".join(L))

