package com.example.heman.group14_hw09_a;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.People;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heman on 4/18/2017.
 */

public class PeopleAsync extends AsyncTask<GoogleSignInAccount, Void, User> {

    Context context;
    IProfie instance;

    public PeopleAsync(Context context, IProfie instance) {
        this.context = context;
        this.instance = instance;
    }

    @Override
    protected User doInBackground(GoogleSignInAccount... params) {
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();

            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(httpTransport,
                    jacksonFactory,
                    context.getString(R.string.clientID),
                    context.getString(R.string.clientSecret),
                    params[0].getServerAuthCode(), "").execute();

            GoogleCredential googleCredential = new GoogleCredential.Builder()
                    .setClientSecrets(
                            context.getString(R.string.clientID),
                            context.getString(R.string.clientSecret)
                    ).setTransport(httpTransport)
                    .setJsonFactory(jacksonFactory)
                    .build();

            googleCredential.setFromTokenResponse(tokenResponse);

            People people = new People.Builder(httpTransport, jacksonFactory, googleCredential)
                    .setApplicationName("MyChatApplication")
                    .build();
            Person profile = people.people().get("people/me").execute();
            List<Gender> genderList = profile.getGenders();
            List<Name> names = profile.getNames();
            List<Photo> photos = profile.getPhotos();
            List<EmailAddress> addresses = profile.getEmailAddresses();
            User user = new User();
            if(genderList != null)
                user.setGender(genderList.get(genderList.size()-1).getValue());
            if(names != null) {
                String[] nameArray = names.get(names.size()-1).get("displayNameLastFirst").toString().split(", ");
                user.setLastName(nameArray[0]);
                user.setFirstName(nameArray[1]);
            }
            if(photos != null)
                user.setPhotoURL(photos.get(photos.size()-1).getUrl().toString());
            if(addresses != null)
                user.setEmail(addresses.get(addresses.size()-1).getValue().toString());
            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(User user) {
        this.instance.setUserProfile(user);
    }

    public interface IProfie {
        void setUserProfile(User user);
    }

}
