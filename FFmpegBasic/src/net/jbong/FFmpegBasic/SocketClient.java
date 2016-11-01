package net.jbong.FFmpegBasic;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Calendar;

/**
 * Created by KANG on 2016-07-28.
 */
/***************************************************************************/
public class SocketClient {

    String SERVERIP;
    int SERVERPORT;
    int BUFSIZE;
    int WIDTH;
    int HEIGHT;

    Socket s;
    BufferedOutputStream outputStream;
    BufferedInputStream inputStream;          // Stream으로 Data Receive

    byte[] buff;

    SocketClient() {
        SERVERIP = "203.249.126.53";
        SERVERPORT = 5901;
        BUFSIZE = 1400;
        WIDTH = 0;
        HEIGHT = 0;

        inputStream = null;
        outputStream = null;

        try {
            s = new Socket(SERVERIP, SERVERPORT);       //socket open
            buff = new byte[1400];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void SockClose() throws IOException {
        outputStream.close();
        inputStream.close();
        s.close();                                      //socket close
    }
}
/***************************************************************************/
class VideoData {
    byte[] byte_seq_num;
    byte[] byte_video_buf;

    int seq_num;

    public VideoData() {
        byte_seq_num = new byte[4];
        byte_video_buf = new byte[1396];

        seq_num = 0;
    }
}
/***************************************************************************/
class Feedback {
    byte[] byte_ID;
    byte[] byte_BW;
    byte[] byte_PLR;

    int ID;
    int BW;
    int PLR;

    public Feedback() {
        byte_ID = new byte[4];
        byte_BW = new byte[4];
        byte_PLR = new byte[4];

        ID = 0;
        BW = 0;
        PLR = 0;
    }
}
/***************************************************************************/
class NetWorkTask extends AsyncTask<Void, Void, Void> {

    Context c;
    View v;

    TextView output;
    SocketClient sock;
    VideoData vd;
    Feedback fb;
    String TotalVideoData;

    int bytesRead;

    Calendar calendar_start, calendar_end;
    long gap_time;
    int bandwidth, PLR;				//packet loss rate
    int PLC;								//packet loss count
    int PC;								//packet count
    
    byte[] b = new byte[1400];

    FileOutputStream out;
    String File_path;

    FFmpegBasic ffmpegbasic;
    
    NetWorkTask(Context c,View v){
        this.c = c;
        this.v = v;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            SockOpen();
            setStream();
            putStream();
            getStream();
            SockClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    // UI update First
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }

    // UI update Second
    @Override
    protected void onPostExecute(Void flag) {

    	// bandwidth + packet loss rate
        output = (TextView)v.findViewById(R.id.bandwidth_PLR);
        String temp = "bandwidth: "+ bandwidth + "\nPacket Loss Rate: "+ PLR+"\n";
        output.setText(temp);
    	System.out.println("video_buf: "+TotalVideoData+"\n");
    	
        super.onPostExecute(flag);
    }

    // Output, Input Stream 설정
    private void setStream() throws IOException {
        sock.outputStream = new BufferedOutputStream(sock.s.getOutputStream());
        sock.inputStream = new BufferedInputStream(sock.s.getInputStream());
    }

    // Stream 보내기
    private void putStream() throws IOException {
        b = "1".getBytes();
        sock.outputStream.write(b);                                     // 1 �쇰븣 binary Data
        sock.outputStream.flush();

    }

    // Stream 받기
    private void getStream() throws IOException, ClassNotFoundException {
    	calendar_start = Calendar.getInstance();
    	// Stream 읽어 쓸 File 열기
        File_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BQSquare_416x24fjjk0_60.bin";
    	//File_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BQSquare_416x240_60.x265";
    	out = new FileOutputStream(File_path);
        
        PLC = 0; PC = 0;
        while ((bytesRead = sock.inputStream.read(sock.buff, 0, 1400)) != -1) {
            // sequence num
            System.arraycopy(sock.buff, 0, vd.byte_seq_num, 0, 4);
            vd.seq_num = bytetoInt(vd.byte_seq_num);
            System.out.println("vd.seq_num : " + vd.seq_num);
            // video buff
            System.arraycopy(sock.buff, 4, vd.byte_video_buf, 0, vd.byte_video_buf.length);
            System.out.println("vd.byte_video_buf : " + vd.byte_video_buf);
            // 파일에 쓰기
            out.write(vd.byte_video_buf);
            if( vd.seq_num == 0 ){
            	PLC++;
            }else if (vd.seq_num == 0 && PLC == 1){
            	TotalVideoData = new String(vd.byte_video_buf);
            }else{
            	TotalVideoData += new String(vd.byte_video_buf);
            }
            PC++;
        }
        System.out.println("getStream close PLC:"+PLC+" PC:"+PC);
        //파일 닫기
        out.close();
        File s_File = new File(File_path);
        
        calendar_end = Calendar.getInstance();
        // Bandwidth와 Packet Loss Rate 구하기
        gap_time = (calendar_end.getTimeInMillis() - calendar_start.getTimeInMillis() ) / 1000;
        bandwidth = (int) (s_File.length() / gap_time / 1024); // kbyte 단위
        PLR = (int)((float)((float)PLC / (float)PC) * 100);
    }

    // Byte -> Int 변환
    int bytetoInt(byte[] src) {
        int[] arr = new int[4];
        for(int i = 0 ; i < 4 ; i++){
            arr[i] = (int)(src[3-i] & 0xFF);
        }
        return ((arr[0] << 24) + (arr[1] << 16) + (arr[2] << 8) + (arr[3] << 0));
    }
    
    

    // Socket 열기
    private void SockOpen() {
        // socket 생성 및, video data 생성
        sock = new SocketClient();
        vd = new VideoData();
    }

    // Socket 닫기
    private void SockClose() throws IOException {
        sock.SockClose();
    }
    
    @Override
    protected void onCancelled() {
    	// TODO Auto-generated method stub
    	super.onCancelled();
    }
}
