// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver13;

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

class OFOxmBsnLagIdVer13 implements OFOxmBsnLagId {
    private static final Logger logger = LoggerFactory.getLogger(OFOxmBsnLagIdVer13.class);
    // version: 1.3
    final static byte WIRE_VERSION = 4;
    final static int LENGTH = 8;

        private final static LagId DEFAULT_VALUE = LagId.NONE;

    // OF message fields
    private final LagId value;
//
    // Immutable default instance
    final static OFOxmBsnLagIdVer13 DEFAULT = new OFOxmBsnLagIdVer13(
        DEFAULT_VALUE
    );

    // package private constructor - used by readers, builders, and factory
    OFOxmBsnLagIdVer13(LagId value) {
        if(value == null) {
            throw new NullPointerException("OFOxmBsnLagIdVer13: property value cannot be null");
        }
        this.value = value;
    }

    // Accessors for OF message fields
    @Override
    public long getTypeLen() {
        return 0x30204L;
    }

    @Override
    public LagId getValue() {
        return value;
    }

    @Override
    public MatchField<LagId> getMatchField() {
        return MatchField.BSN_LAG_ID;
    }

    @Override
    public boolean isMasked() {
        return false;
    }

    public OFOxm<LagId> getCanonical() {
        // exact match OXM is always canonical
        return this;
    }

    @Override
    public LagId getMask()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property mask not supported in version 1.3");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }



    public OFOxmBsnLagId.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFOxmBsnLagId.Builder {
        final OFOxmBsnLagIdVer13 parentMessage;

        // OF message fields
        private boolean valueSet;
        private LagId value;

        BuilderWithParent(OFOxmBsnLagIdVer13 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public long getTypeLen() {
        return 0x30204L;
    }

    @Override
    public LagId getValue() {
        return value;
    }

    @Override
    public OFOxmBsnLagId.Builder setValue(LagId value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public MatchField<LagId> getMatchField() {
        return MatchField.BSN_LAG_ID;
    }

    @Override
    public boolean isMasked() {
        return false;
    }

    @Override
    public OFOxm<LagId> getCanonical()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property canonical not supported in version 1.3");
    }

    @Override
    public LagId getMask()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property mask not supported in version 1.3");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }



        @Override
        public OFOxmBsnLagId build() {
                LagId value = this.valueSet ? this.value : parentMessage.value;
                if(value == null)
                    throw new NullPointerException("Property value must not be null");

                //
                return new OFOxmBsnLagIdVer13(
                    value
                );
        }

    }

    static class Builder implements OFOxmBsnLagId.Builder {
        // OF message fields
        private boolean valueSet;
        private LagId value;

    @Override
    public long getTypeLen() {
        return 0x30204L;
    }

    @Override
    public LagId getValue() {
        return value;
    }

    @Override
    public OFOxmBsnLagId.Builder setValue(LagId value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public MatchField<LagId> getMatchField() {
        return MatchField.BSN_LAG_ID;
    }

    @Override
    public boolean isMasked() {
        return false;
    }

    @Override
    public OFOxm<LagId> getCanonical()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property canonical not supported in version 1.3");
    }

    @Override
    public LagId getMask()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property mask not supported in version 1.3");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_13;
    }

//
        @Override
        public OFOxmBsnLagId build() {
            LagId value = this.valueSet ? this.value : DEFAULT_VALUE;
            if(value == null)
                throw new NullPointerException("Property value must not be null");


            return new OFOxmBsnLagIdVer13(
                    value
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFOxmBsnLagId> {
        @Override
        public OFOxmBsnLagId readFrom(ByteBuf bb) throws OFParseError {
            // fixed value property typeLen == 0x30204L
            int typeLen = bb.readInt();
            if(typeLen != 0x30204)
                throw new OFParseError("Wrong typeLen: Expected=0x30204L(0x30204L), got="+typeLen);
            LagId value = LagId.read4Bytes(bb);

            OFOxmBsnLagIdVer13 oxmBsnLagIdVer13 = new OFOxmBsnLagIdVer13(
                    value
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", oxmBsnLagIdVer13);
            return oxmBsnLagIdVer13;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFOxmBsnLagIdVer13Funnel FUNNEL = new OFOxmBsnLagIdVer13Funnel();
    static class OFOxmBsnLagIdVer13Funnel implements Funnel<OFOxmBsnLagIdVer13> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFOxmBsnLagIdVer13 message, PrimitiveSink sink) {
            // fixed value property typeLen = 0x30204L
            sink.putInt(0x30204);
            message.value.putTo(sink);
        }
    }


    public void writeTo(ByteBuf bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFOxmBsnLagIdVer13> {
        @Override
        public void write(ByteBuf bb, OFOxmBsnLagIdVer13 message) {
            // fixed value property typeLen = 0x30204L
            bb.writeInt(0x30204);
            message.value.write4Bytes(bb);


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFOxmBsnLagIdVer13(");
        b.append("value=").append(value);
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
        OFOxmBsnLagIdVer13 other = (OFOxmBsnLagIdVer13) obj;

        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

}