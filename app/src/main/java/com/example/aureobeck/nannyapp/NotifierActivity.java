package com.example.aureobeck.nannyapp;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class NotifierActivity extends AppCompatActivity {

    // ******   Views  *****

    private static RelativeLayout relativeLayoutConnection;
    private static Switch notificationSwitch;
    private static Switch switchConnection;
    private static TextView textViewSwitch;
    private static TextView textViewStatus;
    private static ImageView imageViewStatus;
    private static ChildEventListener childEventListener;


    // ******   Variables  *****
    Context ctx = this;
    private static Firebase firebaseRef;

    // ******   Inicialization Methods  *****

    // TODO: STRING
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // *****   Inicialize Interface   ******
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificator);

        // *****   Action Bar   *****
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notificador");

        // *****   Firebase   *****
        Firebase.setAndroidContext(this);

        // *****   Inicialize Controls  *****
        findViews();
        configureControls();

        // *****   Events   *****
        onConnectionSwitchPressed();
        onTextViewConnectionClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void findViews() {
        relativeLayoutConnection = (RelativeLayout) findViewById(R.id.relativeLayoutConnection);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        notificationSwitch = (Switch) findViewById(R.id.switchNotification);
        imageViewStatus = (ImageView) findViewById(R.id.imageViewStatus);
        switchConnection = (Switch) findViewById(R.id.switchConnection);
        textViewSwitch = (TextView) findViewById(R.id.textViewSwitch);
    }

    // ******   Config Methods  *****

    // TODO: String
    private void configureControls() {
        setChildEventListener();
        if (getSharedPreferencesFirebaseReadId().equals("")) {
            setControlsNoConnection();
        } else {
            // TODO: Check Connection
            connectFirebase();
            setControlsConnectionOk();
        }
    }

    // ******   Action Methods  *****

    private void onTextViewConnectionClick() {
        textViewSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSharedPreferencesFirebaseReadId().equals("")) {
                    openNewConnectionDialog();
                } else {
                    openEditConnectionDialog();
                }
            }
        });
    }

    public void onConnectionSwitchPressed() {
        switchConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!switchConnection.isChecked()) {
                    setControlsNoConnection();
                } else if (getSharedPreferencesFirebaseReadId().equals("")) {
                    openNewConnectionDialog();
                } else {
                    connectFirebase();
                    setControlsConnectionOk();
                }
            }
        });
    }

    private void setControlsNoConnection() {
        textViewStatus.setText("Sem Conexão!");
        imageViewStatus.setBackgroundResource(R.mipmap.dead_icon);
        relativeLayoutConnection.setBackgroundResource(R.drawable.custom_border_red);
        textViewSwitch.setText("Sem Conexão");
        switchConnection.setChecked(false);
    }

    private void setControlsConnectionOk() {
        textViewStatus.setText("Está tudo bem!");
        imageViewStatus.setBackgroundResource(R.mipmap.happy_icon);
        relativeLayoutConnection.setBackgroundResource(R.drawable.custom_border_green);
        switchConnection.setChecked(true);

        Integer readCode = 54321 - Integer.parseInt(getSharedPreferencesFirebaseReadId());
        textViewSwitch.setText("Conectado - Id: " + readCode);
    }

    // TODO: Disconnect Firebase
    private void connectFirebase() {
        firebaseRef = new Firebase("https://nanny-app-205da.firebaseio.com/").child("clients").child(getSharedPreferencesFirebaseReadId());
        onFirebaseChildEvent();
    }

    private void openEditConnectionDialog() {
        final Dialog dialogNewConnection = new Dialog(ctx);
        dialogNewConnection.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNewConnection.setContentView(R.layout.dialog_one_button_edit_text);
        dialogNewConnection.show();
        dialogNewConnection.setCancelable(false);

        final TextView textViewTitle = (TextView) dialogNewConnection.findViewById(R.id.textViewTitle);
        textViewTitle.setText("Editar Conexão");

        final TextView textViewDescription = (TextView) dialogNewConnection.findViewById(R.id.textViewDescription);
        textViewDescription.setText("Por favor digite o código de conexão");

        final EditText editTextCode = (EditText) dialogNewConnection.findViewById(R.id.editText);
        Integer currentId = 54321 - Integer.parseInt(getSharedPreferencesFirebaseReadId());
        editTextCode.setText(currentId.toString());

        final Button buttonOk = (Button) dialogNewConnection.findViewById(R.id.buttonOk);
        buttonOk.setText("Ok");
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextCode.getText().toString().equals("")) {
                    showToast("Por favor, insira um código válido!", 3000);
                } else {
                    Integer readCode = 54321 - Integer.parseInt(editTextCode.getText().toString());
                    saveSharedPreferencesFirebaseReadId(readCode.toString());
                    setControlsConnectionOk();
                    connectFirebase();
                    dialogNewConnection.cancel();
                    hideKeyboard();
                }
            }
        });

        final Button buttonCancel = (Button) dialogNewConnection.findViewById(R.id.buttonCancel);
        buttonCancel.setText("Cancelar");
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNewConnection.cancel();
            }
        });
    }

    private void openNewConnectionDialog() {
        final Dialog dialogNewConnection = new Dialog(ctx);
        dialogNewConnection.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNewConnection.setContentView(R.layout.dialog_one_button_edit_text);
        dialogNewConnection.show();
        dialogNewConnection.setCancelable(false);

        final TextView textViewTitle = (TextView) dialogNewConnection.findViewById(R.id.textViewTitle);
        textViewTitle.setText("Nova Conexão");

        final TextView textViewDescription = (TextView) dialogNewConnection.findViewById(R.id.textViewDescription);
        textViewDescription.setText("Por favor digite o código de conexão");

        final EditText editTextCode = (EditText) dialogNewConnection.findViewById(R.id.editText);
        editTextCode.setHint("Código");

        final Button buttonOk = (Button) dialogNewConnection.findViewById(R.id.buttonOk);
        buttonOk.setText("Ok");
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextCode.getText().toString().equals("")) {
                    showToast("Por favor, insira um código válido!", 3000);
                } else {
                    Integer readCode = 54321 - Integer.parseInt(editTextCode.getText().toString());
                    saveSharedPreferencesFirebaseReadId(readCode.toString());
                    connectFirebase();
                    setControlsConnectionOk();
                    dialogNewConnection.cancel();
                    hideKeyboard();
                }
            }
        });

        final Button buttonCancel = (Button) dialogNewConnection.findViewById(R.id.buttonCancel);
        buttonCancel.setText("Cancelar");
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                dialogNewConnection.cancel();
            }
        });
    }

    private void configControlsBabyNotCrying() {
        textViewStatus.setText("Está tudo bem!");
        imageViewStatus.setBackgroundResource(R.mipmap.happy_icon);
    }

    private void configControlsBabyCrying() {
        textViewStatus.setText("Bebê Chorando!");
        imageViewStatus.setBackgroundResource(R.mipmap.sad_icon);
        openNotifierDialog();
    }

    // ******   Firebase Methods  *****

    private String getSharedPreferencesFirebaseReadId() {
        return readFromPreferences(ctx, "firebaseCodeRead", "", "userConfig");
    }

    private void saveSharedPreferencesFirebaseReadId (String firebaseId) {
        saveToPreferences(ctx, "firebaseCodeRead", firebaseId, "userConfig");
    }

    private void onFirebaseChildEvent() {
        firebaseRef.removeEventListener(childEventListener);
        firebaseRef.addChildEventListener(childEventListener);
    }

    private void setChildEventListener() {
       childEventListener =  new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String valueRetrived = dataSnapshot.getValue(String.class);
                if (valueRetrived.equals("1")) {
                    configControlsBabyCrying();
                } else {
                    configControlsBabyNotCrying();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
    }

    // ******   General Methods  *****

    // TODO: String
    private void openNotifierDialog() {
        final Dialog dialogNotifier = new Dialog(NotifierActivity.this);
        dialogNotifier.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNotifier.setContentView(R.layout.dialog_one_button);
        dialogNotifier.show();
        dialogNotifier.setCancelable(false);

        final TextView textViewTitle = (TextView) dialogNotifier.findViewById(R.id.textViewTitle);
        textViewTitle.setText("Atenção");

        final TextView textViewDescription = (TextView) dialogNotifier.findViewById(R.id.textViewDescription);
        textViewDescription.setText("Seu bebê está chorando!!");

        final Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        startVibrator(vibrator);

        final Button buttonOk = (Button) dialogNotifier.findViewById(R.id.buttonOk);
        buttonOk.setText("Ok");
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVibrator(vibrator);
                configControlsBabyNotCrying();
                resetFirebaseValue();
                dialogNotifier.cancel();
            }
        });

    }

    private void resetFirebaseValue() {
        firebaseRef.setValue("0");
    }

    private void startVibrator(Vibrator vibrator) {
        long[] pattern = {0, 2000, 1000};
        vibrator.vibrate(pattern, 0);
    }

    private void stopVibrator(Vibrator vibrator) {
        vibrator.cancel();
    }

    public void showToast(String message, Integer length) {
        final Toast toast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, length);
    }

    private void saveToPreferences(Context context, String preferenceName, String preferenceValue, String preferenceFile) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    private String readFromPreferences(Context context, String preferenceName, String defaultValue, String preferenceFile) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
