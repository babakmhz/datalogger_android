package com.android.datalogger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.datalogger.calendar.CivilDate;
import com.android.datalogger.calendar.DateConverter;
import com.android.datalogger.calendar.PersianDate;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int READ_EXTERNAL_STORAGE_CODE = 100;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 101;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private EditText textIP;
    private RecyclerAdapter recyclerAdapter;
    private SqliteDatabase database;
    private TextView textNoItem;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkCallPermission();
        setupViews();

    }

    private void showProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("لطفا شکیبا باشید");
        dialog.setMessage("در حال ارتباط با ماژول");
        dialog.setCancelable(true);
        dialog.setCancelMessage(null);
        dialog.show();
    }

    private void dissMissDialog() {
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export) {
            saveData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {

        final Prefs prefs = new Prefs(MainActivity.this);
        recyclerView = findViewById(R.id.recycler_items);
        fab = findViewById(R.id.fab);
        textIP = findViewById(R.id.text_ip);
        textNoItem = findViewById(R.id.text_no_item);

        if (prefs.getUrl() != null && !prefs.getUrl().equals(""))
            textIP.setText(prefs.getUrl());

        database = new SqliteDatabase(this);

        if (database.getData() != null && !database.getData().isEmpty()) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerAdapter = new RecyclerAdapter(this, database.getData());
            recyclerAdapter.reverseItemOrder();
            recyclerView.setAdapter(recyclerAdapter);
        } else {
            recyclerView.setVisibility(GONE);
            textNoItem.setVisibility(View.VISIBLE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (textIP.getText() != null && !textIP.getText().toString().equals("")) {
                    prefs.saveUrl(textIP.getText().toString());
                    // Toast.makeText(MainActivity.this, "لطفا شکیبا باشید...", Toast.LENGTH_LONG).show();
                    showProgressDialog();
                    sendHttpRequest(textIP.getText().toString(), MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "لطفا آی پی دستگاه را وارد نمایید", Toast.LENGTH_LONG).show();

                }
            }
        });
    }


    private void sendHttpRequest(String url, final Context context) {

        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, "http://" + url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                Log.i(TAG, "onResponse: " + response);
                                Calendar calendar = Calendar.getInstance();

                                PersianDate persianDate = DateConverter.civilToPersian(new CivilDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)));

                                Date date = calendar.getTime();
                                @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("HH:mm:ss");
                                String formatted_time = format.format(date);

                                DataModel model = new DataModel();
                                model.setData(response);
                                model.setDate(String.valueOf(persianDate.getYear()) + "/" + String.valueOf(persianDate.getMonth()) + "/" + persianDate.getDayOfMonth()
                                        + "---" + formatted_time);

                                database.saveData(model);

                                if (recyclerAdapter == null) {
                                    recyclerAdapter = new RecyclerAdapter(MainActivity.this, database.getData());
                                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.VERTICAL,false));
                                } else {
                                    recyclerAdapter.addItem(model);
                                }
                                if (recyclerView.getVisibility() == GONE) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    textNoItem.setVisibility(GONE);
                                }
                                recyclerAdapter.reverseItemOrder();
                                recyclerView.setAdapter(recyclerAdapter);
                                dissMissDialog();
                                Toast.makeText(context, "پاسخ با موفقیت دریافت شد", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.i(TAG, "onResponseerror: " + error);
                        Toast.makeText(MainActivity.this, "خطا در برقراری ارتباط \n" + error.toString(), Toast.LENGTH_LONG).show();
                    }

                });
        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 10000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 2;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_CODE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "برای ذخیره فایلها دسترسی فوق نیاز است. لطفا برنامه را مجددا اجرا نمایید", Toast.LENGTH_LONG).show();
                }
                break;
            case WRITE_EXTERNAL_STORAGE_CODE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "برای ذخیره فایلها دسترسی فوق نیاز است. لطفا برنامه را مجددا اجرا نمایید", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void checkCallPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                ) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_CODE);

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);

        }
    }

    private void saveData() {

        File root = android.os.Environment.getExternalStorageDirectory();

        File dir = new File(root.getAbsolutePath() + "/data_logger_export");
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, "export.txt");

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);

            List<DataModel> dataModels = database.getData();
            Collections.reverse(dataModels);
            for (DataModel dataModel :
                    dataModels) {
                pw.println(dataModel.getData() + "---" + dataModel.getDate());
                Log.i(TAG, "saveData: " + dataModel.getData() + "---" + dataModel.getDate());
            }
            pw.flush();
            pw.close();
            f.close();
            Toast.makeText(this, "فایل با موفقیت در این مسیر ذخیره شد\n"+file.toString(), Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
