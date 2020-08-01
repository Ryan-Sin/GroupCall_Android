package com.example.groupcall.webrtcmng;

public class IceCandidate {
    public String candidate;
    public String sdpMid;
    public int sdpMLineIndex;


    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public int getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(int sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }
    @Override
    public String toString() {
        return "candidate"+ ":" + candidate + ","+"sdpMid"+":"+sdpMid+","+"sdpMLineIndex:"+sdpMLineIndex;
    }
}
