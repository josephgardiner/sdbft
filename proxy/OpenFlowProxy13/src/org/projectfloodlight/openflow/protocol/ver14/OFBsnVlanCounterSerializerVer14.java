// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template const_serializer.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver14;

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
import org.projectfloodlight.openflow.protocol.OFBsnVlanCounter;
import io.netty.buffer.ByteBuf;
import com.google.common.hash.PrimitiveSink;

public class OFBsnVlanCounterSerializerVer14 {

    public final static byte BSN_VLAN_COUNTER_RX_BYTES_VAL = (byte) 0x0;
    public final static byte BSN_VLAN_COUNTER_RX_PACKETS_VAL = (byte) 0x1;
    public final static byte BSN_VLAN_COUNTER_TX_BYTES_VAL = (byte) 0x2;
    public final static byte BSN_VLAN_COUNTER_TX_PACKETS_VAL = (byte) 0x3;

    public static OFBsnVlanCounter readFrom(ByteBuf bb) throws OFParseError {
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ByteBuf bb, OFBsnVlanCounter e) {
        bb.writeByte(toWireValue(e));
    }

    public static void putTo(OFBsnVlanCounter e, PrimitiveSink sink) {
        sink.putByte(toWireValue(e));
    }

    public static OFBsnVlanCounter ofWireValue(byte val) {
        switch(val) {
            case BSN_VLAN_COUNTER_RX_BYTES_VAL:
                return OFBsnVlanCounter.BSN_VLAN_COUNTER_RX_BYTES;
            case BSN_VLAN_COUNTER_RX_PACKETS_VAL:
                return OFBsnVlanCounter.BSN_VLAN_COUNTER_RX_PACKETS;
            case BSN_VLAN_COUNTER_TX_BYTES_VAL:
                return OFBsnVlanCounter.BSN_VLAN_COUNTER_TX_BYTES;
            case BSN_VLAN_COUNTER_TX_PACKETS_VAL:
                return OFBsnVlanCounter.BSN_VLAN_COUNTER_TX_PACKETS;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OFBsnVlanCounter in version 1.4: " + val);
        }
    }


    public static byte toWireValue(OFBsnVlanCounter e) {
        switch(e) {
            case BSN_VLAN_COUNTER_RX_BYTES:
                return BSN_VLAN_COUNTER_RX_BYTES_VAL;
            case BSN_VLAN_COUNTER_RX_PACKETS:
                return BSN_VLAN_COUNTER_RX_PACKETS_VAL;
            case BSN_VLAN_COUNTER_TX_BYTES:
                return BSN_VLAN_COUNTER_TX_BYTES_VAL;
            case BSN_VLAN_COUNTER_TX_PACKETS:
                return BSN_VLAN_COUNTER_TX_PACKETS_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OFBsnVlanCounter in version 1.4: " + e);
        }
    }

}