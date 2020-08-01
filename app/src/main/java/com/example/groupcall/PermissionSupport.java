package com.example.groupcall;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan
 * @description
 * 해당 클래스는 권한요청 리스트를 가지고 있는 클래스이다.**/

public class PermissionSupport {

    private Context context;
    private Activity activity;

    //요청하고자 하는 클래스를 배열로 담아준다.
    private  String [] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private List permissionList;

    //해당 상수는 권한 요청을 할 때 발생한느 창에 대한 결과 값을 받기 위해 지정해주는 int 형채입니다.
    //본인에 맞게 숫자를 지정해서 사용하면된다.
    private final int MULTIPLE_PERMISSIONS = 1023;

    //클래스 생성자
    public PermissionSupport(Activity _activity, Context _context){
        this.activity = _activity;
        this.context = _context;
    }

    //허용 받아야할 권한이 남아있는지 체크
    public boolean checkPermission(){
        int result;

        permissionList = new ArrayList<>();


        //위에서 배열로 선언한 권한 중 허용되지 않은 권한이 있는지 체크
        for (String pms : permissions){
            result = ContextCompat.checkSelfPermission(context, pms);

            if (result  != PackageManager.PERMISSION_GRANTED){

                //승인된 권한 추가
                permissionList.add(pms);
            }
        }


        if(!permissionList.isEmpty()){

            return false;
        }

        return true;
    }

    //권한 허용 요청
    public void requestPermission(){

        ActivityCompat.requestPermissions(activity, (String[]) permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
    }

}
