package com.qizhuo.framework.ui.gamegallery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import com.qizhuo.framework.base.Character.LocalGroupSearch;

import com.qizhuo.framework.gamedata.dao.GameDbUtil;
import com.qizhuo.framework.gamedata.dao.GameEntityDao;
import com.qizhuo.framework.gamedata.dao.entity.GameEntity;
import com.qizhuo.framework.utils.DownloadFileUtil;
import com.qizhuo.framework.utils.HttpDownloader;
import com.qizhuo.framework.utils.ZipUtil;
//import com.unity3d.ads.IUnityAdsListener;
//import com.unity3d.ads.UnityAds;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.qizhuo.framework.R;
import com.qizhuo.framework.base.EmulatorActivity;
import com.qizhuo.framework.ui.gamegallery.GalleryPagerAdapter.OnItemClickListener;
import com.qizhuo.framework.ui.preferences.GeneralPreferenceActivity;
import com.qizhuo.framework.ui.preferences.GeneralPreferenceFragment;
import com.qizhuo.framework.ui.preferences.PreferenceUtil;

import com.qizhuo.framework.utils.DialogUtils;
import com.qizhuo.framework.utils.EmuUtils;
import com.qizhuo.framework.utils.NLog;


public abstract class GalleryActivity extends BaseGameGalleryActivity
        implements OnItemClickListener {

    public static final String EXTRA_TABS_IDX = "EXTRA_TABS_IDX";

    private static final String TAG = GalleryActivity.class.getSimpleName();

    ProgressDialog searchDialog = null;
    private ViewPager pager = null;
    // private DatabaseHelper dbHelper;
    private GalleryPagerAdapter adapter;
    private boolean importing = false;
    private boolean rotateAnim = false;
    private TabLayout mTabLayout;
    //  public static  ArrayList<GameEntity> finalStringListstrlist=new ArrayList<>();
    public static ArrayList<GameEntity> finalStringListstrlist = new ArrayList<>();
    /**
     * 输入框
     */
    private EditText etInput;
    private Button search_btn_backs;
    private boolean DEV_MODE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // DbManager.init(this,"gamedb");
        //  startActivity(new Intent(GalleryActivity.this, DemoActivity.class));
//        finish();
//        if (DEV_MODE) {
////            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
////                    .detectCustomSlowCalls() //API等级11，使用StrictMode.noteSlowCode
////                    .detectDiskReads()
////                    .detectDiskWrites()
////                    .detectNetwork()   // or .detectAll() for all detectable problems
////                    .penaltyDialog() //弹出违规提示对话框
////                    .penaltyLog() //在Logcat 中打印违规异常信息
////                    .penaltyFlashScreen() //API等级11
////                    .build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects() //API等级11
//                    .penaltyLog()
//                    .penaltyDeath()
//                    .build());
//        }
//
//        try {
//            UnityAds.initialize(GalleryActivity.this,"4072917",true);
//            //Network Connectivity Status
//            UnityAds.addListener(new IUnityAdsListener() {
//                @Override
//                public void onUnityAdsReady(String s) {
//
//                }
//
//                /**
//                 * @param s
//                 */
//                @Override
//                public void onUnityAdsStart(String s) {
//
//                }
//
//                /**
//                 * @param s
//                 * @param finishState
//                 */
//                @Override
//                public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
//
//                }
//
//                /**
//                 * @param unityAdsError
//                 * @param s
//                 */
//                @Override
//                public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
//
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        //Network Connectivity Statu
        //  dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_gallery);
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            adapter = new GalleryPagerAdapter(this, this);
            adapter.onRestoreInstanceState(savedInstanceState);
            pager = findViewById(R.id.game_gallery_pager);
            pager.setAdapter(adapter);
            mTabLayout = findViewById(R.id.game_gallery_tab);
            mTabLayout.setupWithViewPager(pager);
            mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (savedInstanceState != null) {
            pager.setCurrentItem(savedInstanceState.getInt(EXTRA_TABS_IDX, 0));
        } else {
            pager.setCurrentItem(PreferenceUtil.getLastGalleryTab(this));
        }
        try {
            exts = getRomExtensions();
            exts.addAll(getArchiveExtensions());
            inZipExts = getRomExtensions();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    ZipUtil.checkInit(getApplication());
                    reloadGames(true, null);
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        etInput = (EditText) findViewById(R.id.search_et_input);
        search_btn_backs = (Button) findViewById(R.id.search_btn_back);
        search_btn_backs.setOnClickListener(v -> {
                    try {
                        if (finalStringListstrlist != null && finalStringListstrlist.size() > 0) {
                            setLastGames(finalStringListstrlist);
                            etInput.getText().clear();
                            InputMethodManager m = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            m.hideSoftInputFromWindow(etInput.getWindowToken(), 0);//比如EditView
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
        etInput.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //  Log.d(TAG,"输入："+ textView.getText());
                try {
                    if (finalStringListstrlist != null && finalStringListstrlist.size() > 0) {
                        Log.d(TAG, "输入数据回车：" + finalStringListstrlist.size());
                        setLastGames(finalStringListstrlist);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (finalStringListstrlist != null && finalStringListstrlist.size() > 0) {
                        ArrayList<GameEntity> games;
                        Log.d(TAG, "输入数据：" + finalStringListstrlist.size());
                        if (!TextUtils.isEmpty(etInput.getText()) && etInput.getText().length() == s.length()) {
                            if (finalStringListstrlist != null && finalStringListstrlist.size() > 0) {
                                games = LocalGroupSearch.searchGroup(s.toString(), finalStringListstrlist);
                            } else {
                                games = finalStringListstrlist;
                            }
                            setLastGames(games);
                        } else {
                            setLastGames(finalStringListstrlist);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void openFolder() {
        // 创建一个意图来打开文档树
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        // 启动活动
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            List<Uri> uris = new ArrayList<>();
            if (data != null) {
                if (data.getClipData() != null) {
                    // 多个文件被选择
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();
                        // 处理每个文件
                        uris.add(fileUri);
                    }
                } else if (data.getData() != null) {
                    // 单个文件被选择
                    Uri fileUri = data.getData();
                    // 处理这个文件
                    uris.add(fileUri);
                }
            }
            for (Uri uri : uris) {
//                reloadGames(true, new File(uri.getPath()));
//                listFilesInDirectory(uri);
                searchUri2Roms(uri);
            }
        }
    }

    private String getFileExtension(DocumentFile documentFile) {
        String fileName = documentFile.getName();
        NLog.d("File Name", documentFile.getName());
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.') + 1);
        }
        return ""; // 如果没有后缀，则返回空字符串
    }

    private void listFilesInDirectory(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri);

        if (documentFile != null && documentFile.isDirectory()) {
            for (DocumentFile file : documentFile.listFiles()) {
                // 这里可以进行文件处理，比如打印文件名称
                String file_ext = getFileExtension(file);
                if (file_ext.equals("nes")) {
                    NLog.d("File Name2", file.getName());
                }
            }
        } else {
            Log.e("Error", "Not a valid directory");
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //菜单
        int itemId = item.getItemId();
        try {
            if (itemId == R.id.gallery_menu_pref) {
                try {
                    Intent i = new Intent(this, GeneralPreferenceActivity.class);
                    i.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName());
                    i.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else if (itemId == R.id.gallery_menu_reload) {
//                try {
//
//                    reloadGames(true, null);
//                    try {
//                        GameDbUtil.getInstance().GetGameEntityService().deleteAll();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                openFolder();
                return true;
            } else if (itemId == R.id.gallery_menu_exit) {
                finish();
                return true;
            } else if (itemId == R.id.gallery_menu_download) {

                try {
                    try {
                        Uri uri = Uri.parse("https://github.com/qizhuocai/FCEmulator_Qizhuo/tree/main/ROM");
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        intent.setData(uri);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (rotateAnim) {
            rotateAnim = false;
        }
        adapter.notifyDataSetChanged();
//        if (reloadGames && !importing) {
////         List<GameEntity> games =   GameDbUtil.getInstance().GetGameEntityList();
////            boolean isDBEmpty =false;
////         if (games!=null&&games.size()>0)
////            {
////                isDBEmpty=true;
////            }
//          //  boolean isDBEmpty = dbHelper.countObjsInDb(GameEntity.class, null) == 0;
//         //   reloadGames(true, null);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceUtil.saveLastGalleryTab(this, pager.getCurrentItem());
    }

    static int resnum = 0;

    @Override
    public void onItemClick(GameEntity game) {

//        if(UnityAds.isReady("qizhuorewardedVideo")&&resnum % 7 == 0&&resnum!=0&&resnum!=1){
//            UnityAds.show(GalleryActivity.this);
//            resnum++;
//        }
        File gameFile = new File(game.path);
        NLog.i(TAG, "select " + game);
        if (game.isInArchive()) {

            try {
                gameFile = new File(getExternalCacheDir(), game.checksum);
                game.path = gameFile.getAbsolutePath();
                GameEntity games = GameDbUtil.getInstance().GetGameEntityService().queryBuilder().where(GameEntityDao.Properties.Zipfile_id.eq(game.zipfile_id)).unique();
//
//            ZipRomFile zipRomFile =GameDbUtil.getInstance().
//                    dbHelper.selectObjFromDb(ZipRomFile.class,
//                    "WHERE _id=" + game.zipfile_id, false);
                File zipFile = new File(games.path);
                if (!gameFile.exists()) {
                    try {
                        EmuUtils.extractFile(zipFile, game.getName(), gameFile);
                    } catch (IOException e) {
                        NLog.e(TAG, "", e);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (gameFile.exists()) {
            game.lastGameTime = System.currentTimeMillis();
            game._id = System.currentTimeMillis();
            game.set_id(System.currentTimeMillis());
            game.runCount++;
            GameDbUtil.getInstance().setGameEntity(game);
            if (!finalStringListstrlist.contains(game)) {
                finalStringListstrlist.add(game);
            }
            //      dbHelper.updateObjToDb(game, new String[]{"lastGameTime", "runCount"});
            onGameSelected(game, 0);
        } else {
//            NLog.w(TAG, "rom file:" + gameFile.getAbsolutePath() + " does not exist");
//            AlertDialog dialog = new AlertDialog.Builder(this)
//                    .setMessage(getString(R.string.gallery_rom_not_found))
//                    .setTitle(R.string.error)
//                    .setPositiveButton(R.string.gallery_rom_not_found_reload, (dialog1, which)
//                            -> reloadGames(true, null))
//                    .setCancelable(false)
//                    .create();
//            dialog.setOnDismissListener(dialog12 ->
//                    reloadGames(true, null));
//            dialog.show();
        }
    }

    public boolean onGameSelected(GameEntity game, int slot) {
        Intent intent = new Intent(this, getEmulatorActivityClass());
        intent.putExtra(EmulatorActivity.EXTRA_GAME, (Serializable) game);
        intent.putExtra(EmulatorActivity.EXTRA_SLOT, slot);
        intent.putExtra(EmulatorActivity.EXTRA_FROM_GALLERY, true);
        startActivity(intent);
        return true;
    }

    @Override
    public void setLastGames(ArrayList<GameEntity> games) {
        adapter.setGames(games);
        pager.setVisibility(games.isEmpty() ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void setNewGames(ArrayList<GameEntity> games) {
        boolean isListEmpty = adapter.addGames(games) == 0;
        pager.setVisibility(isListEmpty ? View.INVISIBLE : View.VISIBLE);
    }

    private void showSearchProgressDialog(boolean zipMode) {
        if (searchDialog == null) {
            searchDialog = new ProgressDialog(this);
            searchDialog.setMax(100);
            searchDialog.setCancelable(false);
            searchDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            searchDialog.setIndeterminate(true);
            searchDialog.setProgressNumberFormat("");
            searchDialog.setProgressPercentFormat(null);
            searchDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                    (dialog, which) -> stopRomsFinding());
        }
        searchDialog.setMessage(getString(zipMode ?
                R.string.gallery_zip_search_label
                : R.string.gallery_sdcard_search_label));
        DialogUtils.show(searchDialog, false);

    }

    public void onSearchingEnd(final int count, final boolean showToast) {
        runOnUiThread(() -> {
            if (searchDialog != null) {
                searchDialog.dismiss();
                searchDialog = null;
            }
            if (showToast) {
                if (count > 0) {
                    Snackbar.make(pager, getString(R.string.gallery_count_of_found_games, count),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    public void onRomsFinderStart(boolean searchNew) {
        if (searchNew) {
            showSearchProgressDialog(false);
        }
    }

//    @Override
//    public void onRomsFinderZipPartStart(final int countEntries) {
//        if (searchDialog != null) {
//            runOnUiThread(() -> {
//                if (searchDialog != null) {
//                    searchDialog.setProgressNumberFormat("%1d/%2d");
//                    searchDialog.setProgressPercentFormat(NumberFormat.getPercentInstance());
//                    searchDialog.setMessage(getString(R.string.gallery_start_sip_search_label));
//                    searchDialog.setIndeterminate(false);
//                    searchDialog.setMax(countEntries);
//                }
//            });
//        }
//    }

    @Override
    public void onRomsFinderCancel(boolean searchNew) {
        super.onRomsFinderCancel(searchNew);
        onSearchingEnd(0, searchNew);
    }

    @Override
    public void onRomsFinderEnd(boolean searchNew) {
        super.onRomsFinderEnd(searchNew);
        onSearchingEnd(0, searchNew);
    }

    @Override
    public void onRomsFinderNewGames(ArrayList<GameEntity> roms) {

        super.onRomsFinderNewGames(roms);
        onSearchingEnd(roms.size(), true);
    }

//    @Override
//    public void onRomsFinderFoundZipEntry(final String message, final int skipEntries) {
//        if (searchDialog != null) {
//            runOnUiThread(() -> {
//                if (searchDialog != null) {
//                    searchDialog.setMessage(message);
//                    searchDialog.setProgress(searchDialog.getProgress() + 1 + skipEntries);
//                }
//            });
//        }
//    }

    @Override
    public void onRomsFinderFoundFile(final String name) {
        if (searchDialog != null) {
            runOnUiThread(() -> {
                if (searchDialog != null) {
                    searchDialog.setMessage(name);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_TABS_IDX, pager.getCurrentItem());
        adapter.onSaveInstanceState(outState);
    }

    @Override
    public void onRomsCopyStart() {
        super.onRomsCopyStart();
        showSearchProgressDialog(false);
    }

    @Override
    public void onRomsFoundFile(String name) {
        if (searchDialog != null) {
            runOnUiThread(() -> {
                if (searchDialog != null) {
                    searchDialog.setMessage("查找到文件:" + name);
                }
            });
        }
    }

    @Override
    public void onRomsCopy2Files(File file) {
        if (searchDialog != null) {
            runOnUiThread(() -> {
                if (searchDialog != null) {
                    searchDialog.setMessage("正在复制文件:" + file.getName());
                }
            });
        }
    }
}
