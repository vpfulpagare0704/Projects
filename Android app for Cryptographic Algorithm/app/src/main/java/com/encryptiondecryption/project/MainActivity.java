package com.encryptiondecryption.project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.encryptiondecryption.project.SplashIntro.StepperWizardColor;
import com.google.android.material.textfield.TextInputEditText;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    public static final int RequestPermissionCode = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int REQUEST = 112;
    String vigenerkey;
    String encryptedAlgorithm, decryptedAlgorithm, encryptedString;
    Button btn_encryption, btn_decryption, btn_send, btn_decryption_message;
    Spinner encryption_spinner, decryption_spinner;
    ImageView imgpickcontact;
    TextInputEditText edt_mobile, edt_message, edt_decyption_message, edt_key, edt_decryption_key;
    TextView final_decrypted_text, tvclear, tvhelp;
    Intent intent;
    LinearLayout decryptionLayout, encryptionLayout;

    String[] Options = {"Sim 1", "Sim 2"};
    AlertDialog.Builder window;
    private String KeyWord = new String();

    //Caesar Cipher
    private String Key = new String();
    private char matrix_arr[][] = new char[5][5];

    //RailFence
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("otp")) {
                final String message = intent.getStringExtra("message");
                final String sender = intent.getStringExtra("Sender");
//                otpOnlyTextView.setText(message.replaceAll("\\D+", ""));
//                fullmessageTextView.setText(sender + " : " + message);
                edt_decyption_message.setText(message);
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                Log.e("OTP MESSSGE", message);
            }
        }
    };

    // This function generates the key in
// a cyclic manner until it's length isi'nt
// equal to the length of original text
    static String generateKey(String str, String key) {
        int x = str.length();

        for (int i = 0; ; i++) {
            if (x == i)
                i = 0;
            if (key.length() == str.length())
                break;
            key += (key.charAt(i));
        }
        return key;
    }


    // This function returns the encrypted text
// generated with the help of the key
    static String cipherText(String str, String key) {
        String cipher_text = "";

        for (int i = 0; i < str.length(); i++) {
            // converting in range 0-25
            if (str.charAt(i)==' '){
                cipher_text+= " ";
            }
            else {
                int x = (str.charAt(i) + key.charAt(i)) % 26;

                // convert into alphabets(ASCII)
                x += 'A';

                cipher_text += (char) (x);
            }
        }
        return cipher_text;
    }

    // This function decrypts the encrypted text
// and returns the original text
    static String originalText(String cipher_text, String key) {
        String orig_text = "";

        for (int i = 0; i < cipher_text.length() &&
                i < key.length(); i++) {
            if (cipher_text.charAt(i)==' '){
                orig_text+=' ';
            }
            else {
                // converting in range 0-25
                int x = (cipher_text.charAt(i) -
                        key.charAt(i) + 26) % 26;

                // convert into alphabets(ASCII)
                x += 'A';
                orig_text += (char) (x);
            }
        }
        return orig_text;
    }

    // This function will convert the lower case character to Upper case
    static String LowerToUpper(String s) {
        StringBuffer str = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLowerCase(s.charAt(i))) {
                str.setCharAt(i, Character.toUpperCase(s.charAt(i)));
            }
        }
        s = str.toString();
        return s;
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        decryptionLayout = findViewById(R.id.decryptionLayout);
        encryptionLayout = findViewById(R.id.encryptionLayout);
        btn_send = findViewById(R.id.btn_send);
        edt_mobile = findViewById(R.id.edt_mobile);
        edt_message = findViewById(R.id.edt_message);
        edt_key = findViewById(R.id.edt_key);
        edt_decyption_message = findViewById(R.id.edt_decyption_message);
        edt_decryption_key = findViewById(R.id.edt_decryption_key);
        edt_decyption_message = findViewById(R.id.edt_decyption_message);
        final_decrypted_text = findViewById(R.id.final_decrypted_text);
        btn_encryption = findViewById(R.id.btn_encryption);
        btn_decryption = findViewById(R.id.btn_decryption);
        tvclear = findViewById(R.id.tvclear);
        btn_decryption_message = findViewById(R.id.btn_decryption_message);
        encryption_spinner = (Spinner) findViewById(R.id.encryption_spinner);
        decryption_spinner = (Spinner) findViewById(R.id.decryption_spinner);

        tvclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });
        tvhelp = findViewById(R.id.tvhelp);
        tvhelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StepperWizardColor.class));
            }
        });

        window = new AlertDialog.Builder(this);
        window.setTitle("Select Sim");

        List<String> weightList = new ArrayList<String>();
        weightList.add("Select Algorithm");
        weightList.add("Caesar Cipher Algorithm");
        weightList.add("Blowfish Cipher Algorithm");
        weightList.add("Vigenere Cipher Algorithm");
        weightList.add("Rail Fence Cipher Algorithm");

        ArrayAdapter<String> adapterWeight = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, weightList);
        adapterWeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encryption_spinner.setAdapter(adapterWeight);
        decryption_spinner.setAdapter(adapterWeight);


        encryption_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                String items = encryption_spinner.getSelectedItem().toString();
                Log.i("Selected item : ", items);
                if (encryption_spinner.getSelectedItem().toString().equals("Caesar Cipher Algorithm")) {
                    edt_key.setText("");
                    edt_key.setHint("Enter Number Key Only");
                    edt_key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                    edt_decryption_key.setText("");
                    edt_decryption_key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);

                } else if (encryption_spinner.getSelectedItem().toString().equals("Rail Fence Cipher Algorithm")) {
                    edt_key.setText("");
                    edt_key.setHint("Enter Number Key Only");
                    edt_key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                    edt_decryption_key.setText("");
                    edt_decryption_key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);

                } else if (encryption_spinner.getSelectedItem().toString().equals("Blowfish Cipher Algorithm")) {
                    edt_key.setText("");
                    edt_key.setHint("Enter Number or alphabets or special symbols Key");
                    edt_key.setInputType(InputType.TYPE_CLASS_TEXT);
                    edt_decryption_key.setText("");
                    edt_decryption_key.setInputType(InputType.TYPE_CLASS_TEXT);

                } else if (encryption_spinner.getSelectedItem().toString().equals("Vigenere Cipher Algorithm")) {
                    edt_key.setText("");
                    edt_key.setHint("Enter Capital Key Only");
                    edt_key.setInputType(InputType.TYPE_CLASS_TEXT);
                    edt_decryption_key.setText("");
                    edt_decryption_key.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        decryption_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                String items = decryption_spinner.getSelectedItem().toString();
                Log.i("Selected item : ", items);
                if (decryption_spinner.getSelectedItem().toString().equals("Caesar Cipher Algorithm")) {
                    edt_key.setText("");
                    edt_decryption_key.setHint("Enter Number Key Only");
                    edt_key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                    edt_decryption_key.setText("");
                    edt_decryption_key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);

                } else if (decryption_spinner.getSelectedItem().toString().equals("Rail Fence Cipher Algorithm")) {
                    edt_key.setText("");
                    edt_decryption_key.setHint("Enter Number Key Only");
                    edt_key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                    edt_decryption_key.setText("");
                    edt_decryption_key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);

                } else if (decryption_spinner.getSelectedItem().toString().equals("Blowfish Cipher Algorithm")) {
                    edt_key.setText("");
                    edt_decryption_key.setHint("Enter Number or alphabets or special symbols Key");
                    edt_key.setInputType(InputType.TYPE_CLASS_TEXT);
                    edt_decryption_key.setText("");
                    edt_decryption_key.setInputType(InputType.TYPE_CLASS_TEXT);

                } else if (decryption_spinner.getSelectedItem().toString().equals("Vigenere Cipher Algorithm")) {
                    edt_key.setText("");
                    edt_decryption_key.setHint("Enter Capital Key Only");
                    edt_key.setInputType(InputType.TYPE_CLASS_TEXT);
                    edt_decryption_key.setText("");
                    edt_decryption_key.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        imgpickcontact = findViewById(R.id.imgpickcontact);
        imgpickcontact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnableRuntimePermission();
            }
        });
        decryptionLayout.setVisibility(View.GONE);
        encryptionLayout.setVisibility(View.VISIBLE);
        btn_encryption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encryptionLayout.setVisibility(View.VISIBLE);
                decryptionLayout.setVisibility(View.GONE);
                btn_encryption.setTextColor(Color.parseColor("#ffffff"));
                btn_decryption.setTextColor(Color.parseColor("#B1B0B0"));
            }
        });

        btn_decryption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decryptionLayout.setVisibility(View.VISIBLE);
                encryptionLayout.setVisibility(View.GONE);
                btn_decryption.setTextColor(Color.parseColor("#ffffff"));
                btn_encryption.setTextColor(Color.parseColor("#B1B0B0"));
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                encryptedAlgorithm = encryption_spinner.getSelectedItem().toString();

                if (edt_mobile.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter Mobile Number", Toast.LENGTH_SHORT).show();
                } else if (edt_message.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter Message", Toast.LENGTH_SHORT).show();
                } else if (encryptedAlgorithm.equals("Select Algorithm")) {
                    Toast.makeText(MainActivity.this, "Please Select Algorithm", Toast.LENGTH_SHORT).show();
                } else if (edt_key.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter Key", Toast.LENGTH_SHORT).show();
                } else {

                    int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS);

                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

                        try {
                            if (Build.VERSION.SDK_INT >= 23) {
                                String[] PERMISSIONS = {android.Manifest.permission.READ_PHONE_STATE};
                                if (!hasPermissions(getApplicationContext(), PERMISSIONS)) {
                                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST);

                                } else {
                                    //Do here

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Select Sim");
                                    builder.setMessage("Do you want to send SMS ?")
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                                public void onClick(DialogInterface dialog, int id) {
                                                    try {

                                                        if (encryptedAlgorithm.equals("Caesar Cipher Algorithm")) {
                                                            int dKey = Integer.parseInt(edt_key.getText().toString());
                                                            while (dKey > 26) {
                                                                dKey = dKey / 26;
                                                            }
                                                            edt_decryption_key.setText(dKey + "");
                                                            Log.d("encryptKey", dKey + "");
                                                            encryptedString = CaesarCipherAlgorithm.caesarCipherEncryption(dKey, edt_message.getText().toString());
                                                            sendSMS();
                                                        } else if (encryptedAlgorithm.equals("Rail Fence Cipher Algorithm")) {
                                                            int msg = edt_message.getText().toString().length();
                                                            int key = Integer.parseInt(edt_key.getText().toString());
                                                            if(key >= msg ){
//                                                                key = key - msg;
                                                                edt_key.setText("");
                                                                Toast.makeText(MainActivity.this, "Key Value Should Be Less than "+msg, Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                encryptedString = RailFenceCipherAlgorithm.railFenceEncryption(key, edt_message.getText().toString());
                                                                sendSMS();
                                                            }
                                                        } else if (encryptedAlgorithm.equals("Blowfish Cipher Algorithm")) {
                                                            encryptedString = encryptCT(edt_message.getText().toString(), edt_key.getText().toString());
                                                                sendSMS();
                                                        } else if (encryptedAlgorithm.equals("Vigenere Cipher Algorithm")) {

                                                            String str = LowerToUpper(edt_message.getText().toString());
                                                            String keyword = LowerToUpper(edt_key.getText().toString());

                                                            vigenerkey = generateKey(str, keyword);
                                                            encryptedString = cipherText(str, vigenerkey);
                                                            sendSMS();
                                                        }

                                                    } catch (
                                                            Exception e) {
                                                        Toast.makeText(MainActivity.this, e.toString() + "", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }

                            } else {
                                //Do here
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "SMS failed, please try again.",
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                MY_PERMISSIONS_REQUEST_SEND_SMS);
                    }
                }
            }
        });

        btn_decryption_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    decryptedAlgorithm = decryption_spinner.getSelectedItem().toString();
                    if (edt_decyption_message.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please Enter Message", Toast.LENGTH_SHORT).show();
                    } else if (decryptedAlgorithm.equals("Select Algorithm")) {
                        Toast.makeText(MainActivity.this, "Please Select Algorithm", Toast.LENGTH_SHORT).show();
                    } else if (edt_decryption_key.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please Enter Key", Toast.LENGTH_SHORT).show();
                    } else {
                        if (decryptedAlgorithm.equals("Caesar Cipher Algorithm")) {
                            int dKey = Integer.parseInt(edt_decryption_key.getText().toString());
                            while (dKey > 26) {
                                dKey = dKey / 26;
                            }
                            final_decrypted_text.setText("Decrypt Message : \n" + CaesarCipherAlgorithm.caesarCipherDecryption(dKey, edt_decyption_message.getText().toString()));
                        } else if (decryptedAlgorithm.equals("Rail Fence Cipher Algorithm")) {

                            int msg = edt_decyption_message.getText().toString().length();
                            int key = Integer.parseInt(edt_decryption_key.getText().toString());
                            if(key >= msg ){
                                key =    key - msg;
                                edt_decryption_key.setText("");
                                Toast.makeText(MainActivity.this, "Key Value Should Be Less than "+msg, Toast.LENGTH_SHORT).show();
                            }
                            //else
                                final_decrypted_text.setText("Decrypt Message : \n" + RailFenceCipherAlgorithm.railFenceDecryption(key, edt_decyption_message.getText().toString()));
//                            final_decrypted_text.setText("Decrypt Message : \n" + decryptCT(edt_decyption_message.getText().toString(),edt_decryption_key.getText().toString() ));

                        } else if (decryptedAlgorithm.equals("Blowfish Cipher Algorithm")) {
                            final_decrypted_text.setText("Decrypt Message : \n" + decryptCT(edt_decryption_key.getText().toString(),edt_decyption_message.getText().toString()));
                        } else if (decryptedAlgorithm.equals("Vigenere Cipher Algorithm")) {
                            final_decrypted_text.setText("Decrypt Message : \n" + originalText(edt_decyption_message.getText().toString(), edt_decryption_key.getText().toString() + edt_decryption_key.getText().toString() + edt_decryption_key.getText().toString() + edt_decryption_key.getText().toString() + edt_decryption_key.getText().toString() + edt_decryption_key.getText().toString() + edt_decryption_key.getText().toString() + edt_decryption_key.getText().toString() + edt_decryption_key.getText().toString()).toLowerCase());
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                }
            }

        });

        checkAndRequestPermissions();
    }

    public void sendSMS(){


        final String SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";
        final PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                SENT), 0);
        final PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        window.setItems(Options, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.d("data:" + edt_mobile.getText().toString(), " " + encryptedString);
                if (which == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        {
                            final PendingIntent localPendingIntent1 = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(SENT), 0);
                            final PendingIntent localPendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(DELIVERED), 0);

                            if (Build.VERSION.SDK_INT >= 22) {
                                SubscriptionManager subscriptionManager = getSystemService(SubscriptionManager.class);

                                @SuppressLint("MissingPermission") SubscriptionInfo subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);
                                if (subscriptionInfo != null || subscriptionInfo != null) {
                                    try {
                                        SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.getSubscriptionId()).sendTextMessage(edt_mobile.getText().toString(), null, encryptedAlgorithm + "\n" + encryptedString, localPendingIntent1, localPendingIntent2);

                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Sim 1 not available", Toast.LENGTH_SHORT).show();
                                }
                            }
                            try {
                                SmsManager.getSmsManagerForSubscriptionId(0).sendTextMessage(edt_mobile.getText().toString(), null, encryptedString, localPendingIntent1, localPendingIntent2);

                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, e.toString() + "", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_LONG).show();

                        int secondsDelayed = 1;
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                Intent intent1 = new Intent(MainActivity.this, MainActivity.class);
//                                                                                startActivity(intent1);
                            }
                        }, secondsDelayed * 800);
                    } else {
                        SmsManager.getDefault().sendTextMessage(edt_mobile.getText().toString(), null, encryptedString, sentPI, deliveredPI);

                        int secondsDelayed = 1;
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                Intent intent1 = new Intent(MainActivity.this, MainActivity.class);
//                                                                                startActivity(intent1);
                            }
                        }, secondsDelayed * 800);
                    }
                } else if (which == 1) {
                    final PendingIntent localPendingIntent1 = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(SENT), 0);
                    final PendingIntent localPendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(DELIVERED), 0);

                    if (Build.VERSION.SDK_INT >= 22) {
                        SubscriptionManager subscriptionManager = getSystemService(SubscriptionManager.class);

                        @SuppressLint("MissingPermission") SubscriptionInfo subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);
                        if (subscriptionInfo != null || subscriptionInfo != null) {
                            SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.getSubscriptionId()).sendTextMessage(edt_mobile.getText().toString(), null,encryptedAlgorithm + "\n" + encryptedString, localPendingIntent1, localPendingIntent2);
                        } else {
                            Toast.makeText(MainActivity.this, "Sim 2 not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                    SmsManager.getSmsManagerForSubscriptionId(0).sendTextMessage(edt_mobile.getText().toString(), null, encryptedAlgorithm + "\n" +encryptedString, localPendingIntent1, localPendingIntent2);

                    Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_LONG).show();
                    int secondsDelayed = 1;
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            Intent intent1 = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent1);
                        }
                    }, secondsDelayed * 800);

                } else {
                    //theres an error in what was selected
                    Toast.makeText(getApplicationContext(), "Hmmm I messed up. I detected that you clicked on : " + which + "?", Toast.LENGTH_LONG).show();

                }
            }
        });
        window.show();
    }
    public void setKey(String k) {
        String K_adjust = new String();
        boolean flag = false;
        K_adjust = K_adjust + k.charAt(0);
        for (int i = 1; i < k.length(); i++) {
            for (int j = 0; j < K_adjust.length(); j++) {
                if (k.charAt(i) == K_adjust.charAt(j)) {
                    flag = true;
                }
            }
            if (flag == false)
                K_adjust = K_adjust + k.charAt(i);
            flag = false;
        }
        KeyWord = K_adjust;
    }

    public void KeyGen() {
        boolean flag = true;
        char current;
        Key = KeyWord;
        for (int i = 0; i < 26; i++) {
            current = (char) (i + 97);
            if (current == 'j')
                continue;
            for (int j = 0; j < KeyWord.length(); j++) {
                if (current == KeyWord.charAt(j)) {
                    flag = false;
                    break;
                }
            }
            if (flag)
                Key = Key + current;
            flag = true;
        }
        System.out.println(Key);
        matrix();
    }

//Vigenere

    private void matrix() {
        int counter = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                matrix_arr[i][j] = Key.charAt(counter);
                System.out.print(matrix_arr[i][j] + " ");
                counter++;
            }
            System.out.println();
        }
    }

    private String format(String old_text) {
        int i = 0;
        int len = 0;
        String text = new String();
        len = old_text.length();
        for (int tmp = 0; tmp < len; tmp++) {
            if (old_text.charAt(tmp) == 'j') {
                text = text + 'i';
            } else
                text = text + old_text.charAt(tmp);
        }
        len = text.length();
        for (i = 0; i < len; i = i + 2) {
            if (text.charAt(i + 1) == text.charAt(i)) {
                text = text.substring(0, i + 1) + 'x' + text.substring(i + 1);
            }
        }
        return text;
    }

    private String[] Divid2Pairs(String new_string) {
        String Original = format(new_string);
        int size = Original.length();
        if (size % 2 != 0) {
            size++;
            Original = Original + 'x';
        }
        String x[] = new String[size / 2];
        int counter = 0;
        for (int i = 0; i < size / 2; i++) {
            x[i] = Original.substring(counter, counter + 2);
            counter = counter + 2;
        }
        return x;
    }

    public int[] GetDiminsions(char letter) {
        int[] key = new int[2];
        if (letter == 'j')
            letter = 'i';
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (matrix_arr[i][j] == letter) {
                    key[0] = i;
                    key[1] = j;
                    break;
                }
            }
        }
        return key;
    }

    public String encryptMessage(String Source) {
        String src_arr[] = Divid2Pairs(Source);
        String Code = new String();
        char one;
        char two;
        int part1[] = new int[2];
        int part2[] = new int[2];
        for (int i = 0; i < src_arr.length; i++) {
            one = src_arr[i].charAt(0);
            two = src_arr[i].charAt(1);
            part1 = GetDiminsions(one);
            part2 = GetDiminsions(two);
            if (part1[0] == part2[0]) {
                if (part1[1] < 4)
                    part1[1]++;
                else
                    part1[1] = 0;
                if (part2[1] < 4)
                    part2[1]++;
                else
                    part2[1] = 0;
            } else if (part1[1] == part2[1]) {
                if (part1[0] < 4)
                    part1[0]++;
                else
                    part1[0] = 0;
                if (part2[0] < 4)
                    part2[0]++;
                else
                    part2[0] = 0;
            } else {
                int temp = part1[1];
                part1[1] = part2[1];
                part2[1] = temp;
            }
            Code = Code + matrix_arr[part1[0]][part1[1]]
                    + matrix_arr[part2[0]][part2[1]];
        }
        return Code;
    }

    public String decryptMessage(String Code) {
        String Original = new String();
        String src_arr[] = Divid2Pairs(Code);
        char one;
        char two;
        int part1[] = new int[2];
        int part2[] = new int[2];
        for (int i = 0; i < src_arr.length; i++) {
            one = src_arr[i].charAt(0);
            two = src_arr[i].charAt(1);
            part1 = GetDiminsions(one);
            part2 = GetDiminsions(two);
            if (part1[0] == part2[0]) {
                if (part1[1] > 0)
                    part1[1]--;
                else
                    part1[1] = 4;
                if (part2[1] > 0)
                    part2[1]--;
                else
                    part2[1] = 4;
            } else if (part1[1] == part2[1]) {
                if (part1[0] > 0)
                    part1[0]--;
                else
                    part1[0] = 4;
                if (part2[0] > 0)
                    part2[0]--;
                else
                    part2[0] = 4;
            } else {
                int temp = part1[1];
                part1[1] = part2[1];
                part2[1] = temp;
            }
            Original = Original + matrix_arr[part1[0]][part1[1]]
                    + matrix_arr[part2[0]][part2[1]];
        }
        return Original;
    }

    public void EnableRuntimePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.READ_CONTACTS)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        } else {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);
            intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 7);
        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    EnableRuntimePermission();
                }

                break;
        }
    }

    @Override
    public void onActivityResult(int RequestCode, int ResultCode, Intent ResultIntent) {

        super.onActivityResult(RequestCode, ResultCode, ResultIntent);

        switch (RequestCode) {

            case (7):
                if (ResultCode == Activity.RESULT_OK) {

                    Uri uri;
                    Cursor cursor1, cursor2;
                    String TempNameHolder, TempNumberHolder, TempContactID, IDresult = "";
                    int IDresultHolder;

                    uri = ResultIntent.getData();

                    cursor1 = getContentResolver().query(uri, null, null, null, null);

                    if (cursor1.moveToFirst()) {

                        TempNameHolder = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        TempContactID = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));

                        IDresult = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        IDresultHolder = Integer.valueOf(IDresult);

                        if (IDresultHolder == 1) {

                            cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + TempContactID, null, null);

                            while (cursor2.moveToNext()) {

                                TempNumberHolder = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

//                                edtincome.setText(TempNameHolder);

                                edt_mobile.setText(TempNumberHolder);

                            }
                        }

                    }
                }
                break;
        }
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            int receiveSMS = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS);
            int readSMS = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS);
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (receiveSMS != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
            }
            if (readSMS != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.READ_SMS);
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
                return false;
            }
            return true;
        }
        return true;

    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }

//Blowfish

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encryptCT(String password, String key) throws
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, UnsupportedEncodingException {
        byte[] KeyData = key.getBytes();
        SecretKeySpec KS = new SecretKeySpec(KeyData, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, KS);
        String encryptedtext = Base64.getEncoder().
                encodeToString(cipher.doFinal(password.getBytes("UTF-8")));
        return encryptedtext;
    }

    public static String decryptCT(String key, String encryptedtext ) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        byte[] KeyData = key.getBytes();
        SecretKeySpec KS = new SecretKeySpec(KeyData, "Blowfish");
        byte[] ecryptedtexttobytes = Base64.getDecoder().
                decode(encryptedtext);
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, KS);
        byte[] decrypted = cipher.doFinal(ecryptedtexttobytes);
        String decryptedString =
                new String(decrypted, Charset.forName("UTF-8"));
        return decryptedString;

    }

    public static char RandomAlpha() {
        //generate random alpha for null space
        Random r = new Random();
        return (char)(r.nextInt(26) + 'a');
    }

    public static int[] arrangeKey(String key) {
        //arrange position of grid
        String[] keys = key.split("");
        Arrays.sort(keys);
        int[] num = new int[key.length()];
        for (int x = 0; x < keys.length; x++) {
            for (int y = 0; y < key.length(); y++) {
                if (keys[x].equals(key.charAt(y) + "")) {
                    num[y] = x;
                    break;
                }
            }
        }

        return num;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

        super.onBackPressed();
    }

}
