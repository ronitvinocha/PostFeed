package com.example.postfeed;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PostsActivity extends AppCompatActivity implements RestAPIResponse {
    private final int PICK_IMAGE_REQUEST = 22;
    public static final int GET_POSTS = 0x01;
    public static final int ADD_POST = 0x02;
     private Uri filePath;
     ConstraintLayout addpost;
     BottomSheetBehavior addPostBottomsheet;
     CallAPI callAPI;
     ImageView addPostImage;
     FirebaseStorage storage;
     StorageReference storageReference;
      EditText caption;
    ArrayList<Post> postList;
    List<Post> temp = new ArrayList();
    PostAdapter postAdapter;
     RecyclerView recyclerView;
    EditText search;
     public static final int  MY_PERMISSIONS_REQUEST_READ_MEDIA=44;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askStoragePermission();
            }
        });
        fab.requestFocus();
        getPosts();
        setupfirebase();
        addpost=findViewById(R.id.addpostlayout);
        addPostBottomsheet=BottomSheetBehavior.from(addpost);
        addPostBottomsheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        if(addPostBottomsheet instanceof LockableBottomSheetBehavior)
        {
            ((LockableBottomSheetBehavior) addPostBottomsheet).setLocked(true);
        }
        addPostBottomsheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState==BottomSheetBehavior.STATE_EXPANDED)
                {
                    fab.setVisibility(View.GONE);
                }
                else
                {
                    fab.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        findViewById(R.id.backbuttonsearchwidget).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPostBottomsheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
        findViewById(R.id.addpost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(caption.getText().toString().length()!=0)
                {
                    uploadtofirebaase();
                }
                else
                {
                    caption.setError("Caption can not be null");
                }

            }
        });
        addPostImage=findViewById(R.id.addpostimage);
        caption=findViewById(R.id.captioneditext);
        addPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
        postList=new ArrayList<>();
        search=findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            // TODO Auto-generated method stub
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {

            // filter your list from your input
            if(s.length()>2)
            {
                filter(s.toString());
            }else
            {
                if(!temp.isEmpty())
                {
                    temp.clear();
                    postAdapter.updateList(postList);
                    recyclerView.smoothScrollToPosition(0);
                }
            }

            //you can use runnable postDelayed like 500 ms to delay search text
        }
    });
    }

    private void askStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
    } else {
        SelectImage();
    }
}
     @Override
public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
        case MY_PERMISSIONS_REQUEST_READ_MEDIA:
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
               SelectImage();
            }
            else
            {
                Log.i("🙏🏻","why here");
                finish();
            }
            break;

        default:
            break;
    }
}
    void filter(String text){
     for(Post d: postList){
           //or use .equal(text) with you want equal match
           //use .toLowerCase() for better matches
           if(d.getUsername().contains(text)){
               temp.add(d);
           }
     }
     //update recyclerview
     postAdapter.updateList(temp);
}
    private void uploadtofirebaase() {
        try {
            MainActivity.hideKeyboard(this);
            showLoader();
            storage=FirebaseStorage.getInstance();
             storageReference=storage.getReference();
             storageReference=storageReference.child("images/"+ UUID.randomUUID().toString());
            InputStream stream=new FileInputStream(new File(FileUtils.getRealPath(this,filePath)));
            UploadTask uploadTask=storageReference.putStream(stream);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.i("🙏🏻",downloadUri.toString());
                        addpost(downloadUri.toString());
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showLoader() {
        findViewById(R.id.addpostprogressbar).setVisibility(View.VISIBLE);
        findViewById(R.id.addpostmain).setVisibility(View.GONE);
    }
    private void hideLoader() {
        findViewById(R.id.addpostprogressbar).setVisibility(View.GONE);
        findViewById(R.id.addpostmain).setVisibility(View.VISIBLE);
    }

    private void setupfirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
    }
    private void addpost(String contentimageurl)
    {
         try {

            URL url = new URL(Constants.SERVER_URL + "/addpost");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("userid",Utils.getDefaults("userid",this)));
            nameValuePairs.add(new BasicNameValuePair("contenttext",caption.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("contentimageurl",contentimageurl));
            callAPI = new CallAPI(url, nameValuePairs, ADD_POST,this);
            callAPI.execute(this);

        } catch (Exception e) {
            Log.i("==errorallletbalance", "failed");
        }
    }
    private void getPosts() {
        try {

            URL url = new URL(Constants.SERVER_URL + "/posts");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            callAPI = new CallAPI(url, nameValuePairs, GET_POSTS,this);
            callAPI.execute(this);

        } catch (Exception e) {
            Log.i("==errorallletbalance", "failed");
        }
    }

    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Image from here..."),
            PICK_IMAGE_REQUEST);
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {

        super.onActivityResult(requestCode,
                               resultCode,
                               data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
            && resultCode == RESULT_OK
            && data != null
            && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            Log.i("🙏🏻",filePath.toString());
             Log.i("🙏🏻",FileUtils.getRealPath(this,filePath));

            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                                    .Images
                                    .Media
                                    .getBitmap(
                                        getContentResolver(),
                                        filePath);
                addPostBottomsheet.setState(BottomSheetBehavior.STATE_EXPANDED);
//                imageView.setImageBitmap(bitmap);
                ImageView addpostimage=findViewById(R.id.addpostimage);
                addpostimage.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }
    @Override
    public void postRestAPICall(String response, int callerid, CallApiResponse callApiResponse) {
        if(callerid==GET_POSTS)
        {
            try{
                Log.i("👁",response);
             recyclerView=findViewById(R.id.postrecyceler);
             postList=new ArrayList<>();
             if(!postList.isEmpty())
             {
                 postList.clear();
             }
            JSONObject json=new JSONObject(response);
            JSONArray jsonArray =json.getJSONArray("response");
            if(jsonArray.length()!=0)
            {
                findViewById(R.id.noposttext).setVisibility(View.GONE);
               for(int i=0;i<jsonArray.length();i++)
               {
                   JSONObject jsonObject=(JSONObject) jsonArray.get(i);
                   Post post=new Post();
                   post.setContentimageurl(jsonObject.getString("contentimageurl"));
                   post.setContenttext(jsonObject.getString("contentText"));
                   post.setUsername(jsonObject.getString("name"));
                   postList.add(post);
               }
            }
            else
            {
                findViewById(R.id.noposttext).setVisibility(View.VISIBLE);
            }
            postAdapter=new PostAdapter(PostsActivity.this,postList);
            recyclerView.setAdapter(postAdapter);
            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            }
            catch (Exception e)
            {
                Log.i("debugging",e.getMessage());
                e.printStackTrace();
            }
        }
        else if(callerid==ADD_POST)
        {
            try {
                JSONObject json=new JSONObject(response);
                if(json.getString("response").compareTo("success")==0)
                {
                    Toast.makeText(PostsActivity.this,"Post Added successfully",Toast.LENGTH_SHORT).show();
                    hideLoader();
                    addPostBottomsheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                    getPosts();
                }
                else
                {

                }
            }
            catch (Exception e)
            {
                Log.i("debugging",e.getMessage());
            }
        }

    }

    @Override
    public void onBackPressed() {
        if(addPostBottomsheet.getState()==BottomSheetBehavior.STATE_EXPANDED)
        {
            addPostBottomsheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else
        {
            super.onBackPressed();
        }
    }
}