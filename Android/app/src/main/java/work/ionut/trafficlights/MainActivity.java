package work.ionut.trafficlights;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static Boolean status = false;
    static Boolean christmas_mode = false;
    static final boolean[] checked = new boolean[]{true, true, true, true};

    static Handler handler=null;
    static int x=0;
    static int y=1;

    static Boolean off = false;
    static Boolean check = false;
    static ImageView[] lights;
    static Switch[] switches;
    static FloatingActionButton fab;
    static FloatingActionButton bluetooth_btn;
    static EditText green_red;
    static EditText yellow;

    static int[] light_status = new int[]{0, 0, 0, 0};

    BluetoothConnectionService mBluetoothConnection;
    private static final UUID uuid =
            UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    public BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    public static Boolean bluetoothError=false;
    public static Boolean bluetoothDisconnect=false;

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

        green_red = findViewById(R.id.green_red);
        yellow = findViewById(R.id.yellow);
        fab = findViewById(R.id.fab);

        bluetooth_btn = findViewById(R.id.bluetooth_btn);
        bluetooth_btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1464c1")));

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
                    if(christmas_mode)
                    {
                        christmas_mode=false;
                        for(int i=0;i<4;i++)
                            setLight(i, 0);
                    }

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
                    if(christmas_mode)
                    {
                        christmas_mode=false;
                        for(int i=0;i<4;i++)
                            setLight(i, 0);
                    }

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

        final Handler handler2 = new Handler();
        final Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                handler2.postDelayed(this, 1000);
                if(bluetoothError)
                {
                    bluetoothError=false;
                    if(!bluetoothDisconnect)
                        mBluetoothConnection.startClient(device, uuid);
                }
                if(bluetoothDisconnect)
                {
                    bluetoothDisconnect=false;
                    bluetoothError=false;
                    bluetooth_btn.setVisibility(View.VISIBLE);
                }
            }
        };
        handler2.postDelayed(runnable2, 1000);

        bluetooth_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothConnection.startClient(device, uuid);
                bluetooth_btn.setVisibility(View.GONE);
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
            off=false;
            handler=null;
        } else
        {
            off = true;
            handler =  new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for(int i=0;i<4;i++)
                        if(!checked[i] && !christmas_mode)
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

    public static void christmasMode()
    {
        Log.d("app", "res christmasMode");

        final Handler christmas_handler = new Handler();
        final Runnable christmas_runnable = new Runnable() {
            @Override
            public void run() {
                if (christmas_mode)
                    christmas_handler.postDelayed(this, 100);
                setLight(x, y);
                y++;
                if(y>4) {
                    y=1; x++;
                }
                if(x>3) {
                    x=0; y=1;
                }
            }
        };
        christmas_handler.postDelayed(christmas_runnable, 100);
    }

    public static void bluetoothUpdate(JSONArray arr){
        for (int i=0; i < arr.length(); i++) {
            try {
                light_status[i]=arr.getInt(i);
                if(checked[i] && !christmas_mode)
                    setLight(i, light_status[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void bluetoothResume(JSONArray arr) throws JSONException {
        christmas_mode=arr.getBoolean(1);
        if(christmas_mode) {
            for(int i=0;i<4;i++)
                setLight(i, 0);
            try {
                Thread.sleep(2000);
                // call some methods here

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            christmasMode();
        }
        green_red.setText(arr.getJSONArray(4).getString(0));
        yellow.setText(arr.getJSONArray(4).getString(1));

        if(arr.getBoolean(5)) {
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF2400")));
            fab.setImageResource(R.drawable.ic_stop_black_24dp);

            status=true;

            JSONArray yellow2 = arr.getJSONArray(3);
            for(int i=0;i<4;i++) {
                switches[i].setClickable(true);
                switches[i].setChecked(yellow2.getBoolean(i));
            }

            JSONArray light_status2 = arr.getJSONArray(2);
            bluetoothUpdate(light_status2);
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

    public static void showBluetoothBtn()
    {
        bluetoothError=true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.christmas)
        {
            christmas_mode=!christmas_mode;
            if(christmas_mode) {
                for(int i=0;i<4;i++)
                    setLight(i, 0);
                christmasMode();
            } else {
                for(int i=0;i<4;i++)
                    setLight(i, 0);
            }
            mBluetoothConnection.write("christmas 1");
            return true;
        } else if (id == R.id.christmas2)
        {
            christmas_mode=!christmas_mode;
            if(christmas_mode) {
                for(int i=0;i<4;i++)
                    setLight(i, 0);
                christmasMode();
            } else {
                for(int i=0;i<4;i++)
                    setLight(i, 0);
            }
            mBluetoothConnection.write("christmas 2");
            return true;
        } else if (id == R.id.halt)
        {
            mBluetoothConnection.write("halt");
            System.exit(0);
            return true;
        } else if (id == R.id.reboot)
        {
            mBluetoothConnection.write("reboot");
            System.exit(0);
            return true;
        } else if (id == R.id.disconnect)
        {
            mBluetoothConnection.write("disconnect");
            bluetoothDisconnect=true;
            return true;
        }else if (id == R.id.author)
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mBluetoothConnection.write("disconnect");
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
