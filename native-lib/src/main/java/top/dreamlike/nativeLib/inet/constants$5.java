// Generated by jextract

package top.dreamlike.nativeLib.inet;

import top.dreamlike.common.CType;
import top.dreamlike.helper.RuntimeHelper;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
class constants$5 {

    static final  GroupLayout in6addr_any$LAYOUT = MemoryLayout.structLayout(
        MemoryLayout.unionLayout(
            MemoryLayout.sequenceLayout(16, CType.C_CHAR$LAYOUT).withName("__u6_addr8"),
            MemoryLayout.sequenceLayout(8, CType.C_SHORT$LAYOUT).withName("__u6_addr16"),
            MemoryLayout.sequenceLayout(4, CType.C_INT$LAYOUT).withName("__u6_addr32")
        ).withName("__in6_u")
    ).withName("in6_addr");
    static final MemorySegment in6addr_any$SEGMENT = RuntimeHelper.lookupGlobalVariable("in6addr_any", constants$5.in6addr_any$LAYOUT);
    static final  GroupLayout in6addr_loopback$LAYOUT = MemoryLayout.structLayout(
        MemoryLayout.unionLayout(
            MemoryLayout.sequenceLayout(16, CType.C_CHAR$LAYOUT).withName("__u6_addr8"),
            MemoryLayout.sequenceLayout(8, CType.C_SHORT$LAYOUT).withName("__u6_addr16"),
            MemoryLayout.sequenceLayout(4, CType.C_INT$LAYOUT).withName("__u6_addr32")
        ).withName("__in6_u")
    ).withName("in6_addr");
    static final MemorySegment in6addr_loopback$SEGMENT = RuntimeHelper.lookupGlobalVariable("in6addr_loopback", constants$5.in6addr_loopback$LAYOUT);
    static final FunctionDescriptor ntohl$FUNC = FunctionDescriptor.of(CType.C_INT$LAYOUT,
        CType.C_INT$LAYOUT
    );
    static final MethodHandle ntohl$MH = RuntimeHelper.downcallHandle(
        "ntohl",
        constants$5.ntohl$FUNC
    );
    static final FunctionDescriptor ntohs$FUNC = FunctionDescriptor.of(CType.C_SHORT$LAYOUT,
        CType.C_SHORT$LAYOUT
    );
    static final MethodHandle ntohs$MH = RuntimeHelper.downcallHandle(
        "ntohs",
            constants$5.ntohs$FUNC,
            Linker.Option.critical(false)
    );
    static final FunctionDescriptor htonl$FUNC = FunctionDescriptor.of(CType.C_INT$LAYOUT,
        CType.C_INT$LAYOUT
    );
    static final MethodHandle htonl$MH = RuntimeHelper.downcallHandle(
        "htonl",
            constants$5.htonl$FUNC,
            Linker.Option.critical(false)
    );
    static final FunctionDescriptor htons$FUNC = FunctionDescriptor.of(CType.C_SHORT$LAYOUT,
        CType.C_SHORT$LAYOUT
    );
    static final MethodHandle htons$MH = RuntimeHelper.downcallHandle(
        "htons",
        constants$5.htons$FUNC
    );
}


