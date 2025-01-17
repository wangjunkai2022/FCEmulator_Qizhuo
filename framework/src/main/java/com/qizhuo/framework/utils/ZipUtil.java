package com.qizhuo.framework.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {
    private static String Data_PATH;// = Environment.getExternalStorageDirectory().getPath();
    // private static final String SD_PATH = Environment.getDataDirectory().getPath();
    //Environment.getExternalStorageDirectory()
    static String versionstr = "version:1.33";
    private static String TAG = "ZipUtil";

    public static void checkInit(Context context) {
        Data_PATH = context.getExternalFilesDir(null).getPath();
        FileInputStream fin = null;
        String path_flag = Data_PATH + "/gamezip/flag";
        File file = new File(path_flag);
        if (file.exists()) {
            try {
                fin = new FileInputStream(file);
                int length = fin.available();
                byte[] buffer = new byte[length];
                fin.read(buffer);
                String res = new String(buffer, StandardCharsets.UTF_8);
                fin.close();
                boolean isOK = versionstr.equals(res);
                if (isOK) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String files_path = context.getExternalFilesDir(null).getPath() + "/Roms";
        clearFolder(new File(files_path));
        // 判断app包里有没有 Classified.zip 文件
        try {
            File outDir = new File(files_path);
            if (!outDir.exists()) {
                outDir.mkdirs(); // 创建目录
            }
            String assetFileName = "gamezip/Classified.zip";
            // 输入流
            try (InputStream is = context.getAssets().open(assetFileName);
                 ZipInputStream zis = new ZipInputStream(is)) {

                ZipEntry zipEntry;
                byte[] buffer = new byte[1024];

                while ((zipEntry = zis.getNextEntry()) != null) {
                    // 创建解压后文件的完整路径
                    File outputFile = new File(files_path, zipEntry.getName());

                    // 如果是目录，则创建目录
                    if (zipEntry.isDirectory()) {
                        outputFile.mkdirs();
                    } else {
                        // 确保父目录存在
                        File parentDir = outputFile.getParentFile();
                        if (!parentDir.exists()) {
                            parentDir.mkdirs();
                        }

                        // 写入文件
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                bos.write(buffer, 0, len);
                            }
                        }
                    }
                    zis.closeEntry();
                }

                System.out.println("解压完成: " + assetFileName + " 到 " + outDir.getAbsolutePath());
                // 解压完毕

                File flag_file = new File(path_flag);
                File parent_dir = new File(flag_file.getParent());
                if (!parent_dir.exists()) {
                    parent_dir.mkdirs();
                }
                // 创建文件路径
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(flag_file))) {
                    // 写入数据
                    bos.write(versionstr.getBytes());
                    bos.flush();
                    System.out.println("文件已成功写入: " + file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("写入文件失败: " + e.getMessage());
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("解压失败: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空文件夹里面全部子文件
     */
    private static void clearFolder(File file) {
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                childFiles[i].delete();
            }
            return;
        }
    }

    public static void extractFilesWithExtension(Context context, Uri zipUri, String extension) {
        // 创建目标目录
        File outputDir = new File(context.getExternalFilesDir(null).getPath() + "/Roms/");
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // 创建多级目录
        }

        // 输入流
        try (InputStream is = context.getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(is)) {

            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            while ((zipEntry = zis.getNextEntry()) != null) {
                // 检查扩展名
                if (!zipEntry.isDirectory() && zipEntry.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                    File outputFile = new File(outputDir, zipEntry.getName());

                    // 确保父目录存在
                    File parentDir = outputFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    // 写入文件
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }
                    System.out.println("解压文件: " + outputFile.getAbsolutePath());
                }
                zis.closeEntry();
            }

            System.out.println("解压完成");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("解压失败: " + e.getMessage());
        }
    }
}