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
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private ArrayAdapter<String> getNamesOfTheImages() {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            int id = getResources().getIdentifier("micr_" + i, "drawable",
                    getPackageName());
            names.add(getResources().getResourceEntryName(id));
        }

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
        mTess.setImage(bitmap);
        String result = mTess.getUTF8Text();
        result = result.replace(" ", "");
        result = result.replace("\n", "");
        result = result.trim();
        Log.v("Result", result);
        if (String.valueOf(result.charAt(0)).equals("a")) {
            getRoutingNumber(result);
            getAccountNumber(result);
        } else if (String.valueOf(result.charAt(0)).equals("c")) {
            getTransitNumber(result);
            getFinancialInstitutionNumber(result);
            getCanadianAccountNumber(result);
        }
    }

    private String getRoutingNumber(String fullNumber) {
        char[] characters = fullNumber.toCharArray();
        String routingNumber = "";
        int count_a = 0;
        for (int index = 0; index < characters.length; index++) {
            String character = String.valueOf(characters[index]);
            if (character.equals("a")) {
                count_a++;
            }
            if (count_a < 2) {
                routingNumber += character;
            }
        }
        routingNumber = routingNumber.replace("a", "");
        Log.v("ROUTING", routingNumber);
        return routingNumber;
    }

    private String getAccountNumber(String fullNumber) {
        char[] characters = fullNumber.toCharArray();
        String accountNumber = "";
        int count_a = 0, count_c = 0;
        for (int index = 0; index < characters.length; index++) {
            String character = String.valueOf(characters[index]);
            if (character.equals("a")) {
                count_a++;
            }
            if (character.equals("c")) {
                count_c++;
            }
            if (count_a == 2 && count_c < 1) {
                accountNumber += character;
            }
        }
        accountNumber = accountNumber.replace("a", "");
        Log.v("ACCOUNT", accountNumber);
        return accountNumber;
    }

    private String getTransitNumber(String fullNumber) {
        char[] characters = fullNumber.toCharArray();
        String transitNumber = "";
        int count_a = 0, count_d = 0;
        for (int index = 0; index < characters.length; index++) {
            String character = String.valueOf(characters[index]);
            if (character.equals("a")) {
                count_a++;
            }
            if (character.equals("d")) {
                count_d++;
            }
            if (count_a == 1 && count_d < 1) {
                transitNumber += character;
            }
        }
        transitNumber = transitNumber.replace("a", "");
        Log.v("TRANSIT", transitNumber);
        return transitNumber;
    }

    private String getFinancialInstitutionNumber(String fullNumber) {
        char[] characters = fullNumber.toCharArray();
        String financialInstitution = "";
        int count_a = 0, count_d = 0;
        for (int index = 0; index < characters.length; index++) {
            String character = String.valueOf(characters[index]);
            if (character.equals("a")) {
                count_a++;
            }
            if (character.equals("d")) {
                count_d++;
            }
            if (count_a < 2 && count_d == 1) {
                financialInstitution += character;
            }
        }
        financialInstitution = financialInstitution.replace("d", "");
        financialInstitution = financialInstitution.replace("c", "");
        Log.v("FINANCIAL INSTITUTION", financialInstitution);
        return financialInstitution;
    }

    private String getCanadianAccountNumber(String fullNumber) {
        char[] characters = fullNumber.toCharArray();
        String accountNumber = "";
        int count_a = 0, count_c = 0;
        for (int index = 0; index < characters.length; index++) {
            String character = String.valueOf(characters[index]);
            if (character.equals("a")) {
                count_a++;
            }
            if (character.equals("c")) {
                count_c++;
            }
            if (count_a == 2 && count_c < 3) {
                accountNumber += character;
            }
        }
        accountNumber = accountNumber.replace("a", "");
        accountNumber = accountNumber.replace("d", "");
        Log.v("ACCOUNT", accountNumber);
        return accountNumber;
    }
}
