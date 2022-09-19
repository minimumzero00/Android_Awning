package com.example.project;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

import static android.text.TextUtils.split;

public class Bluetooth extends AppCompatActivity {

    // 화면전화 시작

//    private Button button10New;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        button10New = (Button) findViewById(R.id.button10);
//
//        button10New.setOnClickListener((v) -> {
//            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//            startActivity(intent);
//        });
//    }
    // 화면전화 끝

    //private ActivityResultLauncher<Intent> resultLauncher; //추가

    // GUI Components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;

    private Button mScanBtn; //블루투스 on 버튼
    private Button mOffBtn; //블루투스 off 버튼
    private Button mListPairedDevicesBtn; //show paired devices(디바이스 표시)
    private Button mDiscoverBtn; //discover new devices(새로운 디바이스 찾기)
    private CheckBox mLED1; //상단 체크박스

    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices; //블루투스 디바이스 데이터 셋
    private ArrayAdapter<String> mBTArrayAdapter; //어댑터
    private ListView mDevicesListView; //디바이스 목록, 뷰(실제화면)

    private Handler mHandler; // 콜백 알림을 받을 기본 핸들러
    private ConnectedThread mConnectedThread; // Bluetooth 백그라운드 작업자 스레드를 사용하여 데이터 전송 및 수신
    private BluetoothSocket mBTSocket = null; // 양방향 클라이언트 간 데이터 경로

    // "random" unique identifier
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    // #defines for identifying shared types between calling functions
    //#호출 함수 간의 공유 유형을 식별하기 위한 도구
    //final: 지역변수를 상수화 시켜주는 명령어
    private final static int REQUEST_ENABLE_BT = 1; // 블루투스 이름 추가를 식별하는 데 사용
    private final static int MESSAGE_READ = 2; // 블루투스 핸들러에서 메시지 업데이트를 식별하는 데 사용
    private final static int CONNECTING_STATUS = 3; // 블루투스 핸들러에서 메시지 상태를 식별하는 데 사용


    @Override //메소드 재정의
    //Bundle 여러가지 타입의 값을 저장하는 Map 클래스
    protected void onCreate(Bundle savedInstanceState) {
//
//        // 화면전화 시작
//        //private Button button10New;
//
//            setContentView(R.layout.activity_main);
//
//            button10New = (Button) findViewById(R.id.button10);
//
//            button10New.setOnClickListener((v) -> {
//                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//                startActivity(intent);
//            });
//
//        // 화면전화 끝

        //super: 부모 클래스로부터 상속받은 필드나 메소드를 자식 클래스에 참조하는데 사용하는 참조변수
        super.onCreate(savedInstanceState);
        //setContentView: 레이아웃 보여주기 위해 사용
        setContentView(R.layout.activity_bluetooth); //activity_bluetooth.xml 보여줌

        //findViewById: 레이아웃에 설정된 뷰들을 가져오는 메소드
        mBluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus); //Status 결과값
        mReadBuffer = (TextView) findViewById(R.id.readBuffer); //PX 결과값
        mScanBtn = (Button)findViewById(R.id.scan); //블루투스 on
        mOffBtn = (Button)findViewById(R.id.off); //블루투스 off
        mDiscoverBtn = (Button)findViewById(R.id.discover); //discover new devices(새로운 디바이스 찾기)
        mListPairedDevicesBtn = (Button)findViewById(R.id.PairedBtn); //show paired devices(디바이스 표시)
        mLED1 = (CheckBox)findViewById(R.id.checkboxLED1); //상단 체크박스

        //리스트뷰를 사용하기 위한 3가지: 뷰(실제화면), 어댑터(뷰와 데이터 사이 매개체), 데이터(실제 데이터)
        //ArrayAdapter: 하나의 항목에 하나의 문자를 나열할 때 사용
        //mBTArrayAdapter: 어댑터 //simple_list_item_1: 텍스트뷰 하나로 구성된 레이아웃
        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        //findViewById: 레이아웃에 설정된 뷰들을 가져오는 메소드
        mDevicesListView = (ListView)findViewById(R.id.devicesListView); //디바이스 목록
        //listview에 mBTArrayAdapter를 적용
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener); //listview의 (?)클릭시 mDeviceClickListener 이벤트


        //Handler: 다른 객체들이 보낸 데이터를 받고 이 데이터를 처리하는 객체
        //스레드에서 UI를 제어하려고 할 때 핸들러(Handler) 사용
        //블루투스 연결 뒤 수신된 데이터를 읽어와 ReceiveData 텍스트 뷰에 표시해주는 부분
        mHandler = new Handler(){ //(원본)
        //mHandler = new Handler(Looper.getMainLooper()){ //(수정)
            //handleMessage() : 메소드에 정의된 기능이 수행됨
            public void handleMessage(Message msg){ //Message 객체를 사용
                //MESSAGE_READ 인지 아니면 CONNECTING_STATUS 인지 따져서 확인
                //만약 MESSAGE_READ 라면 문자열 버퍼 readMessge를 체크하여 mReadBuffer에 저장
                if(msg.what == MESSAGE_READ){ //MESSAGE_READ = 2
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mReadBuffer.setText(readMessage);
                }

                //mas.arg1을 체크하여 “1”이면 “Connected to Device”
                // “1”이 아니면 “Connection Failed”를 <Bluetooth Status>에 디스플레이
                if(msg.what == CONNECTING_STATUS){ //CONNECTING_STATUS = 3
                    if(msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + (String)(msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };

        //mBTArrayAdapter가 null 로 체크 되면 해당 메시지를 mBluetoothStatus 뿐만 아니라 아예 Toast 디스플레이
        //블루투스를 지원하지 않는 기기라면
        if (mBTArrayAdapter == null) { //mBTArrayAdapter: 어댑터
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show(); //(원본)
            //Toast.makeText(getApplicationContext(),"블루투스를 지원하지 않는 기기입니다.",Toast.LENGTH_SHORT).show(); //(수정)
        }
        else { //블루투스를 지원하는 기기라면
            //블루투스 on
            mLED1.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null) //First check to make sure thread created
                        //먼저 스레드가 생성되었는지 확인합니다.
                        mConnectedThread.write("1");
                }
            });

            //블루투스 off
            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });

            //show paired devices
            mOffBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    bluetoothOff(v);
                }
            });

            //discover new devices
            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });
        }
    }

    //블루투스 활성화 메소드
    private void bluetoothOn(View view){ 
        if (!mBTAdapter.isEnabled()) { //블루투스가 비활성화 되어 있으면
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); //사용불가
            //registerForActivityResult(enableBtIntent, REQUEST_ENABLE_BT); //추가
            //ActivityResultLauncher(enableBtIntent, REQUEST_ENABLE_BT); //추가

            mBluetoothStatus.setText("Bluetooth enabled"); //(원본)
            //mBluetoothStatus.setText("활성화"); //(수정)
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show(); //(원본)
            //Toast.makeText(getApplicationContext(),"블루투스가 활성화 되었습니다.",Toast.LENGTH_SHORT).show(); //(수정)

        }
        else{ //블루투스가 활성화 되어 있으면
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show(); //(원본)
            //Toast.makeText(getApplicationContext(),"블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_SHORT).show(); //(수정)
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    //사용자가 라디오를 활성화하기 위해 "예" 또는 "아니오"를 선택한 후 여기에 입력
    //블루투스 활성화 결과를 위한 onActivityResult 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode == REQUEST_ENABLE_BT) { // REQUEST_ENABLE_BT = 1
            // Make sure the request was successful
            if (resultCode == RESULT_OK) { //블루투스 활성화를 했다면
                // The user picked a contact.
                //사용자가 연락처를 선택했습니다.
                // The Intent's data Uri identifies which contact was selected.
                //의도의 데이터 URI는 선택된 연락처를 식별합니다.
                mBluetoothStatus.setText("Enabled"); //(원본)
                //mBluetoothStatus.setText("활성화"); //(수정)
            } else
                mBluetoothStatus.setText("Disabled"); //(원본)
                //mBluetoothStatus.setText("비활성화"); //(수정)
        }
    }
    
    //블루투스 비활성화 메소드
    private void bluetoothOff(View view){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled"); //(원본)
        //mBluetoothStatus.setText("비활성화"); //(수정)
        //Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show(); //(원본)
        Toast.makeText(getApplicationContext(),"블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show(); //(수정)
    }

    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    //블루투스 페어링 장치 목록 가져오는 메소드
    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) { //블루투스가 활성화 상태인지 확인
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
