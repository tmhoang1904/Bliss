package vn.com.arilliance.bliss;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.com.arilliance.bliss.utils.ActivityResult;
import vn.com.arilliance.bliss.utils.Constants;
import vn.com.arilliance.bliss.utils.RotateImage;

public class MainActivity extends AppCompatActivity {
    //View Elements
    private LoginButton btnLoginFb;
    private LinearLayout layoutTakeCamera, layoutGallery, layoutSetting;
    private CircleImageView imgProfile;
    private TextView txtLogout, txtAppLabel;
    //Facebook SDK
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    //Data Holders
    private static int FB_SIGN_IN;
    private File cameraFile;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Object obj = ActivityResult.onActivityResult(requestCode, resultCode, imageReturnedIntent, MainActivity.this);
        if (obj != null && obj instanceof File){
            cameraFile = (File) obj;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("vn.com.arilliance.bliss", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        super.onCreate(savedInstanceState);

        setupFacebookLogin();

        setContentView(R.layout.activity_main);

        initViewElements();

    }

    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(final LoginResult loginResult) {
            final Profile profile = Profile.getCurrentProfile();
            if (profile == null) {
                if (Profile.getCurrentProfile() == null) {
                    profileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            // profile2 is the new profile
                            Log.v("facebook - profile", profile2.getFirstName());
                            profileTracker.stopTracking();
                        }
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                }
            }
//            GraphRequest request = GraphRequest.newMeRequest(
//                    loginResult.getAccessToken(),
//                    new GraphRequest.GraphJSONObjectCallback() {
//                        @Override
//                        public void onCompleted(JSONObject object, GraphResponse response) {
//                            System.out.println("Login Request : " + response.toString());
//                            Profile profile = Profile.getCurrentProfile();
//                            // Application code
//
//                        }
//                    });
//            Bundle parameters = new Bundle();
//            parameters.putString("fields", "location, email, birthday, first_name, last_name");
//            request.setParameters(parameters);
//            request.executeAsync();
//            System.out.println(loginResult.getAccessToken().getUserId());
//            nextActivityFb(profile);
        }

        @Override
        public void onCancel() {
            Toast.makeText(getBaseContext(), "Login Cancel", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(FacebookException e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            if (e instanceof FacebookAuthorizationException) {
                if (AccessToken.getCurrentAccessToken() != null) {
                    LoginManager.getInstance().logOut();
                }
            }
        }
    };

    private void setupFacebookLogin() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        FB_SIGN_IN = CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode();
        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {

            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
//                nextActivityFb(newProfile);
                profileTracker.stopTracking();
            }
        };
        accessTokenTracker.startTracking();
        profileTracker.startTracking();
    }

    private void initViewElements() {
        btnLoginFb = (LoginButton) findViewById(R.id.btn_facebook_login);

        layoutTakeCamera = (LinearLayout) findViewById(R.id.layout_take_camera_main_activity);

        layoutGallery = (LinearLayout) findViewById(R.id.layout_gallery_main_activity);

        layoutSetting = (LinearLayout) findViewById(R.id.layout_setting_main_activity);

        txtAppLabel = (TextView) findViewById(R.id.txt_app_label_main_activity);

        txtLogout = (TextView) findViewById(R.id.txt_logout_main_activity);

        imgProfile = (CircleImageView) findViewById(R.id.img_profile_main_activity);

        setupViewActions();
    }

    private void setupViewActions() {
        layoutTakeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupTakeCameraAction();
            }
        });

        btnLoginFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().registerCallback(callbackManager, callback);
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "email", "user_birthday"));
            }
        });
    }

    private void putImageToGallery(File cameraFile){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "");
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, cameraFile.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, cameraFile.getName().toLowerCase(Locale.US));
        values.put("_data", cameraFile.getAbsolutePath());

        ContentResolver cr = getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private Bitmap createImageFileFromCamera(Intent data) throws IOException {
        Bundle b = data.getExtras();
        Bitmap bitmap = (Bitmap) b.get("data");
        // this part to save captured image on provided path
        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String fileName = df.format(now).replaceAll("[-: ]", "") + ".jpg";
        File folder = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
        if (!folder.exists()) {
            System.out.println("Create Bliss Picture folder : " + folder.mkdirs());
        } else {
            System.out.println("Folder exists!");
        }
        cameraFile = new File(folder, fileName);
        cameraFile.createNewFile();
        bitmap = RotateImage.getRotateBitmap(cameraFile.getAbsolutePath(), bitmap);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        //write the bytes in file
        FileOutputStream fo = new FileOutputStream(cameraFile);
        fo.write(bytes.toByteArray());
        // remember close de FileOutput
        fo.flush();
        fo.close();
        System.out.println(cameraFile.getAbsolutePath());
        return bitmap;
    }

    private void setupTakeCameraAction() {
        // create intent with ACTION_IMAGE_CAPTURE action
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Uri photoPath = Uri.fromFile(cameraFile);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoPath);

        // start camera activity
        startActivityForResult(intent, Constants.ACTIVITY_CODE.GET_IMAGE_FROM_CAMERA);
    }
}
