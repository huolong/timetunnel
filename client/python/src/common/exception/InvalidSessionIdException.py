#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-6-4

@author: jiugao
'''
class InvalidSessionIdException(Exception):
    
    def __init__(self, msg):
        Exception.__init__(self, msg)