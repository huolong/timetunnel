#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-6-3

@author: jiugao
'''


class NoSessionAvaliableException(Exception):
    def __init__(self, msg):
        Exception.__init__(self,msg)