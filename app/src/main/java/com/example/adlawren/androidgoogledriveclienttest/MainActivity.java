package com.example.adlawren.androidgoogledriveclienttest;

import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;
    public static final int RC_CREATE_DRIVE_FILE = 2;

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

    public class UpdatePrivateDriveFileButtonOnClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            updatePrivateDriveFile(getNewDriveFileContentsEditTextContent());
        }
    }

    public class ExportToDriveButtonOnClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            exportToDriveFile("sample_file_name.some_custom_extension",
                    getNewDriveFileContentsEditTextContent());
        }
    }

    // Google Auth
    private GoogleSignInClient mGoogleSignInClient = null;

    // Google Drive
    private DriveClient mDriveClient = null;
    private DriveResourceClient mDriveResourceClient = null;

    private static final String mPrivateDriveFileName = "test_file.some_custom_extension";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleSignInClient = buildGoogleSignInClient();

        SignInButton signInButton = getSignInButton();
        signInButton.setOnClickListener(new SignInButtonOnClickListener());

        Button updatePrivateDriveFileButton = getUpdatePrivateDriveFileButton();
        updatePrivateDriveFileButton.setOnClickListener(new UpdatePrivateDriveFileButtonOnClickListener());

        Button exportToDriveButton = getExportToDriveButton();
        exportToDriveButton.setOnClickListener(new ExportToDriveButtonOnClickListener());
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);

        if (account != null) {
            // Initialize Google Drive variables
            mDriveClient = Drive.getDriveClient(this, account);
            mDriveResourceClient = Drive.getDriveResourceClient(this, account);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Log.i("INFO_TAG", "Successfully signed into Google account!");

                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }

                break;
            case RC_CREATE_DRIVE_FILE:
                if (resultCode == RESULT_OK) {
                    Log.i("INFO_TAG", "Successfully exported file!");
                    Toast.makeText(
                            this, "Successfully exported file!", Toast.LENGTH_LONG)
                                .show();
                }

                break;
            default:
                break;
        }
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .requestScopes(Drive.SCOPE_FILE)
                .build();

        return GoogleSignIn.getClient(this, gso);
    }

    private SignInButton getSignInButton() {
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        return signInButton;
    }

    private TextView getTextView() {
        TextView textView = findViewById(R.id.text_view);
        return textView;
    }

    private EditText getNewDriveFileContentsEditText() {
        EditText editText = findViewById(R.id.new_file_contents_edit_text);
        return editText;
    }

    private String getNewDriveFileContentsEditTextContent() {
        EditText editText = getNewDriveFileContentsEditText();
        return editText.getText().toString();
    }

    private Button getUpdatePrivateDriveFileButton() {
        Button button = findViewById(R.id.update_private_drive_file_button);
        return button;
    }

    private Button getExportToDriveButton() {
        Button button = findViewById(R.id.export_to_drive_button);
        return button;
    }

    private TextView getDriveFileContentsTextView() {
        TextView driveFileContentsTextView = findViewById(R.id.retrieved_file_contents_text_view);
        return driveFileContentsTextView;
    }

    private void updateDriveFileContentsTextView(String newText) {
        TextView driveFileContentsTextView = getDriveFileContentsTextView();
        driveFileContentsTextView.setText(newText);
    }

    private LinearLayout getUserInfoLayout() {
        LinearLayout linearLayout = findViewById(R.id.user_info_layout);
        return linearLayout;
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask
                    .getResult(ApiException.class);
            updateUI(account);

            // Initialize Google Drive variables
            mDriveClient = Drive.getDriveClient(this, account);
            mDriveResourceClient = Drive.getDriveResourceClient(this, account);
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
        LinearLayout userInfoLayout = getUserInfoLayout();

        int signInButtonVisibility, userInfoLayoutVisibility;
        String message;
        if (googleSignInAccount == null) {
            signInButtonVisibility = View.VISIBLE;
            userInfoLayoutVisibility = View.GONE;
            message = "User has NOT signed in!";
        } else {
            signInButtonVisibility = View.GONE;
            userInfoLayoutVisibility = View.VISIBLE;
            message = String.format("User has signed in! (User email: %s)",
                    googleSignInAccount.getEmail());
        }

        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setVisibility(signInButtonVisibility);

        textView.setText(message);

        userInfoLayout.setVisibility(userInfoLayoutVisibility);
    }

    private void updatePrivateDriveFile(final String newText) {
        final Task<DriveFolder> getAppFolderTask = mDriveResourceClient.getAppFolder();
        getAppFolderTask.addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(final DriveFolder driveFolder) {
                // Read the contents of the Drive file
                Task<MetadataBuffer> listChildrenTask = mDriveResourceClient.listChildren(
                        driveFolder);
                listChildrenTask.addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadataBuffer) {
                        // Read file contents
                        for (Metadata metadata : metadataBuffer) {
                            if (metadata.getTitle().equals(mPrivateDriveFileName)) {
                                DriveFile driveFile = metadata.getDriveId().asDriveFile();


                                Task<DriveContents> driveContentsTask = mDriveResourceClient
                                        .openFile(driveFile, DriveFile.MODE_WRITE_ONLY);
                                driveContentsTask
                                        .addOnSuccessListener(
                                                new OnSuccessListener<DriveContents>() {
                                    @Override
                                    public void onSuccess(DriveContents driveContents) {
                                        OutputStream outputStream = driveContents.getOutputStream();
                                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                                            writer.write(newText);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        MetadataChangeSet changeSet = new MetadataChangeSet
                                                .Builder()
                                                .setLastViewedByMeDate(new Date())
                                                .build();
                                        Task<Void> commitTask =
                                                mDriveResourceClient
                                                        .commitContents(driveContents, changeSet);
                                        commitTask.addOnSuccessListener(
                                                new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                displayParsedDriveFileContents();
                                            }
                                        });
                                    }
                                });

                                metadataBuffer.release(); // note: this is a mandatory step!!

                                return;
                            }
                        }

                        metadataBuffer.release(); // note: this is a mandatory step!!

                        // Create file
                        Task<DriveContents> createContentsTask = mDriveResourceClient
                                .createContents();
                        createContentsTask.addOnSuccessListener(
                                new OnSuccessListener<DriveContents>() {
                            @Override
                            public void onSuccess(DriveContents driveContents) {
                                OutputStream outputStream = driveContents.getOutputStream();
                                try (Writer writer = new OutputStreamWriter(outputStream)) {
                                    writer.write(newText);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                MetadataChangeSet metadataChangeSet = new MetadataChangeSet
                                        .Builder()
                                        .setTitle(mPrivateDriveFileName)
                                        .build();

                                Task<DriveFile> createFileTask = mDriveResourceClient.createFile(
                                        driveFolder, metadataChangeSet, driveContents);
                                createFileTask
                                        .addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                                    @Override
                                    public void onSuccess(DriveFile driveFile) {
                                        displayParsedDriveFileContents();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void displayParsedDriveFileContents() {
        Task<DriveFolder> getAppFolderTask = mDriveResourceClient.getAppFolder();
        getAppFolderTask.addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {
                // Read the contents of the Drive file
                Task<MetadataBuffer> listChildrenTask = mDriveResourceClient.listChildren(
                        driveFolder);
                listChildrenTask.addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadataBuffer) {
                        // Read file contents
                        for (Metadata metadata : metadataBuffer) {
                            if (metadata.getTitle().equals(mPrivateDriveFileName)) {
                                DriveFile driveFile = metadata.getDriveId().asDriveFile();


                                Task<DriveContents> driveContentsTask = mDriveResourceClient
                                        .openFile(driveFile, DriveFile.MODE_READ_ONLY);
                                driveContentsTask
                                        .addOnSuccessListener(
                                                new OnSuccessListener<DriveContents>() {
                                    @Override
                                    public void onSuccess(DriveContents driveContents) {
                                        InputStream inputStream = driveContents.getInputStream();


                                        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                                            StringBuilder stringBuilder = new StringBuilder();
                                            int nextCharacterInt;
                                            while ((nextCharacterInt = reader.read()) != -1) {
                                                stringBuilder.append((char) nextCharacterInt);
                                            }

                                            // Update the textview
                                            updateDriveFileContentsTextView(
                                                    stringBuilder.toString());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                metadataBuffer.release(); // note: this is a mandatory step!!

                                return;
                            }
                        }

                        Log.e("ERROR_TAG",
                                "Drive file was not found in App Folder metadata");

                        metadataBuffer.release(); // note: this is a mandatory step!!
                    }
                });
            }
        });
    }

    private void exportToDriveFile(final String fileName, final String fileContent) {
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        createContentsTask.addOnSuccessListener(new OnSuccessListener<DriveContents>() {
            @Override
            public void onSuccess(DriveContents driveContents) {
                OutputStream outputStream = driveContents.getOutputStream();
                try (Writer writer = new OutputStreamWriter(outputStream)) {
                    writer.write(fileContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MetadataChangeSet changeSet = new MetadataChangeSet
                        .Builder()
                        .setTitle(fileName)
                        .build();

                CreateFileActivityOptions createFileActivityOptions =
                        new CreateFileActivityOptions
                                .Builder()
                                .setInitialMetadata(changeSet)
                                .setInitialDriveContents(driveContents)
                                .build();

                Task<IntentSender> createIntentSenderTask = mDriveClient
                        .newCreateFileActivityIntentSender(createFileActivityOptions);
                createIntentSenderTask.addOnSuccessListener(new OnSuccessListener<IntentSender>() {
                    @Override
                    public void onSuccess(IntentSender intentSender) {
                        try {
                            startIntentSenderForResult(
                                    intentSender,
                                    RC_CREATE_DRIVE_FILE,
                                    null,
                                    0,
                                    0,
                                    0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e("ERROR_TAG",
                                    "Error: intent sender for the file creation activity could not be started");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
