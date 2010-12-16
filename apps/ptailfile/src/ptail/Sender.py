#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-11-2

@author: jiugao
'''
import os.path
import logging
from client.TimeTunnel2 import tunnel, post
import traceback

logger = logging.getLogger("tailfile.sender")

def get_lines_len(content, len):
    length = len - 1
    while length >= 0:
        if content[length] == '\n':
            break
        else:
            length -= 1
            continue
    length += 1
    return length

def get_to_send(content, in_len, max_len, encoding):
    length = get_lines_len(content, in_len)
    if length == 0:
        if in_len == max_len:
            logger.error("more than max len and no \\n contain, skip it")
            return ToSend(None, in_len)
        else:
            return ToSend(None, 0)
    else:
        sub_content = content[:length]
        if encoding.lower() != "utf-8":
            c = sub_content.decode(encoding).encode("utf-8")
            return ToSend(c, length)
        else:
            return ToSend(sub_content, length)
        

class ToSend(object):
    def __init__(self, c, len):
        self.c = c
        self.len = len
            
    def __repr__(self):
        L = ["%s=%r" % (k, v) for k, v in self.__dict__.iteritems()]
        return "%s(%s)" % (self.__class__.__name__, ",".join(L))

class TTSender(object):
    def __init__(self, max_buf_len, topic, timeout, encoding, compress, seq):
        self.max_buf_len = max_buf_len
        self.encoding = encoding
        cmpz = False
        if compress.lower() == "true":
            cmpz = True
        sequence=False
        if seq.lower() =="true":
            sequence=True
        self.tunnel = tunnel(topic, cmpz, timeout, sequence)
        
    
    def send(self, content, len, filename=None):
        c = get_to_send(content, len, self.max_buf_len, self.encoding)
        if c.c is not None:
            while True:
                try:
                    props = {"SOURCE":filename}
                    post(self.tunnel, c.c, props)
                    break
                except Exception, e:
                    logger.error("post to TT failed and retry: ")
                    logger.error(str(e))
                    logger.error(traceback.format_exc())
                    continue
        return c.len
    
    def close(self):
        pass

class DiskSender(object):
    def __init__(self, base_path, topic, encoding, max_buf_len):
        self.encoding = encoding
        self.max_buf_len = max_buf_len
        self.basename = "data_"
        self.full_name = base_path + os.sep + topic + os.sep
        self.max_size = 64 * 1024 * 1024
        if os.path.exists(self.full_name) is False:
            os.makedirs(self.full_name, 0744)
        else:
            for f in os.listdir(self.full_name):
                os.remove(self.full_name + f)
        self.w_f = None
        self.len = 0
        self.index = 0
        pass

    def send(self, content, len, filename=None):
        f = self.__get_write_file()
        c = get_to_send(content, len, self.max_buf_len, self.encoding)
        if c.c is not None:  
            f.write(c.c)
            f.flush()
            logger.debug("content: "+str(c.c)+" has been written")
        return c.len
    
    def __get_write_file(self):
        if self.w_f is None:
            self.w_f = open(self.__create_file_name(), "wb")
        else:
            if self.len > self.max_size:
                self.w_f.close()
                self.w_f = open(self.__create_file_name(), "wb")
        return self.w_f
    
    def __create_file_name(self):
        self.index += 1
        return self.full_name + self.basename + str(self.index)
    
    def close(self):
        if self.w_f is not None:
            self.w_f.close()
            
if __name__ == '__main__':
    disk_sender = DiskSender("./out/", "t2", "utf-8", 24)
    disk_sender.send("中文\n", len("中文\n"))

        
        
