package de.dmitryRogozhin;


import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignInfoActivity extends AppCompatActivity {

    private TextView signName;
    private TextView signDescription;
    private ImageView signImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_info);
        signName = findViewById(R.id.SignName);
        signDescription = findViewById(R.id.SignDescription);
        signImage = findViewById(R.id.SignImage);
        findViewById(R.id.Back).setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onStart() {
        this.useSignClass();
        super.onStart();
    }

    private void useSignClass() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int id = extras.getInt("signClass");
            Sign sign = new Sign(id);
            this.signName.setText(sign.getName());
            this.signDescription.setText(sign.getDescription());
            this.signImage.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), sign.getImage()));
        }
    }
}
