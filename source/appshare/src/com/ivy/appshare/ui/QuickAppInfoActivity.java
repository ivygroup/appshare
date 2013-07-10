package com.ivy.appshare.ui;


import com.ivy.appshare.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class QuickAppInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_app_info);
        
        Bitmap image = getIntent().getParcelableExtra("image");
        String name = getIntent().getStringExtra("name");
        String packagename = getIntent().getStringExtra("packagename");
        String version = getIntent().getStringExtra("version");
        String sourcedir = getIntent().getStringExtra("sourcedir");

        ((ImageView)findViewById(R.id.image)).setImageBitmap(image);
        ((TextView)findViewById(R.id.name)).setText(name);
        ((TextView)findViewById(R.id.packagename)).setText(packagename);
        ((TextView)findViewById(R.id.version)).setText(version);
        ((TextView)findViewById(R.id.sourcedir)).setText(sourcedir);
    }
}
