package com.qizhuo.framework.ui.gamegallery;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.qizhuo.framework.Emulator;
import com.qizhuo.framework.R;
import com.qizhuo.framework.base.EmulatorActivity;
import com.qizhuo.framework.gamedata.dao.entity.GameEntity;
import com.qizhuo.framework.ui.gamegallery.RomsFinder.OnRomsFinderListener;
import com.qizhuo.framework.ui.gamegallery.CopyRoms2Files.OnRomsCopyListener;

import com.qizhuo.framework.utils.DialogUtils;
import com.qizhuo.framework.utils.FileUtilsa;
import com.qizhuo.framework.utils.NLog;
import com.qizhuo.framework.utils.ZipUtil;

abstract public class BaseGameGalleryActivity extends AppCompatActivity
        implements OnRomsFinderListener, OnRomsCopyListener {

    private static final String TAG = "BaseGameGalleryActivity";

    protected Set<String> exts;
    protected Set<String> inZipExts;
    protected boolean reloadGames = true;
    protected boolean reloading = false;
    private RomsFinder romsFinder = null;
    // private DatabaseHelper dbHelper = null;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        HashSet<String> exts = new HashSet<>(getRomExtensions());
//        exts.addAll(getArchiveExtensions());
        //创建表
//        dbHelper = new DatabaseHelper(this);
//        SharedPreferences pref = getSharedPreferences("android50comp", Context.MODE_PRIVATE);
//        String androidVersion = Build.VERSION.RELEASE;
//
//        if (!pref.getString("androidVersion", "").equals(androidVersion)) {
//            SQLiteDatabase db = dbHelper.getWritableDatabase();
//            dbHelper.onUpgrade(db, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
//            db.close();
//            Editor editor = pref.edit();
//            editor.putString("androidVersion", androidVersion);
//            editor.apply();
//            NLog.i(TAG, "Reinit DB " + androidVersion);
//        }
        reloadGames = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!FileUtilsa.isSDCardRWMounted()) {
            showSDCardFailed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (romsFinder != null) {
            romsFinder.stopSearch();
        }
    }

    //开始游戏
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void reloadGames(boolean searchNew, File selectedFolder) {
        if (romsFinder == null) {
            reloadGames = false;
            reloading = searchNew;
            romsFinder = new RomsFinder(exts, inZipExts, this, this, searchNew, selectedFolder);
            //  if (!romsFinder.isAlive())
            romsFinder.start();
        }
    }

    public void searchUri2Roms(Uri uri) {
        CopyRoms2Files search = new CopyRoms2Files(this, uri, this, exts);
        search.start();
    }

    @Override
    public void onRomsFinderFoundGamesInCache(ArrayList<GameEntity> oldRoms) {
        try {
            setLastGames(oldRoms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRomsFinderNewGames(ArrayList<GameEntity> roms) {
        try {
            setNewGames(roms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRomsFinderEnd(boolean searchNew) {
        romsFinder = null;
        reloading = false;
    }

    @Override
    public void onRomsFinderCancel(boolean searchNew) {
        romsFinder = null;
        reloading = false;
    }

    protected void stopRomsFinding() {
        if (romsFinder != null) {
            romsFinder.stopSearch();
        }
    }

    public void showSDCardFailed() {
        runOnUiThread(() -> {

            AlertDialog dialog = new Builder(BaseGameGalleryActivity.this)
                    .setTitle(R.string.error)
                    .setMessage(R.string.gallery_sd_card_not_mounted)
                    .setOnCancelListener(dialog1 -> finish())
                    .setPositiveButton(R.string.exit, (dialog1, which) -> finish())
                    .create();
            DialogUtils.show(dialog, true);
        });
    }

    public abstract Class<? extends EmulatorActivity> getEmulatorActivityClass();

    abstract public void setLastGames(ArrayList<GameEntity> games);

    abstract public void setNewGames(ArrayList<GameEntity> games);

    abstract protected Set<String> getRomExtensions();

    public abstract Emulator getEmulatorInstance();

    protected Set<String> getArchiveExtensions() {
        HashSet<String> set = new HashSet<>();
//        set.add("zip");
        return set;
    }

    @Override
    public void onRomsCopyStart() {

    }

    @Override
    public void onRomsFinderCancel() {
        reloadGames(true, getExternalFilesDir(null));
    }
}
