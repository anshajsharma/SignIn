package com.sih.googlesignin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class DisplayUser extends AppCompatActivity {

    CircleImageView circleImageView;
    TextView name,DOB,EmailId;
    Button signout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_user);
        circleImageView = findViewById(R.id.profile_pic);
        name = findViewById(R.id.user_name);
        DOB = findViewById(R.id.DOB);
        EmailId = findViewById(R.id.email_id);
        signout = findViewById(R.id.sign_out);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if(acct != null)
            {
                name.setText(acct.getDisplayName());
                Picasso.with(this)
                        .load(acct.getPhotoUrl())
                        .into(circleImageView);
                EmailId.setText(acct.getEmail());
            }

        }
        else{
            Intent intent = new Intent(DisplayUser.this, MainActivity.class);
            startActivity(intent);
        }
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DisplayUser.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        checkLoginStatus();

    }
    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
        {
            if(currentAccessToken==null)
            {
                name.setText("");
                EmailId.setText("");
                circleImageView.setImageResource(0);
                Toast.makeText(DisplayUser.this,"User Logged out",Toast.LENGTH_LONG).show();
            }
            else
                loadUserProfile(currentAccessToken);
        }
    };

    private void loadUserProfile(AccessToken newAccessToken)
    {
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response)
            {
                try {
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");
                    String image_url = "https://graph.facebook.com/"+id+ "/picture?type=normal";

                    EmailId.setText(email);
                    name.setText(first_name +" "+last_name);

                    Picasso.with(DisplayUser.this).load(image_url).into(circleImageView);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields","first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void checkLoginStatus()
    {
        if(AccessToken.getCurrentAccessToken()!=null)
        {
            loadUserProfile(AccessToken.getCurrentAccessToken());
        }
    }
}
