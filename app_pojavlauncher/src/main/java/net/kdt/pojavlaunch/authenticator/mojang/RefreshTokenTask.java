package net.kdt.pojavlaunch.authenticator.mojang;

import android.content.*;
import android.os.*;
import com.google.gson.*;
import java.util.*;
import net.kdt.pojavlaunch.*;
import net.kdt.pojavlaunch.authenticator.mojang.yggdrasil.*;
import android.app.*;
import net.kdt.pojavlaunch.value.*;

public class RefreshTokenTask extends AsyncTask<String, Void, Throwable> {
    private YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator();
    //private Gson gson = new Gson();
    private RefreshListener listener;
    private MinecraftAccount profilePath;

    private Context ctx;
    private ProgressDialog build;

    public RefreshTokenTask(Context ctx, RefreshListener listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    @Override
    public void onPreExecute() {
        build = new ProgressDialog(ctx);
        build.setMessage(ctx.getString(R.string.global_waiting));
        build.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        build.setCancelable(false);
        build.show();
    }

    @Override
    public Throwable doInBackground(String... args) {
        try {
            this.profilePath = MinecraftAccount.load(args[0]);
            int responseCode = 400;
            responseCode = this.authenticator.validate(profilePath.accessToken).statusCode;
            if (responseCode >= 200 && responseCode < 300) {
                RefreshResponse response = this.authenticator.refresh(profilePath.accessToken, UUID.fromString(profilePath.clientToken));
                // if (response == null) {
                    // throw new NullPointerException("Response is null?");
                // }
                if (response == null) {
                    // Refresh when offline?
                    return null;
                } else if (response.selectedProfile == null) {
                    throw new IllegalArgumentException("Can't refresh a demo account!");
                }
                
                profilePath.clientToken = response.clientToken.toString();
                profilePath.accessToken = response.accessToken;
                profilePath.username = response.selectedProfile.name;
                profilePath.profileId = response.selectedProfile.id;
                profilePath.save();
            }
            return null;
        } catch (Throwable e) {
            return e;
        }
    }

    @Override
    public void onPostExecute(Throwable result) {
        build.dismiss();
        if (result == null) {
            listener.onSuccess(profilePath);
        } else {
            listener.onFailed(result);
        }
    }
}

