package com.example.adlawren.androidgoogledriveclienttest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;

    public class SignInButtonOnClickListener implements SignInButton.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.sign_in_button:
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                    break;
                default:
                    break;
            }
        }
    }

    private GoogleSignInClient mGoogleSignInClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                    .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = getSignInButton();
        signInButton.setOnClickListener(new SignInButtonOnClickListener());
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                break;
            default:
                break;
        }
    }

    private SignInButton getSignInButton() {
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        return signInButton;
    }

    private TextView getTextView() {
        TextView textView = findViewById(R.id.text_view);
        return textView;
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask
                    .getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            Log.e("ERROR_TAG",
                    "Failed to sign into account, signInResult:failed code="
                            + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount googleSignInAccount) {
        SignInButton signInButton = getSignInButton();
        TextView textView = getTextView();

        int signInButtonVisibility;
        String message;
        if (googleSignInAccount == null) {
            signInButtonVisibility = View.VISIBLE;
            message = "User has NOT signed in!";
        } else {
            signInButtonVisibility = View.GONE;
            message = String.format("User has signed in! (User email: %s)",
                    googleSignInAccount.getEmail());
        }

        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setVisibility(signInButtonVisibility);

        textView.setText(message);
    }
}
