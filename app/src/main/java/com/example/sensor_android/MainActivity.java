package com.example.sensor_android;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvX, tvY, tvZ, tvX2, tvY2, tvZ2, tvPosition;
    private boolean isAccelerometerAvailable, isNotFirstTime = false;
    private boolean isGyroscopeAvailable;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, gyroscopeSensor;
    private float currentX, currentY, currentZ, lastX, lastY, lastZ, xDiff, yDiff, zDiff;
    private final float shakeThreshold = 3f;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvX = findViewById(R.id.tv_valueX);
        tvY = findViewById(R.id.tv_valueY);
        tvZ = findViewById(R.id.tv_valueZ);

        tvX2 = findViewById(R.id.tv_valueX2);
        tvY2 = findViewById(R.id.tv_valueY2);
        tvZ2 = findViewById(R.id.tv_valueZ2);

        tvPosition = findViewById(R.id.tv_position);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (accelerometerSensor != null) {
                isAccelerometerAvailable = true;
            }
            if (gyroscopeSensor != null) {
                isGyroscopeAvailable = true;
            }
        } else {
            isAccelerometerAvailable = false;
            isGyroscopeAvailable = false;
            Toast.makeText(this, "Sensor service not detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String valueXStr = "X: " + sensorEvent.values[0];
            String valueYStr = "Y: " + sensorEvent.values[1];
            String valueZStr = "Z: " + sensorEvent.values[2];
            tvX.setText(valueXStr);
            tvY.setText(valueYStr);
            tvZ.setText(valueZStr);

            /////
            currentX = sensorEvent.values[0];
            currentY = sensorEvent.values[1];
            currentZ = sensorEvent.values[2];

            if (isNotFirstTime) {
                xDiff = Math.abs(lastX - currentX);
                yDiff = Math.abs(lastY - currentY);
                zDiff = Math.abs(lastZ - currentZ);

                if (xDiff > shakeThreshold || yDiff > shakeThreshold ||
                        zDiff > shakeThreshold) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(500);
                    }
                }
            }

            String positionStr;
            if (currentZ > 9.0) {
                positionStr = "Lying on the horizontal plane";
            } else if (currentZ < -5.0) {
                positionStr = "Lying face down";
            } else if (currentY > 9.0 || (currentY > 6.5 && currentZ < 7.0)) {
                positionStr = "Lying on the vertical plane";
            } else if (currentX > 9.0 || (currentX > 6.5 && currentZ < 7.0)) {
                positionStr = "Lying on the horizontal plane and facing to the left";
            } else if (currentX < -9.0 || (currentX < -6.5 && currentZ < 7.0)) {
                positionStr = "Lying on the horizontal plane and facing to the right";
            } else {
                positionStr = "is tilting...";
            }
            tvPosition.setText(positionStr);

            lastX = currentX;
            lastY = currentY;
            lastZ = currentZ;
            isNotFirstTime = true;
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            String valueXStr = "X: " + sensorEvent.values[0];
            String valueYStr = "Y: " + sensorEvent.values[1];
            String valueZStr = "Z: " + sensorEvent.values[2];
            tvX2.setText(valueXStr);
            tvY2.setText(valueYStr);
            tvZ2.setText(valueZStr);
            if (sensorEvent.values[2] > 0.5f) {
                getWindow().getDecorView().setBackgroundColor(Color.GREEN);
            } else if (sensorEvent.values[2] < -0.5f) {
                getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
            } else {
                getWindow().getDecorView().setBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAccelerometerAvailable) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (isGyroscopeAvailable) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isAccelerometerAvailable || isGyroscopeAvailable) {
            sensorManager.unregisterListener(this);
        }
    }
}