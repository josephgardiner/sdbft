package net.floodlightcontroller.comms;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class QuorumNode {
	
	short nodeId;
	short quorumId;
	
    @JsonCreator
	public QuorumNode(@JsonProperty("nodeId")short nodeID, @JsonProperty("quorumId")short quorumID) {
		super();
		this.nodeId = nodeID;
		this.quorumId = quorumID;
	}
	public short getNodeId() {
		return nodeId;
	}
	public void setNodeId(short nodeID) {
		this.nodeId = nodeID;
	}
	public short getQuorumId() {
		return quorumId;
	}
	public void setQuorumId(short quorumID) {
		this.quorumId = quorumID;
	}
	
	
	
	@Override
	public String toString() {
		return "QuorumNode [nodeID=" + nodeId + ", quorumID=" + quorumId + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nodeId;
		result = prime * result + quorumId;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuorumNode other = (QuorumNode) obj;
		if (nodeId != other.nodeId)
			return false;
		if (quorumId != other.quorumId)
			return false;
		return true;
	}
	
	
	
	

}
