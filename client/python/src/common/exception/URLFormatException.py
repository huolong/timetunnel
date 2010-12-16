#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-5-31

@author: jiugao
'''

class URLFormatException(Exception):
    def __init__(self, msg):
        Exception.__init__(self, msg)