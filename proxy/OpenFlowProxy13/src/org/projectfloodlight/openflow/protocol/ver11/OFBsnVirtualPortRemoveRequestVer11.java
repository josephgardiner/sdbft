// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver11;

import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.protocol.actionid.*;
import org.projectfloodlight.openflow.protocol.bsntlv.*;
import org.projectfloodlight.openflow.protocol.errormsg.*;
import org.projectfloodlight.openflow.protocol.meterband.*;
import org.projectfloodlight.openflow.protocol.instruction.*;
import org.projectfloodlight.openflow.protocol.instructionid.*;
import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.protocol.stat.*;
import org.projectfloodlight.openflow.protocol.oxm.*;
import org.projectfloodlight.openflow.protocol.oxs.*;
import org.projectfloodlight.openflow.protocol.queueprop.*;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.util.*;
import org.projectfloodlight.openflow.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import io.netty.buffer.ByteBuf;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

class OFBsnVirtualPortRemoveRequestVer11 implements OFBsnVirtualPortRemoveRequest {
    private static final Logger logger = LoggerFactory.getLogger(OFBsnVirtualPortRemoveRequestVer11.class);
    // version: 1.1
    final static byte WIRE_VERSION = 2;
    final static int LENGTH = 20;

        private final static long DEFAULT_XID = 0x0L;
        private final static long DEFAULT_VPORT_NO = 0x0L;

    // OF message fields
    private final long xid;
    private final long vportNo;
//
    // Immutable default instance
    final static OFBsnVirtualPortRemoveRequestVer11 DEFAULT = new OFBsnVirtualPortRemoveRequestVer11(
        DEFAULT_XID, DEFAULT_VPORT_NO
    );

    // package private constructor - used by readers, builders, and factory
    OFBsnVirtualPortRemoveRequestVer11(long xid, long vportNo) {
        this.xid = xid;
        this.vportNo = vportNo;
    }

    // Accessors for OF message fields
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_11;
    }

    @Override
    public OFType getType() {
        return OFType.EXPERIMENTER;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x11L;
    }

    @Override
    public long getVportNo() {
        return vportNo;
    }



    public OFBsnVirtualPortRemoveRequest.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFBsnVirtualPortRemoveRequest.Builder {
        final OFBsnVirtualPortRemoveRequestVer11 parentMessage;

        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean vportNoSet;
        private long vportNo;

        BuilderWithParent(OFBsnVirtualPortRemoveRequestVer11 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_11;
    }

    @Override
    public OFType getType() {
        return OFType.EXPERIMENTER;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFBsnVirtualPortRemoveRequest.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x11L;
    }

    @Override
    public long getVportNo() {
        return vportNo;
    }

    @Override
    public OFBsnVirtualPortRemoveRequest.Builder setVportNo(long vportNo) {
        this.vportNo = vportNo;
        this.vportNoSet = true;
        return this;
    }


        @Override
        public OFBsnVirtualPortRemoveRequest build() {
                long xid = this.xidSet ? this.xid : parentMessage.xid;
                long vportNo = this.vportNoSet ? this.vportNo : parentMessage.vportNo;

                //
                return new OFBsnVirtualPortRemoveRequestVer11(
                    xid,
                    vportNo
                );
        }

    }

    static class Builder implements OFBsnVirtualPortRemoveRequest.Builder {
        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean vportNoSet;
        private long vportNo;

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_11;
    }

    @Override
    public OFType getType() {
        return OFType.EXPERIMENTER;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFBsnVirtualPortRemoveRequest.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public long getExperimenter() {
        return 0x5c16c7L;
    }

    @Override
    public long getSubtype() {
        return 0x11L;
    }

    @Override
    public long getVportNo() {
        return vportNo;
    }

    @Override
    public OFBsnVirtualPortRemoveRequest.Builder setVportNo(long vportNo) {
        this.vportNo = vportNo;
        this.vportNoSet = true;
        return this;
    }
//
        @Override
        public OFBsnVirtualPortRemoveRequest build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            long vportNo = this.vportNoSet ? this.vportNo : DEFAULT_VPORT_NO;


            return new OFBsnVirtualPortRemoveRequestVer11(
                    xid,
                    vportNo
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFBsnVirtualPortRemoveRequest> {
        @Override
        public OFBsnVirtualPortRemoveRequest readFrom(ByteBuf bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property version == 2
            byte version = bb.readByte();
            if(version != (byte) 0x2)
                throw new OFParseError("Wrong version: Expected=OFVersion.OF_11(2), got="+version);
            // fixed value property type == 4
            byte type = bb.readByte();
            if(type != (byte) 0x4)
                throw new OFParseError("Wrong type: Expected=OFType.EXPERIMENTER(4), got="+type);
            int length = U16.f(bb.readShort());
            if(length != 20)
                throw new OFParseError("Wrong length: Expected=20(20), got="+length);
            if(bb.readableBytes() + (bb.readerIndex() - start) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            if(logger.isTraceEnabled())
                logger.trace("readFrom - length={}", length);
            long xid = U32.f(bb.readInt());
            // fixed value property experimenter == 0x5c16c7L
            int experimenter = bb.readInt();
            if(experimenter != 0x5c16c7)
                throw new OFParseError("Wrong experimenter: Expected=0x5c16c7L(0x5c16c7L), got="+experimenter);
            // fixed value property subtype == 0x11L
            int subtype = bb.readInt();
            if(subtype != 0x11)
                throw new OFParseError("Wrong subtype: Expected=0x11L(0x11L), got="+subtype);
            long vportNo = U32.f(bb.readInt());

            OFBsnVirtualPortRemoveRequestVer11 bsnVirtualPortRemoveRequestVer11 = new OFBsnVirtualPortRemoveRequestVer11(
                    xid,
                      vportNo
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", bsnVirtualPortRemoveRequestVer11);
            return bsnVirtualPortRemoveRequestVer11;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFBsnVirtualPortRemoveRequestVer11Funnel FUNNEL = new OFBsnVirtualPortRemoveRequestVer11Funnel();
    static class OFBsnVirtualPortRemoveRequestVer11Funnel implements Funnel<OFBsnVirtualPortRemoveRequestVer11> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFBsnVirtualPortRemoveRequestVer11 message, PrimitiveSink sink) {
            // fixed value property version = 2
            sink.putByte((byte) 0x2);
            // fixed value property type = 4
            sink.putByte((byte) 0x4);
            // fixed value property length = 20
            sink.putShort((short) 0x14);
            sink.putLong(message.xid);
            // fixed value property experimenter = 0x5c16c7L
            sink.putInt(0x5c16c7);
            // fixed value property subtype = 0x11L
            sink.putInt(0x11);
            sink.putLong(message.vportNo);
        }
    }


    public void writeTo(ByteBuf bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFBsnVirtualPortRemoveRequestVer11> {
        @Override
        public void write(ByteBuf bb, OFBsnVirtualPortRemoveRequestVer11 message) {
            // fixed value property version = 2
            bb.writeByte((byte) 0x2);
            // fixed value property type = 4
            bb.writeByte((byte) 0x4);
            // fixed value property length = 20
            bb.writeShort((short) 0x14);
            bb.writeInt(U32.t(message.xid));
            // fixed value property experimenter = 0x5c16c7L
            bb.writeInt(0x5c16c7);
            // fixed value property subtype = 0x11L
            bb.writeInt(0x11);
            bb.writeInt(U32.t(message.vportNo));


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFBsnVirtualPortRemoveRequestVer11(");
        b.append("xid=").append(xid);
        b.append(", ");
        b.append("vportNo=").append(vportNo);
        b.append(")");
        return b.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFBsnVirtualPortRemoveRequestVer11 other = (OFBsnVirtualPortRemoveRequestVer11) obj;

        if( xid != other.xid)
            return false;
        if( vportNo != other.vportNo)
            return false;
        return true;
    }

    @Override
    public boolean equalsIgnoreXid(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFBsnVirtualPortRemoveRequestVer11 other = (OFBsnVirtualPortRemoveRequestVer11) obj;

        // ignore XID
        if( vportNo != other.vportNo)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        result = prime *  (int) (vportNo ^ (vportNo >>> 32));
        return result;
    }

    @Override
    public int hashCodeIgnoreXid() {
        final int prime = 31;
        int result = 1;

        // ignore XID
        result = prime *  (int) (vportNo ^ (vportNo >>> 32));
        return result;
    }

}