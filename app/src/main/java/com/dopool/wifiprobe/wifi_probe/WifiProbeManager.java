package com.dopool.wifiprobe.wifi_probe;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import com.dopool.wifiprobe.Constant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class WifiProbeManager {

    /**
     * 被扫描的所有ip地址
     */
    private List<String> mScanList = new ArrayList<>();
    private String mLocalIp = "" ;

    private MacListListener mListener ;

    public WifiProbeManager() {

        addAllLocalIp();

    }

    /**
     * 把本地的ip 如 192.168.4.22 后面 192.168.4.1 - 255 所有的IP进行扫描 添加到集合中
     */
    private  void addAllLocalIp() {

        if (mLocalIp.equals(getLocalIp())) return;

        mLocalIp = getLocalIp();

        Log.d("wifi-ip", "WifiProbeManager: " + mLocalIp);
        if (TextUtils.isEmpty(mLocalIp))
            return;

        mScanList.clear();
        String netIp = mLocalIp.substring(0, mLocalIp.lastIndexOf(".") + 1);
        for (int i = 1; i < Constant.COUNT; i++)
        {
            mScanList.add(netIp + i);
        }

        mScanList.remove(mLocalIp);
    }

    /**
     * 开始扫描，发包，并将结果发送出去
     * @param listener
     */
    public void startScan(MacListListener listener){

        addAllLocalIp();//每次进来前 先确定一下wifi的地址

        sendQueryPacket(); // 发包

        mListener = listener ;
        mListener.macList(getConnectedHotMac());


    }

    /**
     * 发送包进行arp操作
     */
    private void sendQueryPacket()
    {
        NetBios netBios = null;
        try
        {
            netBios = new NetBios();

            for (int i = 0; i < mScanList.size(); i++)
            {
                netBios.setIp(mScanList.get(i));
                netBios.setPort(Constant.NETBIOS_PORT);
                netBios.send();

            }
            netBios.close();//这里放里面 下面需要捕获到异常
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 从proc/net/arp中读取ip_mac不需要root ，   如果有root权限 可以通过RE管理器去这个文件夹下查看
     */
    private ArrayList<String> getConnectedHotMac() {
        ArrayList<String> connectedMac = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;

            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String mac = splitted[3];
                    if(mac.matches("..:..:..:..:..:..") && !mac.equals("00:00:00:00:00:00"))
                        connectedMac.add(mac);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connectedMac;
    }

    /**
     * 获取设备的ip地址
     */
    private   String getLocalIp() {
        String localIp = "";

        try {
            Enumeration<NetworkInterface> en
                    = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface networkInterface = en.nextElement();
                Enumeration<InetAddress> inetAddresses
                        = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() &&
                            inetAddress instanceof Inet4Address) {
                        localIp = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return localIp;
    }


    public interface MacListListener{
         void macList(List<String> macList);
    }
}
