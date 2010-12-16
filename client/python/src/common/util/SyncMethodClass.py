#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-6-3

@author: jiugao
'''
import inspect

def wrap_callable(any_callable, before, after):
    def _wrapped(*a, **kw):
        before()
        try:
            return any_callable(*a, **kw)
        finally:
            after()
            
    return _wrapped

class GenericWrapper(object):
    
    def __init__(self, obj, before, after, ignore=()):
        
        clasname = 'GenericWrapper'
        self.__dict__['_%s__methods' % clasname] = {}
        self.__dict__['_%s__obj' % clasname] = obj
        for name, method in inspect.getmembers(obj, inspect.ismethod):
            if name not in ignore and method not in ignore:
                self.__methods[name] = wrap_callable(method, before, after)
    
    def __getattr__(self, name):
        try:
            return self.__methods[name]
        except KeyError:
            return getattr(self.__obj, name)
    def __setattr__(self, name, value):
        setattr(self.__obj, name, value)

class SynchronizedObject(GenericWrapper):
    ''' wrap an object and all of its methods with synchronization '''
    def __init__(self, obj, ignore=(), lock=None):
        if lock is None:
            import threading
            lock = threading.RLock()
        GenericWrapper.__init__(self, obj, lock.acquire, lock.release, ignore)

