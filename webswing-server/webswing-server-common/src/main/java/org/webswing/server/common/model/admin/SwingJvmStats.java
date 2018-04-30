package org.webswing.server.common.model.admin;

import java.io.Serializable;
import java.util.Date;

public class SwingJvmStats implements Serializable {

	private static final long serialVersionUID = -5088492375016821298L;
	private Date snapshotTime;
	private double heapSize;
	private double heapSizeUsed;
	private long inboundDataSizeSum;
	private long inboundMsgCount;
	private long outboundDataSizeSum;
	private long outboundMsgCount;

	public double getHeapSize() {
		return heapSize;
	}

	public void setHeapSize(double heapSize) {
		this.heapSize = heapSize;
	}

	public double getHeapSizeUsed() {
		return heapSizeUsed;
	}

	public void setHeapSizeUsed(double heapSizeUsed) {
		this.heapSizeUsed = heapSizeUsed;
	}

	public Date getSnapshotTime() {
		return snapshotTime;
	}

	public void setSnapshotTime(Date snapshotTime) {
		this.snapshotTime = snapshotTime;
	}

	public long getInboundDataSizeSum() {
		return inboundDataSizeSum;
	}

	public void setInboundDataSizeSum(long inboundDataSizeSum) {
		this.inboundDataSizeSum = inboundDataSizeSum;
	}

	public long getInboundMsgCount() {
		return inboundMsgCount;
	}

	public void setInboundMsgCount(long inboundMsgCount) {
		this.inboundMsgCount = inboundMsgCount;
	}

	public long getOutboundDataSizeSum() {
		return outboundDataSizeSum;
	}

	public void setOutboundDataSizeSum(long outboundDataSizeSum) {
		this.outboundDataSizeSum = outboundDataSizeSum;
	}

	public long getOutboundMsgCount() {
		return outboundMsgCount;
	}

	public void setOutboundMsgCount(long outboundMsgCount) {
		this.outboundMsgCount = outboundMsgCount;
	}

}
