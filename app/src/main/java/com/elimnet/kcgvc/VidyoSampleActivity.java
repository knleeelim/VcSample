/**
 * KCG_VC android app
 * author Kyung Neung Lee
 * 2019-04-29
 */
package com.elimnet.kcgvc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.hardware.Camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.elimnet.kcgvc.appSampleHttp.Arguments;
import com.vidyo.LmiDeviceManager.*;

import android.hardware.SensorManager;
import android.widget.Toast;

import net.secuwiz.SecuwaySSLU.service.IMobileApi;

import org.w3c.dom.Text;

public class VidyoSampleActivity extends Activity implements 
	LmiDeviceManagerView.Callback,  
	SensorEventListener,
	View.OnClickListener {

	private static final String TAG = "VidyoSampleActivity";

	private boolean doRender = false;
	
	private LmiDeviceManagerView bcView; // new 2.2.2
	private boolean bcCamera_started = false;
	private static boolean loginStatus = false;
	private static boolean roomlinkStatus = false;
	private boolean cameraPaused = false;
	private boolean cameraStarted = false;
	public static final int CALL_ENDED = 0;
	public static final int MSG_BOX = 1;
	public static final int CALL_RECEIVED = 2;
	public static final int CALL_STARTED = 3;
	public static final int SWITCH_CAMERA = 4;
	public static final int LOGIN_SUCCESSFUL = 5;
	public static final int LIBRARY_STARTED = 6;
	final float degreePerRadian = (float) (180.0f / Math.PI);
	final int ORIENTATION_UP = 0;
	final int ORIENTATION_DOWN = 1;
	final int ORIENTATION_LEFT = 2;
	final int ORIENTATION_RIGHT = 3;
	private float[] mGData = new float[3];
	private float[] mMData = new float[3];
	private float[] mR = new float[16];
	private float[] mI = new float[16];
	private float[] mOrientation = new float[3];

	final int DIALOG_LOGIN = 0;
	final int DIALOG_JOIN_CONF =3;
	final int DIALOG_MSG = 1;
	final int DIALOG_CALL_RECEIVED = 2;
	final int FINISH_MSG = 4;
	final int DIALOG_ROOMLINK = 5;
	final int DIALOG_AUTO = 6;

	VidyoSampleApplication app;
	Handler message_handler;
	StringBuffer message;
	private int currentOrientation;
	private SensorManager sensorManager;
	StringBuffer serverString;
	StringBuffer usernameString;
	StringBuffer passwordString;
	public static boolean isHttps = false;
	String portaAddString;
	String guestNameString;
	String roomKeyString;
	int usedCamera = 1;
	boolean camOn = true;
	boolean speakerOn = true;
	boolean micOn = true;
	boolean speakerphoneOn = true;
	String dialogMessage;
	String i_id = null;
	String i_name = null;
	boolean firstConf = true;
	String last_roomkey = "";
	boolean callinmiddle = false;
	boolean isConnecting = true;
	boolean noPhone = false;
	String mPhoneNumber = null;

	private boolean mIsOnPause = false;
	private ImageView cameraView;
    AudioManager am;
	SharedPreferences loginInfo;
	SharedPreferences.Editor loginInfoEditor;

	private boolean zoommode = true;

	private String getAndroidSDcardMemDir() throws IOException{
	    File sdCard = Environment.getExternalStorageDirectory();
	    File dir = new File (sdCard.getAbsolutePath() + "/VidyoMobile");
	    dir.mkdirs();
	   
	    String sdDir = dir.toString() + "/";
	    return sdDir;
	}

	private String getAndroidInternalMemDir() throws IOException {
		File fileDir = getFilesDir(); //crashing
		if (fileDir != null) {
			String filedir=fileDir.toString() + "/";
			Log.d(TAG, "file directory = " + filedir);
			return filedir;
		} else {
			Log.e(TAG, "Something went wrong, filesDir is null");
		}
		return null;
	}

	private String writeCaCertificates() {
		try {
			InputStream caCertStream = getResources().openRawResource(R.raw.ca_certificates);
//			File caCertFileName;
//			caCertFileName = getFileStreamPath("ca-certificates.crt");

			File caCertDirectory;
			try {
				String pathDir = getAndroidInternalMemDir();
				caCertDirectory = new File(pathDir);
			} catch (Exception e) {
				caCertDirectory = getDir("marina",0);
			}
 			File cafile= new File(caCertDirectory,"ca-certificates.crt");
			
			FileOutputStream caCertFile = new FileOutputStream(cafile);
			byte buf[] = new byte[1024];
        	int len;
        	while ((len = caCertStream.read(buf)) != -1) {
        		caCertFile.write(buf, 0, len);
        	}
        	caCertStream.close();
        	caCertFile.close();
        	
        	return cafile.getPath();
		}
		catch (Exception e) {
			return null;
		}
	}

    IBinder tempService = null;
	private ServiceConnection mConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //서비스 바인더  멤버변수로 저장
			Log.d(TAG, "!!!!!EVERBEENCALLED");
            tempService = service;
			if (checkStatus() == 0){
				Log.d(TAG, "!!!!!something going on here");
				StartRunnable a = new StartRunnable();
				Thread startThread = new Thread(a);
				startThread.start();
			}
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "!!!!!NOW DISCONNECTED");
			tempService = null;
        }
    };
	class StartRunnable implements Runnable {
		public void run()
		{
			isConnecting = true;
			firstConf = true;
			String strResult = "error";

			//bindService 가 정상적으로 되고 onServiceConnected() 까지 호출된 다음 실행
			//서비스에 연결 되었으면 vpn 로그인
			if(tempService != null)
			{
				IMobileApi objAidl = IMobileApi.Stub.asInterface(tempService);

				try {
					//아이디,비밀번호를 이용하여 vpn 시작

					if(mPhoneNumber != null){
						String mPhonePassword = mPhoneNumber.substring(3)+"K!";
						//String mPhonePassword = "K!";
						Log.d(TAG, "phone no: "+mPhoneNumber);
						Log.d(TAG, mPhonePassword);
						strResult = objAidl.StartVpn("https://210.103.52.246", mPhoneNumber, mPhonePassword);
					}else{
						noPhone = true;
						Log.d(TAG, "no phone number");
						strResult = objAidl.StartVpn("https://210.103.52.246", "admin2018", "test001!@");

					}
					//strResult = objAidl.StartVpn("https://210.103.52.246", "01098128187", "98128187K!");
					if(strResult !=null && strResult.equals("0"))
					{
						//연결성공
						Log.d(TAG, "!!!!!vpn startvpn");
						isConnecting = false;
					}
					else
					{
						Log.d(TAG, "!!!!!vpn startvpn error: " + strResult);
						isConnecting = false;
					}
				} catch (RemoteException e) {
					Log.d(TAG, "!!!!!ERROR: "+ e);
				}
			}
		}
	}

	class StartAsync extends AsyncTask<String, Void, String> {
		TextView tv = null;
		public StartAsync(TextView v){
			this.tv = v;
			}
		@Override
		public String doInBackground(String... a)
		{
			isConnecting = true;
			firstConf = true;
			String strResult = "error";

			//bindService 가 정상적으로 되고 onServiceConnected() 까지 호출된 다음 실행
			//서비스에 연결 되었으면 vpn 로그인
			if(tempService != null)
			{
				IMobileApi objAidl = IMobileApi.Stub.asInterface(tempService);

				try {
					//아이디,비밀번호를 이용하여 vpn 시작

					if(mPhoneNumber != null){
						String mPhonePassword = mPhoneNumber.substring(3)+"K!";
						//String mPhonePassword = "K!";
						Log.d(TAG, mPhoneNumber);
						Log.d(TAG, mPhonePassword);
						strResult = objAidl.StartVpn("https://210.103.52.246", mPhoneNumber, mPhonePassword);
						//strResult = objAidl.StartVpn("https://210.103.52.246", "mtest01", "winitech0)");
					}else{
						//strResult = objAidl.StartVpn("https://210.103.52.246", "mtest01", "winitech0)");
						strResult = objAidl.StartVpn("https://210.103.52.246", "admin2018", "test001!@");
						return "전화번호 없음";

					}
					if(strResult !=null && strResult.equals("0"))
					{
						//연결성공

						Log.d(TAG, "!!!!!vpn startvpn");
						isConnecting = false;
						return "VPN 접속성공";

					}
					else
					{
						Log.d(TAG, "!!!!!vpn startvpn error: " + strResult);
						isConnecting = false;

					}
				} catch (RemoteException e) {
				}
			}
			return "다시 시도 하세요";
		}
		@Override
		public void onPostExecute(String str){
			if(checkStatus() == 2){
				Log.d(TAG, "!!!!!vpn VPN 접속중, 잠시 기다린후 버튼을 누르세요");
				tv.setText("VPN 접속중, 잠시 기다린후 버튼을 누르세요");
			}else if(checkStatus() == 1){
				Log.d(TAG, "!!!!!vpn VPN 접속성공, 로그인 하세요");
				tv.setText("VPN 접속성공, 로그인 하세요");
			}else if (checkStatus() == 0){
				Log.d(TAG, "!!!!!vpn");
				tv.setText(str);
			}
		}
	}

	class StopRunnable implements Runnable {
		public void run()
		{
			//bindService 가 정상적으로 되고 onServiceConnected() 까지 호출된 다음 실행
			//서비스에 연결 되었으면 vpn 로그아웃
			if(tempService != null)
			{
				IMobileApi objAidl = IMobileApi.Stub.asInterface(tempService);

				try {

					objAidl.StopVpn();
				} catch (RemoteException e) {
				}
			}
		}
	}


	void vpn(){
		Intent intent = new Intent().setAction("net.secuwiz.SecuwaySSLU.service");
		intent.setPackage("net.secuwiz.SecuwaySSLU.service");
		if(bindService(intent, mConnection,BIND_AUTO_CREATE) == true)
		{
			Log.d(TAG, "!!!!!vpn bindservice success");
		}else{
			dialogMessage = new String("Network Unavailable!\n" + "Check network connection.");
			showDialog(FINISH_MSG);
			//System.exit(1);
		}
	}


	public int checkStatus(){
		//bindService 가 정상적으로 되고 onServiceConnected() 까지 호출된 다음 실행
		//서비스에 연결 되었으면 vpn 상태 체크
		int nStatus = 100;
		if(tempService != null)
		{
			IMobileApi objAidl = IMobileApi.Stub.asInterface(tempService);

			try {
				nStatus = objAidl.VpnStatus();
			} catch (RemoteException e) {
			}
		}

		//nStatus 0 이면 연결 안됨
		//nStatus 1 이면 연결됨
		//nStatus 2 면 연결 중
		//vpn 상태는 0 에서 시작해서 2를 반복하다가 1로 변하면 연결이 된 상태임
		Log.d(TAG, "!!!!!vpn 상태: "+nStatus);
		return nStatus;
	}

  @Override
  public void onCreate(Bundle savedInstanceState) {
	  //for mobile office to start this app it would need to get package name of this app such as com.vidyo.vidyosample
	  //To get data from Mobile Office app
	  TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	  mPhoneNumber = tMgr.getLine1Number();
	  if(mPhoneNumber != null){
		  if(mPhoneNumber.startsWith("+82")){
			  mPhoneNumber = mPhoneNumber.replace("+82", "0");
		  }
	  }

	  am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	  am.setMode(AudioManager.MODE_NORMAL);
	  am.setSpeakerphoneOn(true);

	  Intent mintent = getIntent();
	  i_id = mintent.getStringExtra("id");
	  i_name = mintent.getStringExtra("name");
	  Log.d(TAG, i_id+i_name);

	  isConnecting = true;
	  vpn();
	  Log.d(TAG, "entering onCreate");


	  /*
	  File fileDir = getFilesDir(); //crashing
	  String filedir=fileDir.toString() + "/";
	  //File cacheDir = getCacheDir();
	  //String cachedir=cacheDir.toString() + "/";

	  DeleteDirectory(filedir);
	  //DeleteDirectory(cachedir);*/
	  super.onCreate(savedInstanceState);
	  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	  this.requestWindowFeature(Window.FEATURE_NO_TITLE); // disable title bar for dialog
	  setContentView(R.layout.main);
	  message_handler = new Handler() {
			public void handleMessage(Message msg) {
				Bundle b = msg.getData();
				switch (msg.what) {
					case LIBRARY_STARTED:
						app.DisableAutoLogin();
						break;

					case CALL_STARTED:
						firstConf = false;
						callinmiddle = false;
						double density = getResources().getDisplayMetrics().density;
						app.SetPixelDensity(density);
						app.StartConferenceMedia();
						app.SetPreviewModeON(camOn);
						app.SetCameraDevice(usedCamera);
						app.DisableShareEvents();
						startDevices();
						app.HideToolBar(true);
						break;

					case CALL_ENDED:
						if(firstConf){
							app.JoinRoomLink("http://10.29.16.206",
									last_roomkey,
									i_name,
									"",!camOn, !micOn, !speakerOn);
							firstConf = false;
						}else{
							stopDevices();
							app.RenderRelease();
							callinmiddle = false;
							showDialog(DIALOG_ROOMLINK);
						}
						break;

					case MSG_BOX:
						message = new StringBuffer(b.getString("text"));
						showDialog(DIALOG_MSG);
						break;

					case SWITCH_CAMERA:
						String whichCamera = (String)(msg.obj);
						boolean isFrontCam = whichCamera.equals("FrontCamera");
						Log.d(VidyoSampleApplication.TAG, "Got camera switch = " + whichCamera);

						// switch to the next camera, force settings are per device.
						// sample does not get this values
						//	bcCamera.switchCamera(isFrontCam, false, 0, false, false);
						break;

					case LOGIN_SUCCESSFUL:
						if(!roomlinkStatus) {
							app.SignOut();
							roomlinkStatus = true;
							showDialog(DIALOG_ROOMLINK);
						}
						break;
				}
			}
	  };
//	  app = new VidyoSampleApplication(message_handler);
	  app = (VidyoSampleApplication) getApplication();
	  app.setHandler(message_handler);

	  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);// get the full screen size from android
	  setContentView(R.layout.conference);

	  // Use a layout change listener to re-apply the background image if
	  // orientation changes.
	  final View background = findViewById(R.id.RelativeLayout01);
	  background.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
	  {
		  @Override
		  public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
									 int oldTop, int oldRight, int oldBottom) {
			  int orientation = getResources().getConfiguration().orientation;
			  if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
				  background.setBackgroundResource(R.drawable.background_splash_land);

			  } else {
				  background.setBackgroundResource (R.drawable.background_splash);
			  }
		  }
	  });
	  bcView = new LmiDeviceManagerView(this,this);

	  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	  NetworkInfo netInfo = cm.getActiveNetworkInfo();
	  String caFileName = writeCaCertificates();
	  setupAudio(); // will set the audio to high volume level

	  currentOrientation = -1;

	  sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	  Sensor gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	  Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	  sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_FASTEST);
	  sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
	  String uniqueId = Settings.Secure.getString(this.getContentResolver(),
			  Settings.Secure.ANDROID_ID);
        
	  if (netInfo == null || !netInfo.isConnected()) {
		  dialogMessage = new String("Network Unavailable!\n" + "Check network connection.");
		  showDialog(FINISH_MSG);
		  //app = null;
		  return;
	  } else if (app.initialize(caFileName, uniqueId, this) == false) {
		  dialogMessage = new String("Initialization Failed!\n" + "Check network connection.");
		  showDialog(FINISH_MSG);
		  //app = null;
		  return;
	  } else if(noPhone){
		  Log.d(TAG, "no phone deactivate");
		  dialogMessage = new String("전화번호 미등록!\n" + "앱 실행이 불가합니다.");
		  showDialog(FINISH_MSG);
		  return;
	  }

	  if(!loginStatus) {
		  if(i_id != null && i_name != null){
			  showDialog(DIALOG_AUTO);
		  }else{
			  showDialog(DIALOG_LOGIN);//Logged in for the first time, it will call onCreateDialog passing DIALOG_LOGIN
		  }
		  loginStatus = true;
		  app.HideToolBar(false);
		  app.SetEchoCancellation(true);
	  }
	  Log.d(TAG, "leaving onCreate");
  }

  private void setupAudio() {
		int set_Volume = 65535;
		app.SetSpeakerVolume(set_Volume);
  }

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause Begin");
		isConnecting = false;
		LmiVideoCapturer.onActivityPause();
		mIsOnPause = true;
		pauseCall();
		if (cameraStarted) {
			cameraPaused = true;
			cameraStarted = false;
		} else {
			cameraPaused = false;
		}
		app.DisableAllVideoStreams();
		loginStatus = false;//trial
		app.SignOut();
		if(callinmiddle){
			//app.Leave();
			//stopDevices();
			//app.RenderRelease();
			finish();

		}
		//app.LogOut();
		if (this.isFinishing()) {

		}
		/*if(checkStatus() == 1){
			Log.d(TAG, "!!!!!Stop VPN");
			StopRunnable b = new StopRunnable();
			Thread stopThread = new Thread(b);
			stopThread.start();
		}*/
		Log.d(TAG, "onPause End");
		app.EnableAllVideoStreams();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy Begin");
		stopDevices();
		//finish();
		//app.SignOut();
		app.uninitialize();
		Log.d(TAG, "!!!!!No Way");
		unbindService(mConnection);

	}

	@Override
	public void onResume() {
		super.onResume();
		mIsOnPause = false;
		Log.d(TAG, "onResume Begin");
		resumeCall();
		app.EnableAllVideoStreams();

		Log.d(TAG, "onResume End");

	}
	@Override
	public void onStart(){
		super.onStart();
		Log.d(TAG, "onStart Begin");
		if(!isConnecting){
			if (checkStatus() == 0){
				Log.d(TAG, "!!!!!start VPN");
				StartRunnable a = new StartRunnable();
				Thread startThread = new Thread(a);
				startThread.start();
			}
		}

		Log.d(TAG, "onStart End");
	}


	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		Log.d(TAG, "onBackPressed Begin");
		//stopDevices();
		//app.SignOut();
		//roomlinkStatus = false;
		app.Leave();
		//app.uninitialize();
		//finish();
	}

	void startDevices() {
		doRender = true;
	}
	
	void stopDevices() {
		doRender = false;
	}

	private void resumeCall() {
		this.bcView.onResume();
	}

	private void pauseCall() {
		this.bcView.onPause();
	}

	@Override
	public void onWindowFocusChanged(final boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		Log.d(TAG, "ACTIVITY ON WINDOW FOCUS CHANGED " + (hasWindowFocus ? "true" : "false"));
		if (hasWindowFocus && !mIsOnPause) {
			resumeCall();
			app.EnableAllVideoStreams();
		}
	}

	@Override
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		if(!roomlinkStatus&&!loginStatus){
			i_id = intent.getStringExtra("id");
			i_name = intent.getStringExtra("name");
			if(i_id != null && i_name != null) {
				removeDialog(DIALOG_LOGIN);
				showDialog(DIALOG_AUTO);
			}
		}
	}
	
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_LOGIN) {
			/*if (checkStatus() == 0){
				Log.d(TAG, "!!!!!something going on here");
				StartRunnable a = new StartRunnable();
				Thread startThread = new Thread(a);
				startThread.start();
			}*/
			LayoutInflater factory = LayoutInflater.from(this);//inflater here has view of mainactivity content view
			final View textEntryView = factory.inflate(R.layout.custom_dialog, null);//inflate view this current content with custom_dialog

			final Button login_button = (Button) textEntryView.findViewById(R.id.login_button);
			
			TextView username = (TextView) textEntryView.findViewById(R.id.username_edit);
			TextView password = (TextView) textEntryView.findViewById(R.id.password_edit);

			String idCache = mPhoneNumber;
			if(idCache == null){
				idCache = "";
			}
			String portalInfoArray[] = { "vp.kcg.go.kr", idCache, "" };

			usernameString = new StringBuffer(portalInfoArray[1]);
			passwordString = new StringBuffer(portalInfoArray[2]);
			//above code can be avoided as they are just for default values and actual username and password will be from user input

			loginInfo = getSharedPreferences("loginPrefs", MODE_PRIVATE);
			loginInfoEditor = loginInfo.edit();

			if(loginInfo.getString("username", "") != ""){
				username.setText(loginInfo.getString("username", ""));
				password.setText(loginInfo.getString("password", ""));
			}else{
				username.setText(usernameString.subSequence(0, usernameString.length()));
				password.setText(passwordString.subSequence(0, passwordString.length()));
			}
			login_button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(checkStatus() == 0){
						((TextView) textEntryView.findViewById(R.id.error_view)).setText("VPN 미접속");
						AsyncTask<String, Void, String> tk = new StartAsync(((TextView) textEntryView.findViewById(R.id.error_view))).execute("a");
						return;
					}else if(checkStatus() == 2){
						((TextView) textEntryView.findViewById(R.id.error_view)).setText("VPN 접속중..., 잠시 기다린후 로그인 하세요");
					}else if(checkStatus() == 1){
						TextView username = (TextView) textEntryView.findViewById(R.id.username_edit);
						TextView password = (TextView) textEntryView.findViewById(R.id.password_edit);

						usernameString = new StringBuffer(username.getEditableText().toString());//those string buffers called in
						passwordString = new StringBuffer(password.getEditableText().toString());//stringbuffers are allowed to modify string values

						AsyncTask<String, Void, String> name = new customHttp().execute("method=login&id="+username.getEditableText().toString()+"&pw="+password.getEditableText().toString());
						try{
							i_name = name.get();
						}catch(Exception e){
							Log.d(TAG, e.toString());
						}
						if(i_name != null && !i_name.equals("wrong")){
							Log.d(TAG, "!!!!!"+i_name);
							((TextView) textEntryView.findViewById(R.id.error_view)).setText("");
							//save login
							if (!loginInfo.getString("username", "").equals(username.getText().toString())) {
								loginInfoEditor.putString("username", username.getText().toString());
								loginInfoEditor.putString("password", password.getText().toString());
								loginInfoEditor.commit();
							}
							//save login
							removeDialog(DIALOG_LOGIN);
							roomlinkStatus = true;
							showDialog(DIALOG_ROOMLINK);
						}else{
							((TextView) textEntryView.findViewById(R.id.error_view)).setText("계정 정보 오류");
						}
					}

					/*if (checkStatus() == 0){
						StartRunnable a = new StartRunnable();
						Thread startThread = new Thread(a);
						startThread.start();
					}else{
						removeDialog(DIALOG_LOGIN);
						dialogMessage = new String("Network Unavailable!\n" + "Check network connection.");
						showDialog(FINISH_MSG);
						return;
					}*/
				}
			});

			LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			textEntryView.setLayoutParams(lp);
			return new AlertDialog.Builder(this).setTitle("현장 영상회의 시스템 접속").setView(textEntryView)
			.setNegativeButton("종료",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					removeDialog(DIALOG_LOGIN); loginStatus = false; finish();
					}}).setCancelable(false).create();
		} else if (id == DIALOG_AUTO){
			LayoutInflater factory = LayoutInflater.from(this);
			final View autoView = factory.inflate(R.layout.auto, null);

			final Button login_button = (Button) autoView.findViewById(R.id.auto_button);
			login_button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if(checkStatus() == 0){
						((TextView) autoView.findViewById(R.id.error_view)).setText("VPN 미접속");
						AsyncTask<String, Void, String> tk = new StartAsync(((TextView) autoView.findViewById(R.id.error_view))).execute("a");
						return;
					}else if(checkStatus() == 2){
						((TextView) autoView.findViewById(R.id.error_view)).setText("VPN 접속중..., 잠시 기다린후 확인 버튼을 누르세요");
					}else if(checkStatus() == 1){
						removeDialog(DIALOG_AUTO);
						roomlinkStatus = true;
						showDialog(DIALOG_ROOMLINK);

					}
				}
			});

			LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			autoView.setLayoutParams(lp);

			return new AlertDialog.Builder(this).setTitle("현장 영상회의 시스템 자동 접속").setView(autoView).setCancelable(false).create();
		} else if (id == DIALOG_ROOMLINK){
			if(bcView.getParent() != null){
				Log.d(TAG, "VIEWISFOUND!!!!!!!!");
				((ViewGroup)bcView.getParent()).removeView(bcView);
				((ViewGroup)findViewById(R.id.vc_controller).getParent()).removeView(findViewById(R.id.vc_controller));
				//((ViewGroup)bcView.getParent()).removeView(findViewById(R.id.vc_controller));
			}
			camOn = true;
			speakerOn = true;
			micOn = true;

			//KYUNG I tried to get layout instance add button views and then inflate XML of the layout. There is chance that XML is not bound to layout instance if this doesn't work inflate XML first and then find layout by findviewbyid and then add button views
			//below won't work as it looks for R.id.roomlink_dialog in whatever view is set for setcontentview at the moment, so inflate first and then findviewbyid

			//inflate roomlink_dialog that will holds roomlink buttons later in the codes
			final LayoutInflater factory = LayoutInflater.from(this);//inflater here has view of mainactivity content view
			final View buttonView = factory.inflate(R.layout.roomlink_dialog, null);//inflate view this current content with roomlink_dialog

			LinearLayout roomlink_layout = (LinearLayout) buttonView.findViewById(R.id.roomlink_dialog);

			//at this point make connection with custom DB to get list of rooom links to show
			//For apps no DB connection directly with DB so use WEB SERVICE located on Custom server that link DB and Android APP

			AsyncTask<String, Void, String> roomlinks = new customHttp().execute("method=roomlink");
			//TODO: Check return code from appSampleHttp
			List<String> item = null;
			try{
				item = Arrays.asList(roomlinks.get().split("\\s*,\\s*"));
			}catch(Exception e){
				Log.d(TAG, e.toString());
			}
			final List<String> items = item;
			for (int i = 0; i < items.size(); i++){
				Button roomlink_button = new Button(this);//this button have to be created within for loop
				//If above button is not final, would it be okay to re-assign within the for loop?
				roomlink_button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				roomlink_button.setText(items.get(i));//programmatically chage its name
				i++;
				roomlink_button.setId(i/2); //some int id for each buttons
                final int index = i;
				roomlink_button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(checkStatus() == 1){
							final View controllerView = factory.inflate(R.layout.vc_controller, null);//inflate view this current content with roomlink_dialog
							((ImageView) controllerView.findViewById(R.id.action_cam_icon)).setImageResource(camOn?R.drawable.icon_on_camera:R.drawable.icon_off_camera);
							((ImageView) controllerView.findViewById(R.id.action_speaker_icon)).setImageResource(speakerOn?R.drawable.icon_on_speaker:R.drawable.icon_off_speaker);
							((ImageView) controllerView.findViewById(R.id.action_mic_icon)).setImageResource(micOn?R.drawable.icon_on_mic:R.drawable.icon_off_mic);
							controllerView.findViewById(R.id.action_cam_icon).setOnClickListener(new View.OnClickListener(){
								@Override
								public void onClick(View v){
									((ImageView) v).setImageResource(camOn?R.drawable.icon_off_camera:R.drawable.icon_on_camera);
									app.MuteCamera(camOn);
									app.SetPreviewModeON(!camOn);
									camOn = !camOn;
								}
							});
							controllerView.findViewById(R.id.action_speaker_icon).setOnClickListener(new View.OnClickListener(){
								@Override
								public void onClick(View v){
									((ImageView) v).setImageResource(speakerOn?R.drawable.icon_off_speaker:R.drawable.icon_on_speaker);
									app.MuteSpeak(speakerOn);
									speakerOn = !speakerOn;
								}
							});

							//originally intended as setting -> speakerphone -> zoom/volume switch
							controllerView.findViewById(R.id.action_setting_icon).setOnClickListener(new View.OnClickListener(){
								@Override
								public void onClick(View v){
									/*
									((ImageView) v).setImageResource(speakerphoneOn?R.drawable.icon_on_setting:R.drawable.icon_on_setting);
									am.setSpeakerphoneOn(speakerphoneOn);
									speakerphoneOn = !speakerphoneOn;
									*/
									zoommode = !zoommode;
									String toast_message;

									if (zoommode){
										toast_message = "카메라 줌 조절 모드";
									}else{
										toast_message = "볼륨 조절 모드";
									}
									Toast toast = Toast.makeText(getApplicationContext(),
											toast_message,
											Toast.LENGTH_SHORT);

									toast.show();

								}
							});
							//speakerphone

							controllerView.findViewById(R.id.action_mic_icon).setOnClickListener(new View.OnClickListener(){
								@Override
								public void onClick(View v){
									((ImageView) v).setImageResource(micOn?R.drawable.icon_off_mic:R.drawable.icon_on_mic);
									app.MuteMic(micOn);
									micOn = !micOn;
								}
							});
						/*
						controllerView.findViewById(R.id.action_setting_icon).setOnClickListener(new View.OnClickListener(){
							@Override
							public void onClick(View v){
								app.MuteMic(micOn);
								micOn = !micOn;
							}
						});*/
							controllerView.findViewById(R.id.action_exit_icon).setOnClickListener(new View.OnClickListener(){
								@Override
								public void onClick(View v){
									app.Leave();
								}
							});
							controllerView.findViewById(R.id.action_frontback_icon).setOnClickListener(new View.OnClickListener(){
								@Override
								public void onClick(View v){
									if(camOn) {
										if (usedCamera == 1) {
											((ImageView) v).setImageResource(R.drawable.icon_back_camera);
											usedCamera = 0;
										} else {
											((ImageView) v).setImageResource(R.drawable.icon_front_camera);
											usedCamera = 1;
										}
										app.SetCameraDevice(usedCamera);
									}
								}
							});
							usedCamera = 0;//originally 1
							//app.SetCameraDevice(usedCamera);
							if(findViewById(R.id.glsurfaceview) != null){
								View C = findViewById(R.id.glsurfaceview);
								ViewGroup parent = (ViewGroup) C.getParent();
								int index = parent.indexOfChild(C);
								parent.removeView(C);
								parent.addView(bcView, index);
								((ViewGroup) parent.getParent()).addView(controllerView);
								Log.d(TAG, "!!!!!"+parent.getHeight()+"#"+parent.getWidth());
								Log.d(TAG, "!!!!!"+((ViewGroup) parent.getParent()).getHeight()+"#"+((ViewGroup) parent.getParent()).getWidth());

							}else{
								if(findViewById(R.id.RelativeLayout01) != null){
									Log.d(TAG, "VIEWISFOUND!!!!!!!!");
									((RelativeLayout)findViewById(R.id.RelativeLayout01)).addView(bcView);
									((ViewGroup) findViewById(R.id.RelativeLayout01).getParent()).addView(controllerView);
								}
							}

							//cameraView = (ImageView)findViewById(R.id.action_camera_icon);
							//cameraView.setOnClickListener(this);

	  /* Camera */
							//usedCamera = 1;

							//adjustFontScale(getResources().getConfiguration());

						/*if (checkStatus() == 0){
							StartRunnable a = new StartRunnable();
							Thread startThread = new Thread(a);
							startThread.start();
						}else{
							removeDialog(DIALOG_ROOMLINK);
							dialogMessage = new String("Network Unavailable!\n" + "Check network connection.");
							showDialog(FINISH_MSG);
							return;
						}*/

							app.SetBackgroundColor();
							//app.SetParticipantsLimit();
							String roomkey = items.get(index);
							last_roomkey = roomkey;
							if(i_name == null)i_name = "Unknown";
							callinmiddle = true;
							app.JoinRoomLink("http://10.29.16.206",
									roomkey,
									i_name,
									"",!camOn, !micOn, !speakerOn);
							removeDialog(DIALOG_ROOMLINK);
						}else{
							removeDialog(DIALOG_ROOMLINK);
							dialogMessage = new String("VPN 미접속\n" + "앱에 다시 접속하세요");
							showDialog(FINISH_MSG);
							return;
						}

					}
				});
				//add button to the layout
				roomlink_layout.addView(roomlink_button);
			}

			//additional buttons for mute camera, speaker, mic
			View cam = buttonView.findViewById(R.id.action_cam_icon);
			cam.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
					if(camOn){
						((ImageView) v).setImageResource(R.drawable.icon_off_camera);
						camOn = false;
					}else{
						((ImageView) v).setImageResource(R.drawable.icon_on_camera);
						camOn = true;
					}
				}
			});
			View sp = buttonView.findViewById(R.id.action_speaker_icon);
			sp.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
					if(speakerOn){
						((ImageView) v).setImageResource(R.drawable.icon_off_speaker);
						speakerOn = false;
					}else{
						((ImageView) v).setImageResource(R.drawable.icon_on_speaker);
						speakerOn = true;
					}
				}
			});
			View mic = buttonView.findViewById(R.id.action_mic_icon);
			mic.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
					if(micOn){
						((ImageView) v).setImageResource(R.drawable.icon_off_mic);
						micOn = false;
					}else{
						((ImageView) v).setImageResource(R.drawable.icon_on_mic);
						micOn = true;
					}
				}
			});


			LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			buttonView.setLayoutParams(lp);
			return new AlertDialog.Builder(this).setTitle("회의방 접속").setView(buttonView)
					.setNegativeButton("로그아웃",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									roomlinkStatus = false;
									removeDialog(DIALOG_ROOMLINK);
									showDialog(DIALOG_LOGIN);
								}}).setCancelable(false).create();
		}

		else if (id == DIALOG_MSG) {  // Handle network errors - cannot proceed situations
			AlertDialog alert;
			AlertDialog.Builder builder;
			stopDevices();			
			
			builder = new AlertDialog.Builder(this).setTitle(message)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									removeDialog(DIALOG_MSG);
									showDialog(DIALOG_LOGIN);
								}
							});
			alert = builder.create();			
			return alert;

		} else if (id == FINISH_MSG) {  // Handle network errors - cannot proceed situations
			AlertDialog alert;
			AlertDialog.Builder builder;
			stopDevices();
		
			builder = new AlertDialog.Builder(this).setTitle(dialogMessage)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								removeDialog(FINISH_MSG);
								finish();
							}
						});
			alert = builder.create();
			return alert;
		}
		return null;
	}

	public void LmiDeviceManagerViewRender() {
		if (doRender){
			app.Render();
		}

	}

	public void LmiDeviceManagerViewResize(int width, int height) {
		app.Resize(width, height);
	}

	public void LmiDeviceManagerViewRenderRelease() {
		app.RenderRelease();
	}

	Runnable mRunnable = new Runnable() {
		@Override@TargetApi(12)
		public void run() {
			if(findViewById(R.id.vc_controller) != null){
				findViewById(R.id.vc_controller).animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						findViewById(R.id.vc_controller).setVisibility(View.GONE);
					}
				});
			}
		}
	};
	Handler mHandler = new Handler();
	@TargetApi(12)
	public void LmiDeviceManagerViewTouchEvent(int id, int type, int x, int y) {
		app.TouchEvent(id, type, x, y);
		if(findViewById(R.id.vc_controller) != null && type == 0){
			Log.d(TAG, "!!!!!TOUCHED");
			if(findViewById(R.id.vc_controller).getVisibility() == View.GONE){
				findViewById(R.id.vc_controller).animate().alpha(1.0f).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						findViewById(R.id.vc_controller).setVisibility(View.VISIBLE);
						mHandler.removeCallbacks(mRunnable);
						mHandler.postDelayed(mRunnable, 5000);
					}
				});
			}else{
				findViewById(R.id.vc_controller).animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						findViewById(R.id.vc_controller).setVisibility(View.GONE);
					}
				});
			}
		}
	}

	public int LmiDeviceManagerCameraNewFrame(byte[] frame, String fourcc,
			int width, int height, int orientation, boolean mirrored) {
		return app.SendVideoFrame(frame, fourcc, width, height, orientation, mirrored);
	}

	public int LmiDeviceManagerMicNewFrame(byte[] frame, int numSamples,
			int sampleRate, int numChannels, int bitsPerSample) {
		return app.SendAudioFrame(frame, numSamples, sampleRate, numChannels,
				bitsPerSample);
	}

	public int LmiDeviceManagerSpeakerNewFrame(byte[] frame, int numSamples,
			int sampleRate, int numChannels, int bitsPerSample) {
		return app.GetAudioFrame(frame, numSamples, sampleRate, numChannels,
				bitsPerSample);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		int newOrientation = currentOrientation;

		int type = event.sensor.getType();
		float[] data;
		if (type == Sensor.TYPE_ACCELEROMETER) {
			data = mGData; /* set accelerometer data pointer */
		} else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
			data = mMData; /* set magnetic data pointer */
		} else {
			return;
		}
		/* copy the data to the appropriate array */
		for (int i = 0; i < 3; i++)
			data[i] = event.values[i];		/* copy the data to the appropriate array */

		/*
		 * calculate the rotation data from the latest accelerometer and
		 * magnetic data
		 */
		Boolean ret = SensorManager.getRotationMatrix(mR, mI, mGData, mMData);
		if (ret == false)
			return;
		
		SensorManager.getOrientation(mR, mOrientation);

		Configuration config = getResources().getConfiguration();
		boolean hardKeyboardOrientFix = (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);
		
		int pitch = (int) (mOrientation[1] * degreePerRadian);
		int roll = (int) (mOrientation[2] * degreePerRadian);

		if (pitch < -45) {
			if (hardKeyboardOrientFix)
				newOrientation = ORIENTATION_LEFT;
			else
				newOrientation = ORIENTATION_UP;
		} else if (pitch > 45) {
			if (hardKeyboardOrientFix)
				newOrientation = ORIENTATION_RIGHT;
			else
				newOrientation = ORIENTATION_DOWN;
		} else if (roll < -45 && roll > -135) {
			if (hardKeyboardOrientFix)
				newOrientation = ORIENTATION_UP;
			else
				newOrientation = ORIENTATION_RIGHT;
		} else if (roll > 45 && roll < 135) {
			if (hardKeyboardOrientFix)
				newOrientation = ORIENTATION_DOWN;
			else
				newOrientation = ORIENTATION_LEFT;
		}

		//	Log.d(app.TAG, "Orientation: " + newOrientation + " pitch: " + pitch + " roll: " + roll);
		if (newOrientation != currentOrientation) {
			currentOrientation = newOrientation;
			app.SetOrientation(newOrientation);
		}

		/*
		if (newOrientation != currentOrientation) {
			camera.setCameraOrientation( newOrientation );
			currentOrientation = newOrientation;
		}
		*/
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
			case R.id.action_frontback_icon:
				if (usedCamera == 1) {
					usedCamera = 0;
				} else {
					usedCamera = 1;
				}
				app.SetCameraDevice(usedCamera);

/*				if (bcCamera.isStarted()) {
					if (bcCamera.useFrontCamera) {
						bcCamera.switchCamera(false, false, 0, false, false);
						app.SetCameraDevice(1);
						cameraView.setImageResource(R.drawable.icon_back_camera);
					} else {
						bcCamera.switchCamera(true, false, 0, false, false);
						app.SetCameraDevice(0);
						cameraView.setImageResource(R.drawable.icon_front_camera);
					}
				}*/
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "in onConfigurationChanged");
	}
	public static boolean IsExistFile(String strSrc){
		File file = new File(strSrc);
		return file.exists();
	}
	public static void DeleteDirectory(String path)
	{
		if(IsExistFile(path))
		{
			File file = new File(path);
			File[] childFileList = file.listFiles();
			for(File childFile : childFileList)
			{
				if(childFile.isDirectory())
				{
					DeleteDirectory(childFile.getAbsolutePath());
				}
				else
				{
					childFile.delete();
				}
			}
			file.delete();
		}
	}
	public void adjustFontScale(Configuration configuration)
	{
		configuration.fontScale = (float) 2.0;
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		metrics.scaledDensity = configuration.fontScale * metrics.density;
		getBaseContext().getResources().updateConfiguration(configuration, metrics);
	}

	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(zoommode){
			if (keyCode == KeyEvent.KEYCODE_ZOOM_IN) {
				Log.d(TAG, "Zoom IN");
				LmiVideoCapturerInternal.zoomin();

			} else if (keyCode == KeyEvent.KEYCODE_ZOOM_OUT) {
				Log.d(TAG, "Zoom OUT");
				LmiVideoCapturerInternal.zoomout();

			}
			return true;
		}
		else {
			return super.onKeyDown(keyCode, event);
		}
	}*/
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(zoommode){
			if (keyCode == KeyEvent.KEYCODE_ZOOM_IN) {
				Log.d(TAG, "Zoom IN");
				Intent intent=new Intent("com.local.receiver");
				intent.putExtra("KEYCODE", KeyEvent.KEYCODE_ZOOM_IN);
				sendBroadcast(intent);

			} else if (keyCode == KeyEvent.KEYCODE_ZOOM_OUT) {
				Log.d(TAG, "Zoom OUT");
				Intent intent=new Intent("com.local.receiver");
				intent.putExtra("KEYCODE", KeyEvent.KEYCODE_ZOOM_OUT);
				sendBroadcast(intent);

			}
			return true;
		}
		else {
			return super.onKeyDown(keyCode, event);
		}
	}


}
