package com.example.groupcall.webrtcmng;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.example.groupcall.MainActivity;
import com.example.groupcall.network.StreamingNetwork;
import com.google.gson.JsonObject;

import org.kurento.jsonrpc.JsonUtils;
import org.webrtc.DataChannel;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class PeerClient {

    private int streamtype;
    private Activity context;
    private String name;
    private String roomName;
    private VideoTrack videoTrack;
    private PeerConnection peerConnection;
    private SurfaceViewRenderer surfaceView;
    private SDPObserver sdpObserver = new SDPObserver();
    private PCObserver pcObserver = new PCObserver();
    private boolean isLocalPeer;

    public PeerClient(Activity context, String name, boolean isLocalPeer, SurfaceViewRenderer surfaceView, int streamtype) {
        this.context = context;
        this.name = name;
        this.surfaceView = surfaceView;
        this.isLocalPeer = isLocalPeer;
        this.streamtype = streamtype;
    }

    public PeerClient(Activity context, String name, String roomName, boolean isLocalPeer, SurfaceViewRenderer surfaceView, int streamtype) {
        this.context = context;
        this.name = name;
        this.roomName = roomName;
        this.surfaceView = surfaceView;
        this.isLocalPeer = isLocalPeer;
        this.streamtype = streamtype;
    }


    public void createPeerConnection(PeerConnectionFactory peerConnectionFactory) {
        final List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer iceServer = new PeerConnection.IceServer("stun:stun.l.google.com:19302");
        iceServers.add(iceServer);
        MediaConstraints pcConstraints = new MediaConstraints();
        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, pcConstraints, pcObserver);
        if (isLocalPeer) {
            MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
            mediaStream.addTrack(createVideoTrack(peerConnectionFactory, MainActivity.createVideoCapturer()));
            peerConnection.addStream(mediaStream);
        }
    }

    public void createPeerOffer() {
        MediaConstraints sdpConstraints = new MediaConstraints();
        boolean isReceiveMedia = !isLocalPeer;
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", String.valueOf(isReceiveMedia)));
        peerConnection.createOffer(sdpObserver, sdpConstraints);
    }


    public void setRemoteDescription(SessionDescription sdp) {
        peerConnection.setRemoteDescription(sdpObserver, sdp);
    }

    public void addRemoteIceCandidate(org.webrtc.IceCandidate iceCandidate) {
        peerConnection.addIceCandidate(iceCandidate);
    }


    private class PCObserver implements PeerConnection.Observer {

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidate(org.webrtc.IceCandidate iceCandidate) {

            if (streamtype == WebrtcConfig.STREAM_GROUPCALL) {
                IceCandidate iceCandidate0 = new IceCandidate();
                iceCandidate0.candidate = iceCandidate.sdp;
                iceCandidate0.sdpMid = iceCandidate.sdpMid;
                iceCandidate0.sdpMLineIndex = iceCandidate.sdpMLineIndex;
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", "onIceCandidate");
                jsonObject.add("candidate", JsonUtils.toJsonObject(iceCandidate0));
                jsonObject.addProperty("name", name);
                sendMessage(jsonObject.toString());
            }
//            if (streamtype == WebrtcConfig.STREAM_ONE2MANY) {
//                IceCandidate iceCandidate0 = new IceCandidate();
//                iceCandidate0.candidate = iceCandidate.sdp;
//                iceCandidate0.sdpMid = iceCandidate.sdpMid;
//                iceCandidate0.sdpMLineIndex = iceCandidate.sdpMLineIndex;
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("id", "onIceCandidate");
//                jsonObject.add("candidate", JsonUtils.toJsonObject(iceCandidate0));
//                jsonObject.addProperty("userId", name);
//                sendMessage(jsonObject.toString());
//            }
        }

        @Override
        public void onIceCandidatesRemoved(org.webrtc.IceCandidate[] iceCandidates) {

        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            viewRemoteVideo(mediaStream);

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            System.out.println("onDataChannel()");

        }

        @Override
        public void onRenegotiationNeeded() {

        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

        }
    }

    private class SDPObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(final SessionDescription origSdp) {
            peerConnection.setLocalDescription(sdpObserver, origSdp);
            if (streamtype == WebrtcConfig.STREAM_GROUPCALL) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", "receiveVideoFrom");
                jsonObject.addProperty("sender", name);
                jsonObject.addProperty("sdpOffer", origSdp.description);
                sendMessage(jsonObject.toString());
            }
//            if (streamtype == WebrtcConfig.STREAM_ONE2MANY) {
//                if (isLocalPeer) {
//                    JsonObject jsonObject = new JsonObject();
//
//                    jsonObject.addProperty("id", "presenter");
//                    jsonObject.addProperty("sdpOffer", origSdp.description);
//                    jsonObject.addProperty("userId", name);
//                    jsonObject.addProperty("roomName",roomName);
//
//                    sendMessage(jsonObject.toString());
//                } else {
//                    JsonObject jsonObject = new JsonObject();
//                    jsonObject.addProperty("id", "viewer");
//                    jsonObject.addProperty("sdpOffer", origSdp.description);
//                    jsonObject.addProperty("userId", name);
//                    sendMessage(jsonObject.toString());
//                }
//            }
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(final String error) {
        }

        @Override
        public void onSetFailure(final String error) {
        }
    }

    public void sendMessage(String message) {

        StreamingNetwork.getInstance().sendMessage(message);
    }

    private VideoTrack createVideoTrack(PeerConnectionFactory peerConnectionFactory, VideoCapturer capturer) {
        VideoSource videoSource = peerConnectionFactory.createVideoSource(capturer);
        capturer.startCapture(1000, 720, 30);

        videoTrack = peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
        if (peerConnection != null) {
            videoTrack.setEnabled(true);
            videoTrack.addRenderer(new VideoRenderer(surfaceView));
        }

        return videoTrack;
    }


    public void viewRemoteVideo(final MediaStream mediaStream) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                surfaceView.setVisibility(View.VISIBLE);
                videoTrack = mediaStream.videoTracks.get(0);
                videoTrack.setEnabled(true);
                videoTrack.addRenderer(new VideoRenderer(surfaceView));
            }
        });

    }

    public void closePeer() {
        peerConnection.dispose();
        peerConnection = null;
    }

    public SurfaceViewRenderer getSurfaceView() {
        return surfaceView;
    }


}
