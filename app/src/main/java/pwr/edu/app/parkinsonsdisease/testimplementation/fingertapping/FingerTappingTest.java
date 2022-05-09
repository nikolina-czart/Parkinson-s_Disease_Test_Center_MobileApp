package pwr.edu.app.parkinsonsdisease.testimplementation.fingertapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import pwr.edu.app.parkinsonsdisease.PatientMainActivity;
import pwr.edu.app.parkinsonsdisease.R;

public class FingerTappingTest extends AppCompatActivity {
    private final static String FINGER_TAPPING = "FINGER_TAPPING";
    public static final String EXTRA_DATA = "patientData";
    public final static String HOUR_SINCE_LAST_DRUG = "hour since last drug";
    public final static String NAME_TEST = "name test";

    private long timeCount = 20 * 1000;
    private int numberOfTest = 3;

    private TextView title;
    private Button start;
    private Button startBtn;
    private Button stopBtn;
    private Button rightBtn;
    private Button leftBtn;
    private boolean next = false;
    private boolean start2 = false;
    private int counter = 0;
    private List<String> tapLineList = new ArrayList<>();
    private List<String> accelLineList = new ArrayList<>();
    private List<String> tapList = new ArrayList<>();
    private List<Double> timeList = new ArrayList<>();

    List<String> tapResultsRight = new ArrayList<>();
    List<String> accelResultsRight = new ArrayList<>();
    List<String> tapResultsLeft = new ArrayList<>();
    List<String> accelResultsLeft = new ArrayList<>();

    long startTime = 0;
    LinearLayout tapSpace;
    TextView tapCounter;
    String patientInfo;
    ProgressBar progressBar;
    private boolean rightHand = true;

    private SensorManager mySensorManager; //manadzer czujnikow
    private Sensor myAccelerometer; //obiekt klasy Sensor (czyli czujnik)
    private Accelerometer accel = new Accelerometer();
    private CountDownTimer countDownTimer;
    private String dateString;
    private TextView timeCounterTextView;

    private String hourSinceLastDrug;
    private String nameTest;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_test);

        hourSinceLastDrug = getIntent().getStringExtra(HOUR_SINCE_LAST_DRUG);
        nameTest = getIntent().getStringExtra(NAME_TEST);
        rightHand = getIntent().getExtras().getBoolean(EXTRA_DATA);

        dateString = formatter.format(date);

        init();

        tapSpace.setOnTouchListener((v, event) -> tapSpaceImplementation(event));
        leftBtn.setOnTouchListener((v, event) -> leftBtnImplementation(event));
        rightBtn.setOnTouchListener((v, event) -> rightBtnImplementation(event));

        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        myAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mySensorManager.registerListener(accel, myAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onClickStartBtn(View view) {

        if (!next) {
            progressBar.setMax((int) timeCount / 1000);
            progressBar.setProgress((int) timeCount / 1000);
            startBtn.setEnabled(false);

            new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {
                    startBtn.setText(Integer.toString((int) millisUntilFinished / 1000));
                }

                public void onFinish() {
                    startBtn.setVisibility(View.INVISIBLE);
                    timeCounterTextView.setVisibility(View.VISIBLE);
                    start2 = true;
                    accelLineList.clear();
                    tapLineList.clear();
                    tapLineList.add("time,UP|DOWN,X,Y,R|L|N");

                    Log.d(FINGER_TAPPING, "Właściwy pomiar");
                    rightBtn.setEnabled(true);
                    leftBtn.setEnabled(true);

                    countDownTimer = new CountDownTimer(timeCount, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            timeCounterTextView.setText(hmsTimeFormatter(millisUntilFinished));
                            progressBar.setProgress((int) (millisUntilFinished / 1000));
                        }

                        @Override
                        public void onFinish() {
                            if (rightHand) {
                                timeCount = 20 * 1000;
                                progressBar.setMax((int) timeCount / 1000);
                                progressBar.setProgress((int) timeCount / 1000);
                                timeCounterTextView.setText("20");
                                rightHand = false;
                                startTime = 0;
                                counter = 0;
                                setTitle("Test lewej ręki");
                                title.setText("Badanie LEWEJ ręki");
                                startBtn.setText("START");
                                startBtn.setVisibility(View.VISIBLE);
                                startBtn.setEnabled(true);
                                timeCounterTextView.setVisibility(View.INVISIBLE);
                                tapResultsRight.addAll(tapLineList);
                                accel.stopRunning();
                                accelResultsRight.addAll(accel.getLineList());
                            } else {
                                accel.stopRunning();
                                tapResultsLeft.addAll(tapLineList);
                                accelResultsLeft.addAll(accel.getLineList());
                                startBtn.setVisibility(View.INVISIBLE);
                                timeCounterTextView.setText("Koniec testu");
                                progressBar.setVisibility(View.INVISIBLE);
                                finishTest();
                            }
                            rightBtn.setEnabled(false);
                            leftBtn.setEnabled(false);
                        }

                    }.start();

                }
            }.start();
        }
    }


    public void finishTest() {
        Toast.makeText(this, "Test zakończony", Toast.LENGTH_SHORT).show();
        addDataToFirebase();
        Intent intent = new Intent(this, PatientMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean rightBtnImplementation(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (start2) {
                onTouchListener(x, y, "DOWN", "R");
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (start2) {
                onTouchListener(x, y, "UP", "R");
            }
            return true;
        }
        return false;
    }

    private boolean leftBtnImplementation(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (start2) {
                onTouchListener(x, y, "DOWN", "L");
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (start2) {
                onTouchListener(x, y, "UP", "L");
            }
            return true;
        }
        return false;
    }

    private boolean tapSpaceImplementation(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (start2) {
                onTouchListener(x, y, "DOWN", "N");
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (start2) {
                onTouchListener(x, y, "UP", "N");
            }
            return true;
        }
        return false;
    }

    private void onTouchListener(float x, float y, String k, String l) {

        if (startTime == 0) {
            startTime = SystemClock.elapsedRealtime();
            startBtn.setVisibility(View.INVISIBLE);
            accel.startRunning();
        }
        if (k == "DOWN")
            counter++;

        tapCounter.setText(Integer.toString(counter));

        long endTime = SystemClock.elapsedRealtime();
        long elapsedMilliSeconds = endTime - startTime;
        double elapsedSeconds = elapsedMilliSeconds / 1000.0;

        tapLineList.add(elapsedSeconds + "," + k + "," + x + "," + y + "," + l);
//        Log.d(FINGER_TAPPING, elapsedSeconds + "," + k + "," + x + "," + y + "," + l);
        if (k == "DOWN") {
            tapList.add(l);
            timeList.add(elapsedSeconds);
        }
    }


    private void addDataToFirebase() {
        Log.d(FINGER_TAPPING, "Dodano do bazy danych");

        Log.d(FINGER_TAPPING, "Tap right: " + tapResultsRight.size());
        Log.d(FINGER_TAPPING, "Tap left: " + tapResultsLeft.size());
        Log.d(FINGER_TAPPING, "Accel right: " + accelResultsLeft.size());
        Log.d(FINGER_TAPPING, "Accel left: " + accelResultsRight.size());


        Log.i(FINGER_TAPPING, FINGER_TAPPING);
        DocumentReference testData = db.collection("users")
                .document(Objects.requireNonNull(auth.getUid()))
                .collection("testsHistory")
                .document(FINGER_TAPPING)
                .collection("testDates")
                .document(formatter.format(date));

        Map<String, Object> dataMed = new HashMap<>();
        dataMed.put("hoursSinceLastMed", hourSinceLastDrug);
        testData.set(dataMed);

        Map<String, Object> dataLeft = new HashMap<>();
        Map<String, Object> dataRight = new HashMap<>();
        dataLeft.put("accel", accelResultsLeft);
        dataLeft.put("data", tapResultsLeft);
        dataRight.put("accel", accelResultsRight);
        dataRight.put("data", tapResultsRight);

        testData.collection("LEFT").document("testData").set(dataLeft);
        testData.collection("RIGHT").document("testData").set(dataRight);
    }


    private void init() {
        title = (TextView) findViewById(R.id.connect_title);
        startBtn = (Button) findViewById(R.id.startBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        rightBtn = (Button) findViewById(R.id.rightBtn);
        leftBtn = (Button) findViewById(R.id.leftBtn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_fingerTapping);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
        tapSpace = (LinearLayout) findViewById(R.id.tapSpace2);
        tapCounter = (TextView) findViewById(R.id.tapsCounter);
        timeCounterTextView = findViewById(R.id.textViewTime);
    }

    @SuppressLint("DefaultLocale")
    private String hmsTimeFormatter(long milliSeconds) {
        return String.format("%02d",
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

    }

}
