package com.example.ocrtest;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private Spinner mSpinner;
    private Button mButton;
    private Bitmap mBitmap;

    private TessBaseAPI mTess;
    private String mDataPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataPath = getFilesDir() + "/tesseract/";

        checkFile(new File(mDataPath + "tessdata/"), "mcr");

        mTess = new TessBaseAPI();
        mTess.init(mDataPath, "mcr");

        mImageView = findViewById(R.id.imageView);
        mSpinner = findViewById(R.id.spinner);
        mSpinner.setAdapter(getNamesOfTheImages());
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSpinner.setSelection(i);
                String name = adapterView.getItemAtPosition(i).toString();
                mImageView.setImageDrawable(getDrawable(getDrawableId(name)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mButton = findViewById(R.id.openCameraButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                mBitmap = BitmapFactory.decodeResource(getResources(), getDrawableId(mSpinner.getSelectedItem().toString()));
                processImage(mBitmap);
            }
        });

    }

    private int getDrawableId(String name) {
        switch (name) {
            case "abecedario":
                return R.drawable.abecedario;
            case "cg_check":
                return R.drawable.cg_check;
            case "cp_check":
                return R.drawable.cp_check;
            case "micrb":
                return R.drawable.micrb;
            case "micrg":
                return R.drawable.micrg;
            case "uw_check":
                return R.drawable.uw_check;
            case "ub_check":
                return R.drawable.ub_check;
        }
        return R.drawable.abecedario;
    }

    private ArrayAdapter<String> getNamesOfTheImages() {
        ArrayList<String> names = new ArrayList<>();
        names.add(getResources().getResourceEntryName(R.drawable.abecedario));
        names.add(getResources().getResourceEntryName(R.drawable.cg_check));
        names.add(getResources().getResourceEntryName(R.drawable.cp_check));
        names.add(getResources().getResourceEntryName(R.drawable.micrb));
        names.add(getResources().getResourceEntryName(R.drawable.micrg));
        names.add(getResources().getResourceEntryName(R.drawable.uw_check));
        names.add(getResources().getResourceEntryName(R.drawable.ub_check));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void copyFiles(String name) {
        try {
            String fileName = "tessdata/" + name + ".traineddata";

            //Location of language data files
            String filepath = mDataPath + "/" + fileName;

            AssetManager assetManager = getAssets();

            //Open byte streams for reading/writing
            InputStream instream = assetManager.open(fileName);
            OutputStream outstream = new FileOutputStream(filepath);

            //Copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile(File dir, String name) {

        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(name);
        }

        if (dir.exists()) {
            String datafilepath = mDataPath + "/tessdata/" + name + ".traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles(name);
            }
        }
    }

    public void processImage(Bitmap bitmap) {
        String result = "";
        mTess.setImage(bitmap);
        result = mTess.getUTF8Text();
        Log.v("RESULT: ", result);
    }
}
