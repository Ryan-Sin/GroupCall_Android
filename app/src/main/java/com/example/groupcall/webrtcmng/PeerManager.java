package com.example.groupcall.webrtcmng;

import android.app.Activity;
import android.util.Log;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;


public class PeerManager {

    private HashMap<String,PeerClient> peerMap = new HashMap<String,PeerClient>();
    private PeerConnectionFactory peerConnectionFactory = null;
    private Activity context;
    public PeerManager(Activity context){
        this.context = context;
        PeerConnectionFactory.initializeAndroidGlobals(this.context, true);
        peerConnectionFactory = new PeerConnectionFactory(new PeerConnectionFactory.Options());

    }

    public void createLocalPeer(String name, String roomName, SurfaceViewRenderer surfaceView, int streamtype){

        PeerClient peerClient = new PeerClient(this.context,name,roomName,true,surfaceView,streamtype);
        peerMap.put(name,peerClient);

        peerClient.createPeerConnection(peerConnectionFactory);
        peerClient.createPeerOffer();
    }



    public void createRemotePeer(String name, SurfaceViewRenderer surfaceView, int streamtype){

        PeerClient peerClient = new PeerClient(this.context,name,false,surfaceView,streamtype);
        peerMap.put(name,peerClient);
        peerClient.createPeerConnection(peerConnectionFactory);
        peerClient.createPeerOffer();
    }

    public void setPeerRemoteSdp(String name, SessionDescription sdp){
        peerMap.get(name).setRemoteDescription(sdp);
    }

    public void addPeerIceCandidate(String name, IceCandidate iceCandidate){
        peerMap.get(name).addRemoteIceCandidate(iceCandidate);

    }

    public void removePeer(String name){
        PeerClient peerClient = peerMap.get(name);
        peerClient.closePeer();
        peerMap.remove(name);
    }

    public SurfaceViewRenderer getRemoteSurfaceView(String name){
        PeerClient peerClient = peerMap.get(name);

        return peerClient == null ? null :peerClient.getSurfaceView();
    }
}
