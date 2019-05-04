package work.ionut.trafficlights;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static Boolean status = false;
    static final boolean[] checked = new boolean[]{true, true, true, true};

    static Handler handler=null;

    static Boolean off = false;
    static Boolean check = false;
    private static ImageView[] lights;
    private Switch[] switches;

    public static int[] light_status = new int[]{0, 0, 0, 0};

    BluetoothConnectionService mBluetoothConnection;
    private static final UUID uuid =
            UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    public BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final Intent intent = getIntent();
        final String address = intent.getStringExtra(BluetoothActivity.EXTRA_ADDRESS);
        final BluetoothDevice device = BTAdapter.getRemoteDevice(address);

        Button update = findViewById(R.id.update);

        final EditText green_red = findViewById(R.id.green_red);
        final EditText yellow = findViewById(R.id.yellow);

        final FloatingActionButton fab = findViewById(R.id.fab);
        final ImageView light1 = findViewById(R.id.light1);
        final ImageView light2 = findViewById(R.id.light2);
        final ImageView light3 = findViewById(R.id.light3);
        final ImageView light4 = findViewById(R.id.light4);
        lights = new ImageView[]{light1, light2, light3, light4};

        final Switch switch1 = findViewById(R.id.switch1);
        final Switch switch2 = findViewById(R.id.switch2);
        final Switch switch3 = findViewById(R.id.switch3);
        final Switch switch4 = findViewById(R.id.switch4);

        switches = new Switch[]{switch1, switch2, switch3, switch4};

        for(int i=0;i<4;i++)
            switches[i].setClickable(false);

        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0b6623")));
        fab.setImageResource(R.drawable.ic_play_arrow_black_24dp);


        if(status)
        {
            Log.w("app", "status 1");

            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF2400")));
            fab.setImageResource(R.drawable.ic_stop_black_24dp);

            for(int i=0;i<4;i++) {
                switches[i].setClickable(true);
                if(!checked[i])
                    switches[i].setChecked(false);
            }
        }

        for(int i=0;i<4;i++)
            setLight(i, 0);

        mBluetoothConnection.startClient(device, uuid);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checked[0])
                {
                    Snackbar.make(buttonView, "Semaforul 1 a fost oprit.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    setLight(0, 2);
                    mBluetoothConnection.write("switch 0 0");
                } else {
                    Snackbar.make(buttonView, "Semaforul 1 a fost pornit.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    setLight(0, 1);
                    mBluetoothConnection.write("switch 0 1");
                }
                checked[0]=!checked[0];
                checkHandler();
            }
        });
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checked[1])
                {
                    Snackbar.make(buttonView, "Semaforul 2 a fost oprit.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    setLight(1, 2);
                    mBluetoothConnection.write("switch 1 0");
                } else {
                    Snackbar.make(buttonView, "Semaforul 2 a fost pornit.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    setLight(1, 1);
                    mBluetoothConnection.write("switch 1 1");
                }
                checked[1]=!checked[1];
                checkHandler();
            }
        });
        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checked[2])
                {
                    Snackbar.make(buttonView, "Semaforul 3 a fost oprit.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    setLight(2, 2);
                    mBluetoothConnection.write("switch 2 0");
                } else {
                    Snackbar.make(buttonView, "Semaforul 3 a fost pornit.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    setLight(2, 1);
                    mBluetoothConnection.write("switch 2 1");
                }
                checked[2]=!checked[2];
                checkHandler();
            }
        });
        switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checked[3])
                {
                    Snackbar.make(buttonView, "Semaforul 4 a fost oprit.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    setLight(3, 2);
                    mBluetoothConnection.write("switch 3 0");
                } else {
                    Snackbar.make(buttonView, "Semaforul 4 a fost pornit.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    setLight(3, 1);
                    mBluetoothConnection.write("switch 3 1");
                }
                checked[3]=!checked[3];
                checkHandler();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!status)
                {
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF2400")));
                    fab.setImageResource(R.drawable.ic_stop_black_24dp);
                    setLight(0, 1);
                    setLight(1, 1);
                    setLight(2, 3);
                    setLight(3, 3);

                    for(int i=0;i<4;i++)
                        switches[i].setClickable(true);

                    mBluetoothConnection.write("start");
                    Snackbar.make(view, "Semafoarele au fost pornite.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0b6623")));
                    fab.setImageResource(R.drawable.ic_play_arrow_black_24dp);

                    for(int i=0;i<4;i++) {
                        switches[i].setChecked(true);
                        checked[i] = true;
                        setLight(i, 0);
                        switches[i].setClickable(false);
                    }

                    if(handler!=null)
                        checkHandler();

                    mBluetoothConnection.write("stop");

                    Snackbar.make(view, "Semafoarele au fost oprite.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                status = !status;
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try  {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                } catch (Exception ignored) { }

                mBluetoothConnection.write("update "+green_red.getText()+" "+yellow.getText());
                Snackbar.make(view, "Datele au fost transmise.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
    }

    public void checkHandler()
    {
        if(handler!=null)
        {
            for(int i=0;i<4;i++)
                if(!checked[i])
                    return;
            Log.w("app", "NOT NULL");
            off=false;
            handler=null;
        } else
        {
            Log.w("app", "NULL");
            off = true;
            handler =  new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for(int i=0;i<4;i++)
                        if(!checked[i])
                        {
                            if(!check)
                                setLight(i, 0);
                            else
                                setLight(i, 2);
                        }
                    check=!check;

                    if (off)
                        handler.postDelayed(this, 1000);
                }
            }, 1000);
        }
    }

    public static void bluetoothUpdate(JSONArray arr){
        for (int i=0; i < arr.length(); i++) {
            try {
                light_status[i]=arr.getInt(i);
                if(checked[i])
                    setLight(i, light_status[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setLight(int l, int status)
    {
        switch(status) {
            case 0:
                lights[l].setImageResource(R.drawable.s0);
                break;
            case 1:
                lights[l].setImageResource(R.drawable.s1);
                break;
            case 2:
                lights[l].setImageResource(R.drawable.s2);
                break;
            case 3:
                lights[l].setImageResource(R.drawable.s3);
                break;
            default:
                lights[l].setImageResource(R.drawable.s0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.christmass)
        {
            mBluetoothConnection.write("christmass");
            return true;
        } else if (id == R.id.halt)
        {
            mBluetoothConnection.write("halt");
            return true;
        }else if (id == R.id.author)
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothConnection.write("disconnect");
    }
}
