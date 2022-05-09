package pwr.edu.app.parkinsonsdisease.mbientlab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mbientlab.metawear.AsyncDataProducer;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.data.AngularVelocity;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Debug;
import com.mbientlab.metawear.module.Gyro;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import bolts.Continuation;
import pwr.edu.app.parkinsonsdisease.PatientMainActivity;
import pwr.edu.app.parkinsonsdisease.R;
/**
 * A placeholder fragment containing a simple view.
 */
public class DeviceSetupActivityFragment extends Fragment implements ServiceConnection {
    private final static String GYROSCOPE_TEST = "GYROSCOPE_TEST";
    private final static String ACCELEROMETER_TEST = "ACCELEROMETER_TEST";

    private int numberOfTest = 3;
    private long timeCount = 30 * 1000;

    private CountDownTimer countDownTimer;
    private Accelerometer accelerometer;
    private Gyro gyroscope;

    private Map<String, List<String>> mapResults = new HashMap<>();
    List<String> resultsLeft= new ArrayList<>();
    List<String> resultsRight = new ArrayList<>();

    private ProgressBar progressBar;
    private String hourSinceLastDrug;
    private String nameTest;
    private Button startTestBtn;
    private Button stopTestBtn;
    private TextView testSideTextView;
    private TextView timeCounterTextView;
    private TextView informationTextView;

    private MetaWearBoard metawear = null;
    private FragmentSettings settings;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String nameDocumentTest;

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public interface FragmentSettings {
        BluetoothDevice getBtDevice();
    }

    public DeviceSetupActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity owner = getActivity();
        if (!(owner instanceof FragmentSettings)) {
            throw new ClassCastException("Owning activity must implement the FragmentSettings interface");
        }

        settings = (FragmentSettings) owner;
        owner.getApplicationContext().bindService(new Intent(owner, BtleService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_device_setup, container, false);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        DeviceSetupActivity activity = (DeviceSetupActivity) getActivity();
        hourSinceLastDrug = activity.getHourSinceLastDrug();
        nameTest = activity.getNameTest();

        metawear = ((BtleService.LocalBinder) service).getMetaWearBoard(settings.getBtDevice());

        switch (nameTest) {
            case ACCELEROMETER_TEST:
                metawear.tearDown();
                accelerometer = metawear.getModule(Accelerometer.class);
                accelerometer.configure()
                        .odr(25f)
                        .commit();
                break;

            case GYROSCOPE_TEST:
                gyroscope = metawear.getModule(Gyro.class);
                gyroscope.configure()
                        .odr(Gyro.OutputDataRate.ODR_25_HZ)
                        .range(Gyro.Range.FSR_2000)
                        .commit();
                break;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    /**
     * Called when the app has reconnected to the board
     */
    public void reconnected() {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.startTestBtn).setOnClickListener(v -> onClickStart());

//        view.findViewById(R.id.stopTestBtn).setOnClickListener(v -> onClickStop());
    }

    private void finishTest() {
        addDataToFirebase();
        metawear.getModule(Debug.class).disconnectAsync();
        Toast.makeText(getContext(), "Zakończono test", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity().getApplicationContext(), PatientMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void addDataToFirebase() {
        resultsLeft.add(0, "timestamp, aX, aY, aZ");
        resultsRight.add(0, "timestamp, aX, aY, aZ");
        Log.d("TEST_NAME", nameTest);

        Map<String, Object> empty = new HashMap<>();
        Log.d("TEST", "document: " + nameDocumentTest);
        Log.d("TEST", "test: " + nameTest);
        if(nameDocumentTest.equals(nameTest)){
            Log.d("TEST", "identyczne");
        }

        DocumentReference testData = db.collection("users")
                .document(Objects.requireNonNull(auth.getUid()))
                .collection("testsHistory")
                .document(nameDocumentTest);

        testData.set(empty);
        testData.collection("testDates");
        testData.collection("testDates").document(formatter.format(date));

        DocumentReference testDataSet = db.collection("users")
                .document(auth.getUid())
                .collection("testsHistory")
                .document(nameDocumentTest).collection("testDates").document(formatter.format(date));

        Map<String, Object> dataMed = new HashMap<>();
        dataMed.put("hoursSinceLastMed", hourSinceLastDrug);
        testDataSet.set(dataMed);

        Map<String, Object> dataLeft = new HashMap<>();
        Map<String, Object> dataRight = new HashMap<>();
        dataLeft.put("accel", resultsLeft);
        dataRight.put("accel", resultsRight);

        testDataSet.collection("LEFT");
        testDataSet.collection("RIGHT");
        testDataSet.collection("LEFT").document("testData").set(dataLeft);
        testDataSet.collection("RIGHT").document("testData").set(dataRight);
    }

    private void setPrograssBar() {
        progressBar.setMax((int) timeCount / 1000);
        progressBar.setProgress((int) timeCount / 1000);
    }

    private void initView() {
        progressBar = getActivity().findViewById(R.id.progressBar);
        startTestBtn = getActivity().findViewById(R.id.startTestBtn);
        stopTestBtn = getActivity().findViewById(R.id.stopTestBtn);
        testSideTextView = getActivity().findViewById(R.id.connect_title);
        timeCounterTextView = getActivity().findViewById(R.id.textViewTime);
        informationTextView = getActivity().findViewById(R.id.connect_information_window);
    }

    private void onClickStart() {
        initView();
        setPrograssBar();
        startTestBtn.setEnabled(false);
        implementationTest();
        getCountDownTimer();
    }

    private void getCountDownTimer() {
        countDownTimer = new CountDownTimer(timeCount, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeCounterTextView.setText(hmsTimeFormatter(millisUntilFinished));
                progressBar.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                setViewElement();
                stopDevice();
                if(numberOfTest == 3){
                    startTestBtn.setEnabled(true);
                    testSideTextView.setText("Badanie LEWEJ ręki");
                }else if (numberOfTest == 1){
                    testSideTextView.setText("Koniec badania");
                    informationTextView.setText("Dobra robota!");
                    startTestBtn.setEnabled(true);
                }else if(numberOfTest==0){
                    timeCounterTextView.setText("Koniec");
                    progressBar.setVisibility(View.INVISIBLE);
                    finishTest();
//                    stopTestBtn.setVisibility(View.VISIBLE);
                }
                --numberOfTest;
            }

        }.start();
        countDownTimer.start();


    }

    private void stopDevice() {
        switch (nameTest) {
            case ACCELEROMETER_TEST:
                accelerometer.stop();
                accelerometer.acceleration().stop();
                break;

            case GYROSCOPE_TEST:
                Log.i("TEST", "Gryoscope stop");
                gyroscope.stop();
                gyroscope.angularVelocity().stop();
                break;
        }
        metawear.tearDown();
    }

    private void setViewElement() {
        timeCount = 30 * 1000;
        progressBar.setMax((int) timeCount / 1000);
        progressBar.setProgress((int) timeCount / 1000);
        timeCounterTextView.setText("30");
        startTestBtn.setEnabled(true);
    }

    private void implementationTest() {
        switch (nameTest) {
            case ACCELEROMETER_TEST:
                nameDocumentTest = "ACCELEROMETER_TEST";
                accelerometerImplementation();
                break;

            case GYROSCOPE_TEST:
                nameDocumentTest = " GYROSCOPE_TEST";
                gyroscopeImplementation();
                break;
        }
    }

    private void accelerometerImplementation() {
        AtomicLong timeStart= new AtomicLong((new Timestamp(System.currentTimeMillis())).getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("ss.SSS");

        accelerometer.acceleration().addRouteAsync(source ->
                source.stream((Subscriber) (data, env) -> {
                    float x = data.value(Acceleration.class).x();
                    float y = data.value(Acceleration.class).y();
                    float z = data.value(Acceleration.class).z();

                    long timeNew = (new Timestamp(System.currentTimeMillis())).getTime();
                    long timeStamp = timeNew - timeStart.get();
//                    Log.d("TEST", sdf.format(timeStamp) + " => " +  timeNew + " - " + timeStart);
                    if(numberOfTest == 3){
                        resultsRight.add(sdf.format(timeStamp)  + "," + x + "," + y + "," + z);
                    }else if(numberOfTest == 1){
                        resultsLeft.add(sdf.format(timeStamp)  + "," + x + "," + y + "," + z);
                    }

                })).continueWith((Continuation<Route, Void>)
                task -> {
                    accelerometer.acceleration().start();
                    accelerometer.start();
                    return null;
                });
    }

    private void gyroscopeImplementation() {
        AtomicLong timeStart= new AtomicLong((new Timestamp(System.currentTimeMillis())).getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("ss.SSS");

        final AsyncDataProducer producer = gyroscope.packedAngularVelocity() == null ?
                gyroscope.packedAngularVelocity() :
                gyroscope.angularVelocity();
        producer.addRouteAsync(source -> source.stream((data, env) -> {
            float x = data.value(AngularVelocity.class).x();
            float y = data.value(AngularVelocity.class).y();
            float z = data.value(AngularVelocity.class).z();

            long timeNew = (new Timestamp(System.currentTimeMillis())).getTime();
            long timeStamp = timeNew - timeStart.get();
            Log.i("TEST", sdf.format(timeStamp)  + " , " + x + " , " + y + " , " + z);
            if(numberOfTest == 3){
                resultsRight.add(sdf.format(timeStamp)  + "," + x + "," + y + "," + z);
            }else if(numberOfTest == 1){
                resultsLeft.add(sdf.format(timeStamp)  + "," + x + "," + y + "," + z);
            }
        })).continueWith(task -> {
            gyroscope.angularVelocity().start();
            gyroscope.start();
            return null;
        });
    }

    @SuppressLint("DefaultLocale")
    private String hmsTimeFormatter(long milliSeconds) {

        return String.format("%02d",
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }
}