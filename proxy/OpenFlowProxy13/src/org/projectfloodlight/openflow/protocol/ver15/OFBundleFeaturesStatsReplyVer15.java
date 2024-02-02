// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver15;

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
import com.google.common.collect.ImmutableSet;
import java.util.List;
import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

class OFBundleFeaturesStatsReplyVer15 implements OFBundleFeaturesStatsReply {
    private static final Logger logger = LoggerFactory.getLogger(OFBundleFeaturesStatsReplyVer15.class);
    // version: 1.5
    final static byte WIRE_VERSION = 6;
    final static int MINIMUM_LENGTH = 26;

        private final static long DEFAULT_XID = 0x0L;
        private final static Set<OFStatsReplyFlags> DEFAULT_FLAGS = ImmutableSet.<OFStatsReplyFlags>of();
        private final static Set<OFBundleFeatureFlags> DEFAULT_CAPABILITIES = ImmutableSet.<OFBundleFeatureFlags>of();
        private final static List<OFBundleFeaturesProp> DEFAULT_PROPERTIES = ImmutableList.<OFBundleFeaturesProp>of();

    // OF message fields
    private final long xid;
    private final Set<OFStatsReplyFlags> flags;
    private final Set<OFBundleFeatureFlags> capabilities;
    private final List<OFBundleFeaturesProp> properties;
//
    // Immutable default instance
    final static OFBundleFeaturesStatsReplyVer15 DEFAULT = new OFBundleFeaturesStatsReplyVer15(
        DEFAULT_XID, DEFAULT_FLAGS, DEFAULT_CAPABILITIES, DEFAULT_PROPERTIES
    );

    // package private constructor - used by readers, builders, and factory
    OFBundleFeaturesStatsReplyVer15(long xid, Set<OFStatsReplyFlags> flags, Set<OFBundleFeatureFlags> capabilities, List<OFBundleFeaturesProp> properties) {
        if(flags == null) {
            throw new NullPointerException("OFBundleFeaturesStatsReplyVer15: property flags cannot be null");
        }
        if(capabilities == null) {
            throw new NullPointerException("OFBundleFeaturesStatsReplyVer15: property capabilities cannot be null");
        }
        if(properties == null) {
            throw new NullPointerException("OFBundleFeaturesStatsReplyVer15: property properties cannot be null");
        }
        this.xid = xid;
        this.flags = flags;
        this.capabilities = capabilities;
        this.properties = properties;
    }

    // Accessors for OF message fields
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_15;
    }

    @Override
    public OFType getType() {
        return OFType.STATS_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFStatsType getStatsType() {
        return OFStatsType.BUNDLE_FEATURES;
    }

    @Override
    public Set<OFStatsReplyFlags> getFlags() {
        return flags;
    }

    @Override
    public Set<OFBundleFeatureFlags> getCapabilities() {
        return capabilities;
    }

    @Override
    public List<OFBundleFeaturesProp> getProperties() {
        return properties;
    }



    public OFBundleFeaturesStatsReply.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFBundleFeaturesStatsReply.Builder {
        final OFBundleFeaturesStatsReplyVer15 parentMessage;

        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean flagsSet;
        private Set<OFStatsReplyFlags> flags;
        private boolean capabilitiesSet;
        private Set<OFBundleFeatureFlags> capabilities;
        private boolean propertiesSet;
        private List<OFBundleFeaturesProp> properties;

        BuilderWithParent(OFBundleFeaturesStatsReplyVer15 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_15;
    }

    @Override
    public OFType getType() {
        return OFType.STATS_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFBundleFeaturesStatsReply.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public OFStatsType getStatsType() {
        return OFStatsType.BUNDLE_FEATURES;
    }

    @Override
    public Set<OFStatsReplyFlags> getFlags() {
        return flags;
    }

    @Override
    public OFBundleFeaturesStatsReply.Builder setFlags(Set<OFStatsReplyFlags> flags) {
        this.flags = flags;
        this.flagsSet = true;
        return this;
    }
    @Override
    public Set<OFBundleFeatureFlags> getCapabilities() {
        return capabilities;
    }

    @Override
    public OFBundleFeaturesStatsReply.Builder setCapabilities(Set<OFBundleFeatureFlags> capabilities) {
        this.capabilities = capabilities;
        this.capabilitiesSet = true;
        return this;
    }
    @Override
    public List<OFBundleFeaturesProp> getProperties() {
        return properties;
    }

    @Override
    public OFBundleFeaturesStatsReply.Builder setProperties(List<OFBundleFeaturesProp> properties) {
        this.properties = properties;
        this.propertiesSet = true;
        return this;
    }


        @Override
        public OFBundleFeaturesStatsReply build() {
                long xid = this.xidSet ? this.xid : parentMessage.xid;
                Set<OFStatsReplyFlags> flags = this.flagsSet ? this.flags : parentMessage.flags;
                if(flags == null)
                    throw new NullPointerException("Property flags must not be null");
                Set<OFBundleFeatureFlags> capabilities = this.capabilitiesSet ? this.capabilities : parentMessage.capabilities;
                if(capabilities == null)
                    throw new NullPointerException("Property capabilities must not be null");
                List<OFBundleFeaturesProp> properties = this.propertiesSet ? this.properties : parentMessage.properties;
                if(properties == null)
                    throw new NullPointerException("Property properties must not be null");

                //
                return new OFBundleFeaturesStatsReplyVer15(
                    xid,
                    flags,
                    capabilities,
                    properties
                );
        }

    }

    static class Builder implements OFBundleFeaturesStatsReply.Builder {
        // OF message fields
        private boolean xidSet;
        private long xid;
        private boolean flagsSet;
        private Set<OFStatsReplyFlags> flags;
        private boolean capabilitiesSet;
        private Set<OFBundleFeatureFlags> capabilities;
        private boolean propertiesSet;
        private List<OFBundleFeaturesProp> properties;

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_15;
    }

    @Override
    public OFType getType() {
        return OFType.STATS_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OFBundleFeaturesStatsReply.Builder setXid(long xid) {
        this.xid = xid;
        this.xidSet = true;
        return this;
    }
    @Override
    public OFStatsType getStatsType() {
        return OFStatsType.BUNDLE_FEATURES;
    }

    @Override
    public Set<OFStatsReplyFlags> getFlags() {
        return flags;
    }

    @Override
    public OFBundleFeaturesStatsReply.Builder setFlags(Set<OFStatsReplyFlags> flags) {
        this.flags = flags;
        this.flagsSet = true;
        return this;
    }
    @Override
    public Set<OFBundleFeatureFlags> getCapabilities() {
        return capabilities;
    }

    @Override
    public OFBundleFeaturesStatsReply.Builder setCapabilities(Set<OFBundleFeatureFlags> capabilities) {
        this.capabilities = capabilities;
        this.capabilitiesSet = true;
        return this;
    }
    @Override
    public List<OFBundleFeaturesProp> getProperties() {
        return properties;
    }

    @Override
    public OFBundleFeaturesStatsReply.Builder setProperties(List<OFBundleFeaturesProp> properties) {
        this.properties = properties;
        this.propertiesSet = true;
        return this;
    }
//
        @Override
        public OFBundleFeaturesStatsReply build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            Set<OFStatsReplyFlags> flags = this.flagsSet ? this.flags : DEFAULT_FLAGS;
            if(flags == null)
                throw new NullPointerException("Property flags must not be null");
            Set<OFBundleFeatureFlags> capabilities = this.capabilitiesSet ? this.capabilities : DEFAULT_CAPABILITIES;
            if(capabilities == null)
                throw new NullPointerException("Property capabilities must not be null");
            List<OFBundleFeaturesProp> properties = this.propertiesSet ? this.properties : DEFAULT_PROPERTIES;
            if(properties == null)
                throw new NullPointerException("Property properties must not be null");


            return new OFBundleFeaturesStatsReplyVer15(
                    xid,
                    flags,
                    capabilities,
                    properties
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFBundleFeaturesStatsReply> {
        @Override
        public OFBundleFeaturesStatsReply readFrom(ByteBuf bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property version == 6
            byte version = bb.readByte();
            if(version != (byte) 0x6)
                throw new OFParseError("Wrong version: Expected=OFVersion.OF_15(6), got="+version);
            // fixed value property type == 19
            byte type = bb.readByte();
            if(type != (byte) 0x13)
                throw new OFParseError("Wrong type: Expected=OFType.STATS_REPLY(19), got="+type);
            int length = U16.f(bb.readShort());
            if(length < MINIMUM_LENGTH)
                throw new OFParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);
            if(bb.readableBytes() + (bb.readerIndex() - start) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            if(logger.isTraceEnabled())
                logger.trace("readFrom - length={}", length);
            long xid = U32.f(bb.readInt());
            // fixed value property statsType == 19
            short statsType = bb.readShort();
            if(statsType != (short) 0x13)
                throw new OFParseError("Wrong statsType: Expected=OFStatsType.BUNDLE_FEATURES(19), got="+statsType);
            Set<OFStatsReplyFlags> flags = OFStatsReplyFlagsSerializerVer15.readFrom(bb);
            // pad: 4 bytes
            bb.skipBytes(4);
            Set<OFBundleFeatureFlags> capabilities = OFBundleFeatureFlagsSerializerVer15.readFrom(bb);
            // pad: 6 bytes
            bb.skipBytes(6);
            List<OFBundleFeaturesProp> properties = ChannelUtils.readList(bb, length - (bb.readerIndex() - start), OFBundleFeaturesPropVer15.READER);

            OFBundleFeaturesStatsReplyVer15 bundleFeaturesStatsReplyVer15 = new OFBundleFeaturesStatsReplyVer15(
                    xid,
                      flags,
                      capabilities,
                      properties
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", bundleFeaturesStatsReplyVer15);
            return bundleFeaturesStatsReplyVer15;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFBundleFeaturesStatsReplyVer15Funnel FUNNEL = new OFBundleFeaturesStatsReplyVer15Funnel();
    static class OFBundleFeaturesStatsReplyVer15Funnel implements Funnel<OFBundleFeaturesStatsReplyVer15> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFBundleFeaturesStatsReplyVer15 message, PrimitiveSink sink) {
            // fixed value property version = 6
            sink.putByte((byte) 0x6);
            // fixed value property type = 19
            sink.putByte((byte) 0x13);
            // FIXME: skip funnel of length
            sink.putLong(message.xid);
            // fixed value property statsType = 19
            sink.putShort((short) 0x13);
            OFStatsReplyFlagsSerializerVer15.putTo(message.flags, sink);
            // skip pad (4 bytes)
            OFBundleFeatureFlagsSerializerVer15.putTo(message.capabilities, sink);
            // skip pad (6 bytes)
            FunnelUtils.putList(message.properties, sink);
        }
    }


    public void writeTo(ByteBuf bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFBundleFeaturesStatsReplyVer15> {
        @Override
        public void write(ByteBuf bb, OFBundleFeaturesStatsReplyVer15 message) {
            int startIndex = bb.writerIndex();
            // fixed value property version = 6
            bb.writeByte((byte) 0x6);
            // fixed value property type = 19
            bb.writeByte((byte) 0x13);
            // length is length of variable message, will be updated at the end
            int lengthIndex = bb.writerIndex();
            bb.writeShort(U16.t(0));

            bb.writeInt(U32.t(message.xid));
            // fixed value property statsType = 19
            bb.writeShort((short) 0x13);
            OFStatsReplyFlagsSerializerVer15.writeTo(bb, message.flags);
            // pad: 4 bytes
            bb.writeZero(4);
            OFBundleFeatureFlagsSerializerVer15.writeTo(bb, message.capabilities);
            // pad: 6 bytes
            bb.writeZero(6);
            ChannelUtils.writeList(bb, message.properties);

            // update length field
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);

        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFBundleFeaturesStatsReplyVer15(");
        b.append("xid=").append(xid);
        b.append(", ");
        b.append("flags=").append(flags);
        b.append(", ");
        b.append("capabilities=").append(capabilities);
        b.append(", ");
        b.append("properties=").append(properties);
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
        OFBundleFeaturesStatsReplyVer15 other = (OFBundleFeaturesStatsReplyVer15) obj;

        if( xid != other.xid)
            return false;
        if (flags == null) {
            if (other.flags != null)
                return false;
        } else if (!flags.equals(other.flags))
            return false;
        if (capabilities == null) {
            if (other.capabilities != null)
                return false;
        } else if (!capabilities.equals(other.capabilities))
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
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
        OFBundleFeaturesStatsReplyVer15 other = (OFBundleFeaturesStatsReplyVer15) obj;

        // ignore XID
        if (flags == null) {
            if (other.flags != null)
                return false;
        } else if (!flags.equals(other.flags))
            return false;
        if (capabilities == null) {
            if (other.capabilities != null)
                return false;
        } else if (!capabilities.equals(other.capabilities))
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public int hashCodeIgnoreXid() {
        final int prime = 31;
        int result = 1;

        // ignore XID
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

}
