package top.dreamlike.panama.nativelib;

import java.lang.foreign.MemorySegment;

public interface Libc {
    int open(MemorySegment pathname, int flags);

    int read(int fd, MemorySegment buf, int count);

    int write(int fd, MemorySegment buf, int count);

    int close(int fd);

    int socket(int domain, int type, int protocol);




    interface Fcntl {
        static final int O_RDONLY = 0;
        static final int O_WRONLY = 1;
        static final int O_RDWR = 2;
        static final int O_CREAT = 64;
        static final int O_EXCL = 128;
        static final int O_TURNC = 512;
        static final int O_APPEND = 1024;
        static final int O_NONBLOCK = 2048;
        static final int O_SYNC = 1052672;
        static final int O_ASYNC = 8192;
        static final int O_DIRECT = 16384;
        static final int O_DIRECTORY = 65536;
        static final int O_NOFOLLOW = 131072;
        static final int O_CLOEXEC = 524288;
        static final int O_PATH = 2097152;
    }

    interface Socket {
        interface Domain {
            static final int AF_UNIX = 1;
            static final int AF_LOCAL = 1;
            static final int AF_INET = 2;
            static final int AF_AX25 = 3;
            static final int AF_IPX = 4;
            static final int AF_APPLETALK = 5;
            static final int AF_X25 = 9;
            static final int AF_INET6 = 10;
            static final int AF_DECnet = 12;
            static final int AF_KEY = 15;
            static final int AF_NETLINK = 16;
            static final int AF_PACKET = 17;
            static final int AF_RDS = 21;
            static final int AF_PPPOX = 24;
            static final int AF_LLC = 26;
            static final int AF_IB = 27;
            static final int AF_MPLS = 28;
            static final int AF_CAN = 29;
            static final int AF_TIPC = 30;
            static final int AF_BLUETOOTH = 31;
            static final int AF_IUCV = 32;
            static final int AF_RXRPC = 33;
            static final int AF_ISDN = 34;
            static final int AF_PHONET = 35;
            static final int AF_IEEE802154 = 36;
            static final int AF_CAIF = 37;
            static final int AF_ALG = 38;
            static final int AF_NFC = 39;
            static final int AF_VSOCK = 40;
            static final int AF_KCM = 41;
            static final int PF_QIPCRTR = 42;
            static final int AF_SMC = 43;
            static final int AF_XDP = 44;
            static final int AF_MCTP = 45;
            static final int AF_MAX = 46;

        }

        interface Type {
            static final int SOCK_STREAM = 1;
            static final int SOCK_DGRAM = 2;
            static final int SOCK_RAW = 3;
            static final int SOCK_RDM = 4;
            static final int SOCK_SEQPACKET = 5;
            static final int SOCK_DCCP = 6;
            static final int SOCK_PACKET = 10;
            static final int SOCK_CLOEXEC = 02000000;
            static final int SOCK_NONBLOCK = 04000;
        }
    }
}
