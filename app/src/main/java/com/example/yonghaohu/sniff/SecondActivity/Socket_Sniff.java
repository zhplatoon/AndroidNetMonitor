package com.example.yonghaohu.sniff.SecondActivity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.message.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.entity.BasicHttpEntityHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.example.yonghaohu.sniff.R;
import com.example.yonghaohu.sniff.RootTools.RootTools;
import com.example.yonghaohu.sniff.shark.SniffPackets;
import com.example.yonghaohu.sniff.useless.MyFileManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by yonghaohu on 15/12/31.
 */
public class Socket_Sniff extends  Thread{
    private Context mycontext;
    private boolean stop = false;
    private int seconds_duration = 10;
    List<Program>  transfer_list_program;


    public Socket_Sniff(Context context, List<Program> transfer) {
        mycontext = context;
        transfer_list_program = transfer;
        if (RootTools.installBinary(context, R.raw.androidlsof, "androidlsof") == false) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.extraction_error)
                    .setMessage(R.string.extraction_error_msg)
                    .setNeutralButton(R.string.ok, null).show();
        }else {
            Log.d("INstallBinary TCPDump", "SUCCESS");
        }

    }

    @Override
    public void run() {
        stop = false;
        StartAndroidLsof();
//        StartSocketSummary();
    }

    public void SetSecondsDuration(int SecondsDuration) {
        seconds_duration =  SecondsDuration;
    }

    protected void StopProcess() {
       stop = true;
    }

    protected void StartAndroidLsof() {
        while(!stop)
            AndroidLsof();
    }
    protected void StartSocketSummary() {
        while(!stop)
            SocketSummary();
    }

    protected void AndroidLsof() {
        RootCmd("date +%s >> /data/data/com.example.yonghaohu.sniff/files/lsofres;"+
                " /data/data/com.example.yonghaohu.sniff/files/./androidlsof +c 0 -i -F ctPnf 2>&1"
                        +" >> /data/data/com.example.yonghaohu.sniff/" + "files/lsofres");
    }


    protected void SocketSummary() {//List<Program> transfer_list_program

        PollData polldata = new PollData();
        //String apkRoot= getPackageCodePath();
        //AlertDialog.Builder builder2 = new AlertDialog.Builder(mycontext);
        String returnString = "";
        //if(RootCmd(apkRoot) == "false") {
        //    builder2.setMessage("can not root");
        //    builder2.show();
        //}
        //if(RootCmd("/proc/") == "false") {
        //    builder2.setMessage("can not root all");
        //    builder2.show();
        //}

        for (int i = 0; i < transfer_list_program.size(); i++) {
            String path = "/proc/" + transfer_list_program.get(i).getPid() + "/fd";
            String cmd =  "mkdir /sdcard/Android/data/com.example.yonghaohu.sniff";
            if(RootCmd(cmd) == "false") {
                cmd = "echo \"" + cmd + "\n cmd execute false \" " + " >> "+R.string.app_path+ R.string.fdres_file_name;
                RootCmd(cmd);
            }else {
                //   cmd =  "chmod 777 /sdcard/Android/data/com.example.yonghaohu.sniff";
                //   if(RootCmd(cmd) == "false") {
                //       builder2.setMessage("chmod false\n");
                //       builder2.show();
                //   }else{
                //       builder2.setMessage("mkdir and chomod success\n");
                //       builder2.show();
                //   }
            }

            cmd =  "ls -l "+path+" >  /sdcard/Android/data/com.example.yonghaohu.sniff/fdres";

            if(RootCmd(cmd) == "false") {
                cmd = "echo \"" + cmd + "\n cmd execute false \" " + " >> "+R.string.app_path+ R.string.fdres_file_name;
                RootCmd(cmd);
            }else {
                //builder2.show();
                cmd = "cat  /sdcard/Android/data/com.example.yonghaohu.sniff/fdres";
                returnString = RootCmd(cmd);
                if(returnString == "false") {
                    cmd = "echo \"" + cmd + "\n cmd execute false \" " + " >> "+R.string.app_path+ R.string.fdres_file_name;
                    RootCmd(cmd);
                }else {
                    String res_content = new String();
                    ArrayList<Integer> res_of_socket = new ArrayList<Integer>();
                    res_of_socket = polldata.ParseOutput(returnString);
                    res_content += "Applicaction "+transfer_list_program.get(i).getName()+" 's Pid is  ";
                    res_content += transfer_list_program.get(i).getPid()+"\n";
                    if(res_of_socket.size() == 0)
                        res_content = " has no sockets";
                    else {
                        res_content += "Socket infor : \ntcp start: ";
                        res_content += polldata.testNoListeningTcpPorts(res_of_socket);
                        res_content += "\ntcp6 start:";
                        res_content += polldata.testNoListeningTcp6Ports(res_of_socket);
                    }
                    cmd = "echo \"" + res_content + "\" " + " >> /data/data/com.example.yonghaohu.sniff/files/socketres";
                    returnString = RootCmd(cmd);
                }
            }

//            NdkJniUtils jni = new NdkJniUtils();
//            builder2.setMessage("Is file readable :" + scriptfile.canRead()+" finishcall");
//            builder2.show();
            //   String res = jni.getCLanguageString(path);
            //   builder2.setMessage(res);
            //   builder2.show();

//            try {
//                AccessController.checkPermission(new FilePermission(path, "read"));
//                StringBuilder sb = new StringBuilder();
//                sb.append("You have  permition: "+path+"\n"+transfer_list_program.get(i).getName()+"\n");
//                File dir = new File(path);
//                File file[] = dir.listFiles();
//                AlertDialog.Builder builder2 = new AlertDialog.Builder(SecondActivity.this);
//                if(file == null || file.length == 0) {
//                   String cmd =  "netstat -apeen ";// lsof -i -w tcp
//
//                    String returnString = resultExeCmd(cmd);
//                   builder2.setMessage(cmd+"then "+returnString+" end\n");
//                   builder2.show();
//                    String apkRoot= getPackageCodePath();
//                    builder2.setMessage("目前路径 ： "+ apkRoot);
//                    builder2.show();
//                    if(RootCmd(apkRoot) != true) {
//                        builder2.setMessage("can not root");
//                        builder2.show();
//                    }
//                     cmd = "ls -l " + path;
//                     returnString = resultExeCmd(cmd);
//                    if(returnString.length() != 0 || returnString != null) {
//                        builder2.setMessage(cmd+"then "+returnString);
//                        builder2.show();
//                        char first = 'l';//returnString.charAt(0);
//                        if(first != 'l')
//                            returnString = "普通文件";
//                    }else
//                        returnString = "NULL";
//                    sb.append(returnString +"\n");
//                }
//                else {
//                    String res_content = new String();
//                    ArrayList<Integer> res_of_socket = new ArrayList<Integer>();
//                    res_of_socket = polldata.ParsePid(transfer_list_program.get(i).getPid());
//                    if(res_of_socket == null)
//                        res_content = "aaaaaaaaaaaa";
//                    else {
//                        res_content = polldata.testNoListeningTcpPorts(res_of_socket);
//                        res_content += "finish";
//                    }
//                    sb.append(res_content);
//                }
//                builder2.setMessage(sb);
//                builder2.show();
//            } catch (SecurityException e) {
//                StringBuilder sb = new StringBuilder();
//                sb.append("You have no  permition to use : "+path);
//                AlertDialog.Builder builder2 = new AlertDialog.Builder(SecondActivity.this);
//                builder2.setMessage(sb);
//                builder2.show();
//
//            }


        }
    }


    public static String changeCharset(String str, String newCharset)
            throws UnsupportedEncodingException {
        if (str != null) {
            // 用默认字符编码解码字符串。与系统相关，中文windows默认为GB2312
            byte[] bs = str.getBytes();
            return new String(bs, newCharset); // 用新的字符编码生成字符串
        }
        return null;
    }


    public static String resultExeCmd(String cmd) {
        String returnString = "";
        Process pro = null;
        Runtime runTime = Runtime.getRuntime();
        if (runTime == null) {
            System.err.println("Create runtime false!");
        }
        try {
            pro = runTime.exec(cmd);
            BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                returnString = returnString + line + "\n";
            }
            input.close();
            output.close();
            pro.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnString;
    }

    public String RootCmd(String cmd){
        Process process = null;
        DataOutputStream os = null;
        String returnString = cmd+"\noutput is : ";
        try{
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd+"\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return "false";
        } finally {
            try {
                if (os != null)   {
                    os.close();
                }
                //AlertDialog.Builder builder2 = new AlertDialog.Builder(mycontext);
                process = Runtime.getRuntime().exec(cmd);
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                PrintWriter output = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    returnString = returnString + line + "\n";
                }
                //builder2.setMessage(returnString);
                //builder2.show();
                input.close();
                output.close();

                process.destroy();
            } catch (Exception e) {
            }
        }
        return returnString;
    }

    public void writeFileSdcardFile(String fileName,String write_str){
        try{
            FileOutputStream fout = new FileOutputStream(fileName);
            byte [] bytes = write_str.getBytes();

            fout.write(bytes);
            fout.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
