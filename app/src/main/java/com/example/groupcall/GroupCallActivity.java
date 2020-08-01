package com.example.groupcall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupcall.network.StreamingNetwork;
import com.example.groupcall.webrtcmng.PeerManager;
import com.example.groupcall.webrtcmng.WebrtcConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ryan
 * @description : 해당 액티비티는 영상통화 서버와 유저가 실제 연결을 하고 화상통화를 진행하는 액티비티이다.
 * **/
public class GroupCallActivity extends AppCompatActivity {

    private PeerManager peerManager = null;
    private Gson gson = new Gson();

    /**LocalVideoView = 나의 화면
     * remoteVideoView 0, 1, 2 = 상대방 화면**/
    private SurfaceViewRenderer local_VideoView;
    private SurfaceViewRenderer remote_VideoView0;
    private SurfaceViewRenderer remote_VideoView1;
    private SurfaceViewRenderer remote_VideoView2;

    private ConcurrentHashMap<SurfaceViewRenderer, Boolean> peerViewRendersMap = new ConcurrentHashMap<>();
    private ArrayList<SurfaceViewRenderer> peerViewRenderList = new ArrayList<>();
    private ArrayList<String> userList = new ArrayList<String>();


    private int roomNo; // 방 고유번호
    private String roomName; // 방 이름
    private String userName; // 로이인한 유저 이름
    private String IP= "13.125.193.124:8443"; // 서버 아이피 주소


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);

        Intent getIntent = getIntent();
        roomNo = getIntent.getIntExtra("No", 0);
        roomName = getIntent.getStringExtra("room");
        userName = getIntent.getStringExtra("userName");

        //뷰 ID연결.
        initChildView();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
   }

    /*초기 뷰 설정하기 위한 메소드*/
    private void initChildView(){
        initParticipantView();
    }

    /*참여자 화면초기화*/
    private void initParticipantView(){

        /**나의 화면**/
        local_VideoView = findViewById(R.id.groupcall_localView);

        /**상대방 화면**/
        remote_VideoView0 = findViewById(R.id.groupcall_remoteView0);
        remote_VideoView1 = findViewById(R.id.groupcall_remoteView1);
        remote_VideoView2 = findViewById(R.id.groupcall_remoteView2);

        peerViewRenderList.add(local_VideoView);
        peerViewRenderList.add(remote_VideoView0);
        peerViewRenderList.add(remote_VideoView1);
        peerViewRenderList.add(remote_VideoView2);

        peerViewRendersMap.put(local_VideoView,false);
        peerViewRendersMap.put(remote_VideoView0,false);
        peerViewRendersMap.put(remote_VideoView1,false);
        peerViewRendersMap.put(remote_VideoView2,false);

        //스트리밍 서버에 연결하기
        connect_streamingServer();
    }


    //서버 연결 및 영상통화 방 입장.
    private void connect_streamingServer() {

        if (userName.equals("") || userName == null || roomName.equals("") || roomName == null) return;

        StreamingNetwork.getInstance().setMainHandler(mainHandler);
        StreamingNetwork.getInstance().setCertificationPath(getApplicationContext(),"kurento_room_base64.crt");
        StreamingNetwork.getInstance().connectSocket("wss://"+IP+"/groupcall");

        message();
    }

    private void message(){

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "joinRoom");
        jsonObject.addProperty("no", roomNo);
        jsonObject.addProperty("room", roomName);
        jsonObject.addProperty("name", userName);
        StreamingNetwork.getInstance().sendMessage(jsonObject.toString());
    }

    //접속해있던 유저추가
    private void onExistingParticipants(JsonObject jsonMsg) {

        local_VideoView.setMirror(true);
        EglBase rootEglBase = EglBase.create();
        local_VideoView.init(rootEglBase.getEglBaseContext(), null);
        local_VideoView.setZOrderMediaOverlay(true);

        peerViewRendersMap.put(local_VideoView,true);

        PeerConnectionFactory.initializeAndroidGlobals(this, true);
        peerManager = new PeerManager(this);

        peerManager.createLocalPeer(userName,roomName, local_VideoView, WebrtcConfig.STREAM_GROUPCALL);

        JsonArray remotes = jsonMsg.getAsJsonArray("data");

        for(JsonElement element : remotes){
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("name",element);
            onNewParticipantArrived(jsonObject);
        }
    }

    //유저아웃처리
    private void onParticipantLeft(JsonObject jsonMsg){
        String name = jsonMsg.get("name").getAsString();
        SurfaceViewRenderer surfaceViewRenderer = peerManager.getRemoteSurfaceView(name);
        surfaceViewRenderer.setVisibility(View.INVISIBLE);
        surfaceViewRenderer.release();

        peerManager.removePeer(name);
        peerViewRendersMap.put(surfaceViewRenderer, false);


    }

    //새로접속한 유저추가
    private void onNewParticipantArrived(JsonObject jsonMsg) {

        String name = jsonMsg.get("name").getAsString();

        userList.add(name);
        //현재 사용중되지 않는 뷰검사
        int index = 0;
        for (int i = 0; i < peerViewRenderList.size(); i++) {
            if (peerViewRendersMap.get(peerViewRenderList.get(i)) == false) {
                index = i;
                break;
            }
        }

        //뷰리스트에서 유저에게 할당할 뷰 선택
        SurfaceViewRenderer localSurfaceView = peerViewRenderList.get(index);

        //선택된뷰를 사용상태로 설정
        peerViewRendersMap.put(localSurfaceView, true);


        localSurfaceView.setMirror(true);
        EglBase rootEglBase = EglBase.create();
        localSurfaceView.init(rootEglBase.getEglBaseContext(), null);
        localSurfaceView.setZOrderMediaOverlay(true);

        //원격유저추가
        peerManager.createRemotePeer(name,localSurfaceView, WebrtcConfig.STREAM_GROUPCALL);
    }
    //비디오응답처리
    private void onReceiveVideoAnswer(JsonObject jsonMsg) {
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, jsonMsg.get("sdpAnswer").getAsString());
        peerManager.setPeerRemoteSdp(jsonMsg.get("name").getAsString(), sdp);

    }
    //ICE후보추가
    private void onIceCandidate(JsonObject jsonMsg) {
        JsonObject candidate = jsonMsg.get("candidate").getAsJsonObject();
        IceCandidate iceCandidate = new IceCandidate(candidate.get("sdpMid").getAsString(), Integer.parseInt(candidate.get("sdpMLineIndex").getAsString()), candidate.get("candidate").getAsString());
        peerManager.addPeerIceCandidate(jsonMsg.get("name").getAsString(), iceCandidate);

    }

    //UI쓰레드 핸들러
    public Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);

            String msg = (String) message.obj;
            JsonObject jsonMessage = gson.fromJson(msg, JsonObject.class);

            switch (jsonMessage.get("id").getAsString()) {

                //접속해있던 유저추가
                case "existingParticipants":
                    onExistingParticipants(jsonMessage);
                    break;
                //새로접속한 유저추가
                case "newParticipantArrived":
                    onNewParticipantArrived(jsonMessage);
                    break;
                //유저아웃처리
                case "participantLeft":
                    onParticipantLeft(jsonMessage);
                    break;
                //비디오응답처리
                case "receiveVideoAnswer":
                    onReceiveVideoAnswer(jsonMessage);
                    break;
                //ICE후보추가
                case "iceCandidate":
                    onIceCandidate(jsonMessage);
                    break;
                default:
                    break;
            }
        }
    };

}
