package com.example.groupcall.network;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author Ryan
 * @description
 *  해당 클래스는 영상통화 서버와 연결을 관리하는 클래스이다.
 * **/
public class StreamingNetwork {

    //송신메시지 구분
    private final int MESSAGE_SOCKET_OPEN = 1;
    private final int MESSAGE_SOCKET_MESG = 2;

    //SSL인증서 입력스트림
    private InputStream certificationStream;

    //수신메시지처리 핸들러
    private Handler mainHandler;

    //송신메시지처리 핸들러
    private Handler backHandler;

    //송신메시지처리 핸들러스레드
    private HandlerThread backThread;
    private WebSocket webSocket;
    private String connUrl = "";

    private static StreamingNetwork streamingNetwork = new StreamingNetwork();

    private StreamingNetwork() {
        backThread = new HandlerThread("NETWORK HANDLER");
        backThread.start();

        initBackgroundHandler();

    }
    private void initBackgroundHandler(){
        backHandler = new Handler(backThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case MESSAGE_SOCKET_OPEN:
                        try {

//                            if(webSocket != null)return;

                            if(msg.obj.toString().equals(connUrl))return;
                            connUrl = msg.obj.toString();

                            webSocket = new WebSocketFactory()
                                    .setSSLContext(createSSLContext())
                                    .setVerifyHostname(false)
                                    .createSocket(connUrl)
                                    .addListener(new WebSocketAdapter() {
                                        @Override
                                        public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                                            super.onBinaryMessage(websocket, binary);
                                        }

                                        @Override
                                        public void onTextMessage(WebSocket websocket, String text) throws Exception {
                                            super.onTextMessage(websocket, text);
                                            Message message = Message.obtain(mainHandler);
                                            message.obj = text;
                                            mainHandler.sendMessage(message);

                                        }
                                    }).connect();
                        } catch (Exception e) {
                           e.printStackTrace();
                        }
                        break;
                    case MESSAGE_SOCKET_MESG:
                        webSocket.sendText(msg.obj.toString()+"");
                        break;
                }
            }
        };

    }

    public static StreamingNetwork getInstance(){
        return streamingNetwork;
    }


    //핸들러 세팅
    public void setMainHandler(Handler mainHandler){
        this.mainHandler = mainHandler;

    }

    //서버인증서 세팅
    public void setCertificationPath(Context context, String path){
        try {
            certificationStream = context.getAssets().open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //SSL인증서 생성
    private SSLContext createSSLContext() {
        SSLContext sslContext = null;
        try {

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            CertificateFactory cf;

            cf = CertificateFactory.getInstance("X.509");

            InputStream caInput = new BufferedInputStream(certificationStream);
            Certificate ca = cf.generateCertificate(caInput);
            keyStore.setCertificateEntry("ca", ca);

            sslContext = SSLContext.getInstance("TLS");

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            sslContext.init(null, tmf.getTrustManagers(), null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return sslContext;
        }
    }

    //서버 소켓 연결
    public void connectSocket(String url) {
        Message msg = Message.obtain(backHandler);
        msg.what = MESSAGE_SOCKET_OPEN;
        msg.obj = url;


        backHandler.sendMessage(msg);
    }

    //서버에 메시지 보내기
    public void sendMessage(String message) {
        Message msg = Message.obtain(backHandler);
        msg.obj = message;
        msg.what = MESSAGE_SOCKET_MESG;

        backHandler.sendMessage(msg);

    }

    //서버와 연결 끊기
    public void disconnectServer(){
        if(webSocket != null) {
            webSocket.disconnect();
            webSocket.sendClose();
            webSocket = null;
        }
    }
}
