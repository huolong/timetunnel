import static com.sun.btrace.BTraceUtils.*;

import com.sun.btrace.*;
import com.sun.btrace.annotations.*;

@BTrace
public class Tracer {
  @OnMethod(clazz = "/com.taobao.timetunnel.+/", method = "", location = @Location(value = Kind.RETURN))
  public static void trace(@ProbeClassName String clazz,
                    @ProbeMethodName String method,
                    @Self Object self,
                    @Return Object result) {

  }
/*
  @OnMethod(clazz = "/com.taobao.timetunnel.+/", method = "", location = @Location(value = Kind.ENTRY))
  public static void trace(@ProbeClassName String clazz,
                    @ProbeMethodName String method,
                    @Self Object self,
                    AnyType... args) {
    
  }
*/
}
