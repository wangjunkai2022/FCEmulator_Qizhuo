package com.qizhuo.framework.ui.gamegallery;

import static com.qizhuo.framework.ui.gamegallery.GalleryActivity.finalStringListstrlist;

import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.qizhuo.framework.gamedata.dao.GameDbUtil;
import com.qizhuo.framework.gamedata.dao.entity.GameEntity;
import com.qizhuo.framework.utils.NLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class CopyRoms2Files extends Thread {
    private static final String TAG = "RomsFinderUri";
    private Uri rootUri;
    private BaseGameGalleryActivity activity;
    private AtomicBoolean running = new AtomicBoolean(false);
    private OnRomsCopyListener listener;
    Set<String> ext;
    String basePath;

    public CopyRoms2Files(BaseGameGalleryActivity activity, Uri rootUri, OnRomsCopyListener listener, Set<String> ext) {
        try {
            this.rootUri = rootUri;
            this.activity = activity;
            this.listener = listener;
            this.ext = ext;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取两个URI之间的相对路径
     *
     * @param baseUri   基础URI
     * @param targetUri 目标URI
     * @return 相对路径
     */
    public static String getRelativePath(Uri baseUri, Uri targetUri) {
        // 获取基础URI的路径
        String basePath = baseUri.getPath();
        String[] split = basePath.split(":", -1);
        basePath = split[split.length - 1];
        // 获取目标URI的路径
        String targetPath = targetUri.getPath();
        split = targetPath.split(":", -1);
        targetPath = split[split.length - 1];
        if (basePath != null && targetPath != null) {
            // 确保基础路径以斜杠结尾
            if (!basePath.endsWith("/")) {
                basePath += "/";
            }

            // 检查目标路径是否以基础路径开头
            if (targetPath.startsWith(basePath)) {
                // 返回相对路径，去掉基础路径部分
                String filePath = targetPath.substring(basePath.length());
                return filePath;
//                return filePath.substring(0, filePath.indexOf('/', -1));
            }
        }
        return ""; // 如果无法计算相对路径则返回null
    }


    public void findNesFiles(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(activity, uri);
        if (documentFile != null && documentFile.isDirectory()) {
            searchNesFiles(documentFile, documentFile.getName());
        } else {
            NLog.e("查找文件失败", Uri.decode(uri.getPath()) + "不是文件夹");
        }
        activity.runOnUiThread(() -> listener.onRomsFinderCancel());
    }

    private void searchNesFiles(DocumentFile documentFile, String currentPath) {
        for (DocumentFile file : documentFile.listFiles()) {
            String newPath = currentPath + "/" + file.getName(); // 路径拼接
            if (file.isDirectory()) {
                searchNesFiles(file, newPath); // 递归查找
            } else if (file.getName() != null) {
                listener.onRomsFoundFile(file.getName());
                boolean isOk = false;
                for (String ex : ext) {
                    if (file.getName().endsWith(ex)) {
                        isOk = true;
                        break;
                    }
                }
                if (!isOk) {
                    NLog.d("文件不是需要的Rom", Uri.decode(file.getUri().getPath()));
                    continue;
                }
                // 指定应用内的 roms 文件夹
                File baseDir = activity.getExternalFilesDir(null); // 获取应用的基本外部存储目录
                String abs_path = getRelativePath(rootUri, file.getUri());
                File romsDir = new File(baseDir, "roms" + "/" + abs_path);
                romsDir.getParentFile().mkdirs();
                copyDocumentFileToLocalFile(file, romsDir);
                NLog.d("找到的游戏Rom文件", Uri.decode(file.getUri().getPath()));
            }
        }
    }

    private void copyDocumentFileToLocalFile(DocumentFile documentFile, File destFile) {
        // 检查目标文件是否已存在
        if (destFile.exists()) {
            NLog.d("不复制文件", "路径中找到了此文件: " + Uri.decode(destFile.getAbsolutePath()));
            return; // 如果文件存在，跳过复制
        }
        listener.onRomsCopy2Files(destFile);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = activity.getContentResolver().openInputStream(documentFile.getUri());
            out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            NLog.d("复制完毕", "复制到: " + Uri.decode(destFile.getAbsolutePath()));
        } catch (Exception e) {
            NLog.e("复制错误", e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                NLog.e("Close Stream Error", e.getMessage());
            }
        }
    }


    @Override
    public void run() {

        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            running.set(true);
            NLog.i(TAG, "start");
            activity.runOnUiThread(() -> listener.onRomsCopyStart());
            if (this.rootUri != null) {
                try {
                    //全盘查找
                    findNesFiles(rootUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                activity.runOnUiThread(() -> listener.onRomsFinderCancel());
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void stopSearch() {
        if (running.get()) {
            listener.onRomsFinderCancel();
        }
        running.set(false);
        NLog.i(TAG, "cancel search");
    }


    public interface OnRomsCopyListener {

        void onRomsCopyStart();

        void onRomsFoundFile(String name);

        void onRomsCopy2Files(File file);

        void onRomsFinderCancel();
    }

}
