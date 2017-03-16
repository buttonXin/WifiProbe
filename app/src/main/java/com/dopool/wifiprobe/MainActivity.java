package com.dopool.wifiprobe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dopool.wifiprobe.wifi_probe.WifiProbeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ListView mWifi_list;
    //wifi探针
    private int WiFi_time = 30 *1000 ;//30秒扫描一次
    private List<String> mMacList  = new ArrayList<>(); //存放mac的集合
    private ArrayAdapter<String> mAdapter;
    private WifiProbeManager mProbe ;
    private Timer mTimer ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProbe = new WifiProbeManager();

        initView();

        initScan();
    }

    /**
     * 在一定时间内进行扫描（也就是读取文件夹内的mac地址）
     */
    private void initScan() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startScan();
            }
        } , 0 , WiFi_time );
    }

    /**
     * 开始扫描 ， 如果wifi断开了数据就会没有了 ， 如果wifi地址变了内容也会变的
     */
    private void startScan() {
        mProbe.startScan(new WifiProbeManager.MacListListener() {
            @Override
            public void macList(final List<String> macList) {
                //因为在线程中进行扫描的，所以要切换到主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMacList.clear();
                        mMacList.addAll(macList);
                        Log.d("macList", "" + mMacList);
                        mAdapter.notifyDataSetChanged();

                        mWifi_list.setAdapter(mAdapter);

                    }
                });
            }
        });
    }

    private void initView() {
        mWifi_list = (ListView) findViewById(R.id.list_wifi);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMacList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }
}
