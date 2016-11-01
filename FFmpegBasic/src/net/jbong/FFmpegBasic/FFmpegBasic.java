package net.jbong.FFmpegBasic;

import net.jbong.libffmpeg.ExecuteBinaryResponseHandler;
import net.jbong.libffmpeg.FFmpeg;
import net.jbong.libffmpeg.LoadBinaryResponseHandler;
import net.jbong.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import net.jbong.libffmpeg.exceptions.FFmpegNotSupportedException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//public class FFmpegBasic extends Activity implements View.OnClickListener{
public class FFmpegBasic extends Activity {
	private static final String TAG = FFmpegBasic.class.getSimpleName();
	
	FFmpeg ffmpeg;
	//EditText commandEditText;
	LinearLayout outputLayout;
	LinearLayout command_Layout;
	LinearLayout video_Layout;
	//Button runButton;
	private ProgressDialog progressDialog;
	NetWorkTask net;
	
	MoviePlayView playView;
	//1
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        outputLayout = (LinearLayout)findViewById(R.id.LinearLayout);
        video_Layout = (LinearLayout)findViewById(R.id.video_output);
        command_Layout = (LinearLayout)findViewById(R.id.command_output);
        
        try {
            net = new NetWorkTask(this, outputLayout);
            net.execute().get();									// 종료까지 기다림.
        }catch(Exception e){
            e.printStackTrace();
        }
        
        //ffmpeg context 받아오기
        ffmpeg = FFmpeg.getInstance(this);
        loadFFMpegBinary();
        initUI();
        
        // play
        //playView = new MoviePlayView(FFmpegBasic.this);
        //video_Layout.addView(playView);
        
        // decode 명령
        String cmd = "-y -i "+net.File_path + " " + Environment.getExternalStorageDirectory().getAbsolutePath()+"/output.mp4";
    	String[] command = cmd.split(" ");
        if (command.length != 0) {
            execFFmpegBinary(command);
        } else {
            Toast.makeText(FFmpegBasic.this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
        }
        
    }
    
    private void initUI() {
    	//runButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    addTextViewToLayout("FAILED with output : "+s);
                }

                @Override
                public void onSuccess(String s) {
                    addTextViewToLayout("SUCCESS with output : "+s);
                    Log.i(TAG,"SUCCESS with output: "+s);
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg "+command);
                    addTextViewToLayout("progress : "+s);
                    progressDialog.setMessage("Processing\n"+s);
                }

                @Override
                public void onStart() {
                    command_Layout.removeAllViews();

                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg "+command);
                    progressDialog.dismiss();
                    
                    // play
                    playView = new MoviePlayView(FFmpegBasic.this);
                    video_Layout.addView(playView);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void addTextViewToLayout(String text) {
        TextView textView = new TextView(FFmpegBasic.this);
        textView.setText(text);
        command_Layout.addView(textView);
        Log.d("TAG",text);
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(FFmpegBasic.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.device_not_supported))
                .setMessage(getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FFmpegBasic.this.finish();
                    }
                })
                .create()
                .show();

    }
 
}

class MoviePlayView extends View {
    private Bitmap mBitmap;
    Context c;
    public MoviePlayView(Context context) {
        super(context);
        c = context;
        
        if (initBasicPlayer() < 0) {
        	Toast.makeText(context, "CPU doesn't support NEON", Toast.LENGTH_LONG).show();
        	
        	((Activity)context).finish();
        }
        
        //String fname = "/mnt/sdcard/HSTest/T4_MVI_1498.AVI";
        String fname = Environment.getExternalStorageDirectory().toString()+"/BQSquare_416x240_60.bin";
        //String fname = Environment.getExternalStorageDirectory().toString()+"/BQSquare_416x240_60.x265";
        Log.e("",fname);
        
        int openResult = openMovie(fname);
        if (openResult < 0) {
        	Toast.makeText(context, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
        	
        	((Activity)context).finish();
        }
        else
        	mBitmap = Bitmap.createBitmap(getMovieWidth(), getMovieHeight(), Bitmap.Config.RGB_565);
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	renderFrame(mBitmap);
        //canvas.drawBitmap(mBitmap, 0, 0, null);
        
    	int w = mBitmap.getWidth();
    	int h = mBitmap.getHeight();
    	
        Rect src = new Rect(0,0,w,h);
        Rect dst = new Rect(100,200,100+w*2,200+h*2);
        
        canvas.drawBitmap(mBitmap, src, dst, null);
        
        invalidate();
    }
    
    static {
        System.loadLibrary("basicplayer");
    }

    public static native int initBasicPlayer();
	public static native int openMovie(String filePath);
	public static native int renderFrame(Bitmap bitmap);
	public static native int getMovieWidth();
	public static native int getMovieHeight();
	public static native void closeMovie();
}