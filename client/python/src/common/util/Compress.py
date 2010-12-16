#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-11-4

@author: jiugao
'''
import zlib

def compress(content):
    return zlib.compress(content, 9)

def decompress(content):
    return zlib.decompress(content)
