package com.example.calug.loginconn;

import android.renderscript.ScriptGroup;
import android.support.annotation.RestrictTo;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TCPClient {

    private static String TAG = "TCPClient";
    private String serverIp = "192.168.0.103";
    private long starTime = 01;
    private int serverPort = 4444;
    private Socket connectionSocket;
    private SendRunnable sendRunnable;
    private Thread sendThread;
    byte[] dataToSend;
    private ReceiveRunnable receiveRunnable;
    private Thread receiveThread;
    boolean receiveThreadRunning = false;



    public class ConnectRunnable implements Runnable {

        public void run() {

            try {
                Log.d(TAG, "C: Connecting...");

                InetAddress serverAddr = InetAddress.getByName(serverIp);

                starTime = System.currentTimeMillis();

                connectionSocket = new Socket();

                connectionSocket.connect(new InetSocketAddress(serverAddr, serverPort), 5000);

                long time = System.currentTimeMillis() - starTime;
                Log.d(TAG, "Connected. Current duration: " + time + " ms");


            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Connection thread stopped");
        }
    }


    public void Connect(String ip, int port) {

        serverIp = ip;
        serverPort = port;
        new Thread(new ConnectRunnable()).start();

    }

    public class SendRunnable implements Runnable {

        byte[] data;
        private OutputStream out;
        private boolean hasMessage = false;
        int dataType = 1;

        public SendRunnable(Socket server){
            try{
                this.out = server.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void Send(byte[] bytes){
            this.data = bytes;
            dataType = TCPCommands.TYPE_FILE_CONTENT;
            this.hasMessage = true;
        }

        public void SendCMD(byte[] bytes){
            this.data = bytes;
            dataType = TCPCommands.TYPE_CMD;
            this.hasMessage = true;

        }

        @Override
        public void run() {
            Log.d(TAG,"Sending started");
            while(!Thread.currentThread().isInterrupted() && isConnected()){
                if(this.hasMessage){
                    starTime = System.currentTimeMillis();
                    try{
                        //send the length
                        this.out.write(ByteBuffer.allocate(4).putInt(data.length).array());
                        //send the type of the data
                        this.out.write(ByteBuffer.allocate(4).putInt(dataType).array());
                        //send the data
                        this.out.write(data,0,data.length);
                        this.out.flush();


                    } catch (IOException e) {
                        this.hasMessage = false;
                        this.data = null;
                        long time = System.currentTimeMillis() - starTime;
                        Log.d(TAG,"Command has been sent! Current duration: "+time+" ms");
                        if(!receiveThreadRunning)
                            startReceiving();
                    }
                }
                Log.d(TAG,"Sending stopped");
            }
        }

    }

    private void startSending(){
        sendRunnable = new SendRunnable(connectionSocket);
        sendThread = new Thread(sendRunnable);
        sendThread.start();
    }

    private void startReceiving() {
        receiveRunnable = new ReceiveRunnable(connectionSocket);
        receiveThread = new Thread(receiveRunnable);
        receiveThread.start();
    }

    public boolean isConnected(){
        return connectionSocket != null && connectionSocket.isConnected() && !connectionSocket.isClosed();
    }

    public void WriteData(byte[] data){
        if(isConnected()){
            startSending();
            sendRunnable.Send(data);
        }
    }

    public void WriteCommand(String cmd){
        if(isConnected()){
            startSending();
            sendRunnable.SendCMD(cmd.getBytes());
        }
    }

    public class ReceiveRunnable implements Runnable {

        private Socket sock;
        private InputStream input;

        public ReceiveRunnable(Socket server){
            sock = server;
            try{
                input = sock.getInputStream();
            }catch (Exception e){

            }
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted() && isConnected())
            {
                if(!receiveThreadRunning){
                    receiveThreadRunning = true;
                }

                try{
                    byte[] data = new byte[4];
                    //read the first integer,it defines the lenth of the data to expect
                    input.read(data,0,data.length);
                    int length = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    //read the second integer, it defines the type of data to expect
                    input.read(data,0,data.length);
                    int type = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();

                    int read = 0;
                    int downloaded = 0;

                    if(type == TCPCommands.TYPE_CMD){
                        //we are expecting a command/message from the server
                        //allocate byte array large enough to containg the data to come
                        data = new byte[length];
                        StringBuilder sb = new StringBuilder();
                        InputStream bis = new BufferedInputStream(input);

                        //read until all data is read or until we have read the expected amount
                        while((read = bis.read(data)) != -1){
                            downloaded += read;
                            sb.append(new String(data,0,read,"UTF-8"));//append data to the stringbuilder
                            if(downloaded == length){
                                break;
                            }
                        }
                    } else if(type == TCPCommands.TYPE_FILE_CONTENT){
                        //we are expecting a file/raw bytes from the server
                        byte[] inputData = new byte[2048];
                        InputStream bis = new BufferedInputStream(input);

                        //read until all data is read or until we have read expected amount
                        while((read = bis.read(inputData)) != -1){
                            //buffer loop
                            downloaded += read;
                            if(downloaded == length){
                                break;
                            }
                        }

                    }

                }catch (Exception e){

                }
            }
            receiveThreadRunning = false;
        }
    }

    private void stopThreads() {
        if(receiveThread != null)
            receiveThread.interrupt();

        if(sendThread != null)
            sendThread.interrupt();
    }

    public void Disconnect() {
        stopThreads();

        try{
            connectionSocket.close();
            Log.d(TAG,"Disconnected");
        }catch (IOException e){

        }
    }


}

 class TCPCommands {

    public static int TYPE_CMD = 1;
    public static int TYPE_FILE_CONTENT = 2;

    public static String CMD_REQUEST_FILES = "server_get_files";
    public static String CMD_REQUEST_FILE_RESPONSE = "server_get_files_response";
    public static String CMD_REQUEST_FILE_DOWNLOAD = "server_download_file";




}




