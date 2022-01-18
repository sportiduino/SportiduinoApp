package org.sportiduino.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;

import org.sportiduino.app.databinding.ActivityAboutBinding;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textViewDescription.setMovementMethod(LinkMovementMethod.getInstance());
        binding.textViewLicense.setMovementMethod(LinkMovementMethod.getInstance());
        binding.textViewVersion.setText(String.format(getString(R.string.version_s), BuildConfig.VERSION_NAME));

        binding.textViewGithub.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getString(R.string.github_url)));
            startActivity(intent);
        });
    }
}