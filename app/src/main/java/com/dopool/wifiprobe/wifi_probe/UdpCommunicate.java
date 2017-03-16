package com.dopool.wifiprobe.wifi_probe;






import com.dopool.wifiprobe.Constant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public abstract class UdpCommunicate
{
    private static final String tag = UdpCommunicate.class.getSimpleName();

    private byte[] mBuffer = new byte[1024];
    private byte[] mBytes;

    private DatagramSocket mUdpSocket;

    public abstract String getPeerIp();

    public abstract int getPort();

    public abstract byte[] getSendContent();

    protected UdpCommunicate() throws SocketException
    {
        mUdpSocket = new DatagramSocket();
        mUdpSocket.setSoTimeout(Constant.UPD_TIMEOUT);
    }

    /**
     * 将当前的IP发送出去，返回当前IP所占用的对应的mac
     * @throws IOException
     */
    protected void send() throws IOException
    {
        mBytes = getSendContent();
        DatagramPacket dp = new DatagramPacket(mBytes, mBytes.length,
                InetAddress.getByName(getPeerIp()), getPort());
        mUdpSocket.send(dp);
    }


    protected void close()
    {
        if (mUdpSocket != null)
        {
            mUdpSocket.close();
        }
    }

}
