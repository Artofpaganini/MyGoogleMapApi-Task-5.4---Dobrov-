package by.andresen.intern.dobrov.mygooglemaps;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String ERROR_MESSAGE = "Have some problem with connection to Google Service, please check logs ";
    private Button buttonMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonMap = findViewById(R.id.buttom_map);

        if (isServicesOK()) {
            init();
        } else {
            Toast.makeText(this, ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }
    }

    private void init() {
        buttonMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);

            startActivity(intent);
        });
    }

    private boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: CHECK GOOGLE SERVICES VERSION");
        int available = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: SERVICES  IS OK!!!");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK:  WE GOT ERROR IN SERVICES");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);

        } else {
            Toast.makeText(this, "YOU CAN'T MAKE MAP REQUEST", Toast.LENGTH_SHORT).show();

        }
        return false;
    }

}