namespace java com.taobao.timetunnel.thrift.gen

exception Failure {
  1: i16     code,        
  2: string reason,   
  3: string detail    
}

service ExternalService {
  void post(1:string category, 2:binary token, 3:binary message) throws (1:Failure f), 

  list<binary> ackAndGet(1:string category, 2:binary token) throws (1:Failure f)
}

service InternalService {
  
  void copy(1:string category, 2:binary token, 3:binary message) throws (1:Failure f),
  
  void dump(1:string category, 2:binary token, 3:binary message) throws (1:Failure f),

  void trim(1:string category, 2:binary token, 3:i32 size) throws (1:Failure f)
}

