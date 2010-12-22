#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-22

@author: jiugao
'''

class Tunnel(object):
    def __init__(self, name, compress=False, sequence=False, timeout=None):
        self.name = name
        self.compress = compress
        self.sequence = sequence
        if timeout is None:
            self.timeout = 1800
        else:
            self.timeout = timeout
        self.signature = self.name + str(self.compress) + str(self.timeout) + str(self.sequence)
    
    def __str__(self):
        return "tunnle: " + self.name + " and compress: " + str(self.compress) + " and timeout: " + str(self.timeout) + " and sequence: " + str(self.sequence)
    
    def __repr__(self):
        L = ["%s=%r" % (k, v) for k, v in self.__dict__.iteritems()]
        return "%s(%s)" % (self.__class__.__name__, ",".join(L))
    
    def __eq__(self, o):
        return self.signature == o.signature
    
    def __hash__(self):
        return hash(self.signature)
    
if __name__ == '__main__':
    print repr(Tunnel("h", True))
    print hash(Tunnel("h", False))
    print hash(Tunnel("h", False))
    print hash(Tunnel("h", True))
