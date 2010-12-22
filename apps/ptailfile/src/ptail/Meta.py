#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-11-1

@author: jiugao
'''
import os
from string import atol
import logging
import traceback
from threading import Thread
from time import sleep

logger = logging.getLogger("tailfile.meta")

SEP = "_"

class MetaUrl(object):
    def __init__(self, path, name):
        self.path = path
        self.name = name

    def __repr__(self):
        L = ["%s=%r" % (k, v) for k, v in self.__dict__.iteritems()]
        return "%s(%s)" % (self.__class__.__name__, ",".join(L))
    
class MetaInfo(object):
    def __init__(self, last_file=None, cur_file=None, offset=None):
        if last_file is None:
            self.last_file = "null"
        else:
            self.last_file = last_file
        if cur_file is None:
            self.cur_file = "null"
        else:
            self.cur_file = cur_file
        if offset is None:
            self.offset = 0L
        else:
            self.offset = offset
         
    
    def set(self, line):
        self.last_file = line[0].rstrip("\n")
        self.cur_file = line[1].rstrip("\n")
        self.offset = atol(line[2])
        
    def __str__(self):
        return "last: " + self.last_file + " cur: " + self.cur_file + " offset: " + str(self.offset)
    
    def __repr__(self):
        L = ["%s=%r" % (k, v) for k, v in self.__dict__.iteritems()]
        return "%s(%s)" % (self.__class__.__name__, ",".join(L))
    
    def content(self):
        return self.last_file + "\n" + self.cur_file + "\n" + str(self.offset)

class Meta(object):
    def __init__(self, meta_url):
        global SEP
        self.meta = None
        self.index = 0L
        self.dir = meta_url.path
        self.name = meta_url.name + SEP
        pass
    
    def startRecoding(self):
        class Recoder(Thread):
            def __init__(self, func, name):
                self.func = func
                self.end = False
                Thread.__init__(self, name="thread_wirte_meta_" + name)
            
            def run(self):
                while self.end is False:
                    sleep(1)
                    self.func()
            
            def join(self, timeout=None):
                self.end = True
                Thread.join(self, timeout) 
        
        self.rec = Recoder(self.write_disk, self.name)
        self.rec.start()
    
    def write_disk(self):
        print "sync to disk"
        if self.meta is not None:
            last_index = self.index
            self.index += 1
            name = self.__bulidname(self.index)
            try:
                f = open(name, "w")
                f.write(self.meta.content())
                logger.debug("write meta to disk: " + str(self.meta.content()))
            except Exception, e:
                logger.error(e)
                raise e
            finally:
                f.close()         
                   
            try:
                last_name = self.__bulidname(last_index)
                if os.path.exists(last_name):
                    os.remove(last_name)
            except Exception, e:
                logger.error(e)
    
    def flush(self, meta_info):
        self.meta = meta_info
    
    def close(self):
        if hasattr(self, "rec"):
            self.rec.join(2)
        self.write_disk();
    
    def restore(self):
        highest_index = -1L
        if os.path.exists(self.dir) is False:
            os.makedirs(self.dir, 0744)
        
        files = os.listdir(self.dir)
        if files is None or len(files) == 0:
            return MetaInfo()
        global SEP
        next_higest_index = -1L
        
        for f in files:
            print "restore meta file: "+str(f)
            if f.startswith(self.name) is False:
                continue
            rp = f.rpartition(SEP)
            if len(rp) == 0:
                logger.error("error filename format: " + f)
                print "error filename format: " + f
                continue
            try:
                num = atol(rp[len(rp) - 1])
            except:
                logger.error("error filename format: " + f)
                print "error filename format: " + f
                continue
            if num > highest_index:
                full_path = self.__bulidname(next_higest_index)
                if os.path.exists(full_path):
                    os.remove(full_path)
                next_higest_index = highest_index
                highest_index = num
        
        self.index = highest_index
        return self.__load_from_file(next_higest_index)
       
    def __bulidname(self, index):
        return self.dir + os.sep + self.name + str(index)
        
    def __load_from_file(self, next_higest_index):
        if self.index == -1L:
            logger.info("no meta found")
            print "no meta found"
            return MetaInfo()
        meta_name = self.__bulidname(self.index)
        next_meta_name = self.__bulidname(next_higest_index)
        if os.path.exists(meta_name) is False:
            raise Exception("load from file for meta failed")
        try:
            meta = self.__setMeta(meta_name)
            if os.path.exists(next_meta_name) is True:
                os.remove(next_meta_name)
            return meta
        except Exception, e:
            logger.error(e)
            logger.error(traceback.format_exc())
            if os.path.exists(next_meta_name) is False:
                raise Exception("load from file for meta failed")
            else:
                os.remove(meta_name)
                meta = self.__setMeta(next_meta_name)
                return meta
            
    def __setMeta(self, file_name):       
        f = open(file_name, "r")
        line = None
        try:
            line = f.readlines()
        except Exception, e:
            logger.error(e)
            logger.error(traceback.format_exc())
            raise Exception("load from file for meta failed")
        finally:
            f.close()
        meta = MetaInfo()
        meta.set(line)
        return meta
            

if __name__ == '__main__':
    meta = Meta(MetaUrl("../../conf/test", "test"))
    print meta.restore()
    meta.flush(MetaInfo("ml3", "m23", 4588))
    meta.startRecoding()
    meta.flush(MetaInfo("m2l3", "m223", 9999))
    sleep(10)
    meta.close()
    

        
    
    
