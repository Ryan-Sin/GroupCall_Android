package com.example.groupcall;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupcall.network.NetworkStatus;

import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.VideoCapturer;

/**
 * @author Ryan
 * @description : 해당 프로젝트는 Kurento-Group-Call 안드로이드 클라이언트이다.
 * 최대 4명과 영상통화가 가능하며, 방을 만들거나 방을 찾아서 입장하는 형식으로 구현을 해놨다.
 *
 * 해당 화면은 사용자가 다중 영상통화를 하기 전 유저 이름을 만들고, 영상통화 방을 만들거나, 영상통화 방을 입장할 수 있는 화면이다.**/
public class MainActivity extends AppCompatActivity {


    TextView name_Tv, content_Tv;
    Context context = this;

    //유저 이름
    String userName;

    //네트워크 상태 체크
    int networkStatus;

    //권한 체크 클래스
    private PermissionSupport permission;

    //권한 체크
    private void permissionCheck(){

        // SDK 23버전 이하 버전에서는 Permission(권한) 필요하지 않는다.
        if (Build.VERSION.SDK_INT >= 23){

            //권한 체크 클래스 객체 생성
            permission = new PermissionSupport(this, this);

            //권한 체크 후 결과 값이 false로 들어온다면
            if (!permission.checkPermission()){

                //권한을 요청해준다.
                permission.requestPermission();
            }
        }
    }


    //Request Permission에 대한 결과 값을 받아올 수 있다.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == 1023 && (grantResults.length == permissions.length)){

            Log.e( "permissionResult: ","반복문" );
            /**grantResults 값이 0 이면 사용자거 허용, / -1이면 거부한 것이다.
             * -1이 있는지 체크하여 하나라도 -1이 나온다면 false 를 return 해준다. **/
            for (int i =0; i< grantResults.length; i++){


                if (grantResults[i] == -1){
                    Log.e("실행?","!!!!!1111" + grantResults[i]);
                    Toast.makeText(context,"영상통화를 사용하기 위해 마이크 권한이 필요합니다.2",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkStatus = NetworkStatus.getConnectivityStatus(getApplicationContext());

        permissionCheck();

        /**
         * APP 소개글 및 유저 이름 등록
         * **/
        content_Tv = findViewById(R.id.content);
        content_Tv.setText("APP 제목 : 다중영상 통화 \n\n" +
                "APP 설명서 \n\n " +
                "1. 방 입장 - 검색 아이콘을 클릭 해당 방 \n 이름을 입력하고 확인 버튼을 누르면\n 입장이 가능하다.\n\n" +
                "2. 방 생성 - 전화 아이콘을 클릭해 방에\n 이름을 입력하고 확인 버튼을 누른다.");


        name_Tv = findViewById(R.id.userName);
        name_Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserName();
            }
        });
    }

    EditText userName_Edit;
    private void addUserName(){
        AlertDialog.Builder addUserDialog = new AlertDialog.Builder(this);
        addUserDialog.setTitle("유저 이름을 입력해주세요.");

        userName_Edit = new EditText(this);
        userName_Edit.setHint("유저 이름");

        addUserDialog.setView(userName_Edit);
        addUserDialog.setPositiveButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                name_Tv.setText("클릭 후 유저 이름을 생성하세요.");
            }
        });

        addUserDialog.setNegativeButton("확인",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                userName = userName_Edit.getText().toString();

                if (userName.equals("")) {
                    Toast.makeText(context, "유저 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                name_Tv.setText(userName);
            }
        });
        addUserDialog.show();
    }

    private static VideoCapturer videoCapturer;

    /**VideoCapturer 클레스는 카메라나 비디오파일에서 캡처를 하는 클레스 **/
    public static VideoCapturer createVideoCapturer() {
        videoCapturer = createCameraGrabber(new Camera1Enumerator(false));
        return videoCapturer;
    }

    /**사용할수있는 카메라 DEVICE 검색**/
    private static VideoCapturer createCameraGrabber(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        for (int i=0; i<deviceNames.length; i++){
            Log.e("사용할수있는 카메라", deviceNames[i]+"");
        }

        /**사용 가능한 카메라를 찾는다.**/
        for (String deviceName : deviceNames) {

            /**후면 카메라를 찾는다.**/
            if (enumerator.isFrontFacing(deviceName)) {
                Log.e("enumerator", "isFrontFacing");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
            /**후면 카메라를 찾을 수 없다면 다른 카메라를 찾는다.**/
            else if (!enumerator.isFrontFacing(deviceName)) {
                Log.e("!enumerator", "!!");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }


    /**액션바 아이콘 연결**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sample,menu);
        return true;
    }

    Intent intent;
    EditText roomName_Edit;
    /**액션바 아이콘 클릭 이벤트**/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.search:

                AlertDialog.Builder searchDialog = new AlertDialog.Builder(this);
                searchDialog.setTitle("방 찾기");
                searchDialog.setMessage("방 이름을 입력하고 확인 버튼을 눌러주세요.");

                roomName_Edit = new EditText(this);
                roomName_Edit.setHint("방 이름");

                searchDialog.setView(roomName_Edit);
                searchDialog.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                searchDialog.setNegativeButton("확인",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String roomName = roomName_Edit.getText().toString();

                        if (roomName.equals("") || roomName == null || userName == null) {
                            Toast.makeText(context, "유저 이름 또는 방 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (networkStatus == NetworkStatus.TYPE_NOT_CONNECTED){
                            Toast.makeText(context, "인터넷 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                        }


                        /**방 이름과 유저 이름을 화상통화 액티비티로 보낸다.**/
                        intent = new Intent(getApplicationContext(), GroupCallActivity.class);
                        intent.putExtra("No", 0);
                        intent.putExtra("room", roomName);
                        intent.putExtra("userName", userName);
                        startActivity(intent);
                    }
                });

                searchDialog.show();
                return true;

            case R.id.call:

                AlertDialog.Builder callDialog = new AlertDialog.Builder(this);

                callDialog.setTitle("방 등록");
                callDialog.setMessage("방을 생성해주세요.");


                final EditText callName = new EditText(this);
                callDialog.setView(callName);

                callDialog.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {


                    }
                });


                callDialog.setNegativeButton("확인",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String roomName = callName.getText().toString();

                        if (roomName.equals("") || roomName == null || userName == null) {
                            Toast.makeText(context, "유저 이름 또는 방 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (networkStatus == NetworkStatus.TYPE_NOT_CONNECTED){
                            Toast.makeText(context, "인터넷 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        /**방 이름과 유저 이름을 화상통화 액티비티로 보낸다.**/
                        intent = new Intent(getApplicationContext(), GroupCallActivity.class);
                        intent.putExtra("No", 0);
                        intent.putExtra("room", roomName);
                        intent.putExtra("userName", userName);
                        startActivity(intent);
                    }
                });

                callDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
