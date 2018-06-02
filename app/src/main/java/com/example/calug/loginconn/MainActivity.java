package com.example.calug.loginconn;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    String ip = "192.168.0.103";
    int port = 4444;
    String msg;
    Socket s;
    String username;
    String password;
    private static String LOG = "log";


    EditText mEtUsername;
    EditText mEtPassword;
    Button mButton;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEtUsername = findViewById(R.id.mEtUsername);
        mEtPassword = findViewById(R.id.mEtPassword);
        mButton = findViewById(R.id.mButton);
        textView = findViewById(R.id.textView);

        mEtUsername.setText("raul");
        mEtPassword.setText("password");

        mButton.setOnClickListener(new View.OnClickListener() {



            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {

                /*
                TCPClient tcp = new TCPClient();
                tcp.Connect(ip,port);
                msg = "string";
                byte[] toSend = msg.getBytes();
                tcp.WriteData(toSend);
                */



                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void ... voids) {


                        //String login = voids[0];

                        try{
                            s = new Socket("192.168.0.102",4444);
                            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                            username = mEtUsername.getText().toString();
                            password = mEtPassword.getText().toString();
                            //DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                            PrintWriter out = new PrintWriter(s.getOutputStream());
                            String toSend = username+" "+password;
                            //out.write(" ");
                            //out.flush();

                            out.write(username+" ");
                            out.flush();

                            out.write(password+" *");

                            out.flush();

                            String receiveUser;
                            String receivePw;
                            BufferedReader inFromServer = new BufferedReader(new InputStreamReader((s.getInputStream())));
                            //String m = "log "+msg+" *";
                            //outToServer.writeBytes(m);

                            receiveUser = inFromServer.readLine();


                            out.close();


                            if(receiveUser.length()>=0){
                                String[] users;
                                users = receiveUser.split(" ");

                                    s.close();
                                    textView.setText(receiveUser);

                            }






                        }catch (Exception e){

                        }finally {
                            try {
                                s.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }



                        return null;
                    }


                }.execute();

            }
        });
    }

}
