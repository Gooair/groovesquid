/*
 * Copyright (C) 2013 Maino
 * 
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of
 * this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send
 * a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco,
 * California, 94105, USA.
 * 
 */

package groovesquid.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Maino
 */
public class Utils {
    
    private final static Logger log = Logger.getLogger(Utils.class.getName());
    private static final MessageDigest md5digest;
    private static final MessageDigest sha1digest;
    
    static {
        try {
            md5digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("no MD5 digester", ex);
        }
        try {
            sha1digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("no SHA1 digester", ex);
        }
    }
    
    public static String md5(String s) {
        try {
            return bytesToHexString(md5digest.digest(s.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            // utf-8 always available
            throw new RuntimeException("no UTF-8 decoder available", ex);
        }
    }

    public static String sha1(String s) {
        try {
            return bytesToHexString(sha1digest.digest(s.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            // utf-8 always available
            throw new RuntimeException("no UTF-8 decoder available", ex);
        }
    }
    
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString((int) b & 0xFF);
            if (hex.length() == 1)
                result.append('0');
            result.append(hex);
        }
        return result.toString();
    }

    public static byte[] hexStringToBytes(String hexStr) {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream((hexStr.length() + 1) / 2 + 1);
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            byte byt = Integer.valueOf(Integer.parseInt(str, 16)).byteValue();
            outBytes.write(byt);
        }
        return outBytes.toByteArray();
    }
    
    public static String createRandomHexNumber(int length) {
        Random random = new Random(System.currentTimeMillis());
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            int r = random.nextInt(16);
            result[i] = Integer.toHexString(r).charAt(0);
        }
        return new String(result);
    }
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    public static int compareVersions(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i=0;
        while(i<vals1.length&&i<vals2.length&&vals1[i].equals(vals2[i])) {
        i++;
        }

        if (i<vals1.length&&i<vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return diff<0?-1:diff==0?0:1;
        }

        return vals1.length<vals2.length?-1:vals1.length==vals2.length?0:1;
    }
    
    public static String dataDirectory() {
        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN"))
            return System.getenv("APPDATA");
        else if (OS.contains("MAC"))
            return System.getProperty("user.home") + "/Library/Application "
                    + "Support";
        else if (OS.contains("NUX"))
            return System.getProperty("user.home");
        return System.getProperty("user.dir");
    }
    
    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    public static void closeQuietly(Closeable closeable) {
        closeQuietly(closeable, null);
    }

    public static void closeQuietly(Closeable closeable, String streamDescription) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
                if (!isEmpty(streamDescription)) {
                    log.severe("error closing stream " + streamDescription + ": " + ex);
                }
            }
        }
    }
    
    public static boolean isEmptyDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            return files != null && files.length == 0;
        }
        return false;
    }

    /**
     * Deletes a file, never throwing an exception.
     * If file is a directory, delete it and all sub-directories.
     * <p/>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
     * </ul>
     *
     * @param file file or directory to delete, can be <code>null</code>
     * @return <code>true</code> if the file or directory was deleted, otherwise
     *         <code>false</code>
     */
    public static boolean deleteQuietly(File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (Exception ignore) {
            // intentionally ignored
        }
        try {
            return file.delete();
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            throw new IllegalArgumentException(directory + " does not exist");
        }

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        cleanDirectory(directory);
        if (!directory.delete()) {
            throw new IOException("Unable to delete directory: " + directory);
        }
    }

    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p/>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     * (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file file or directory to delete, must not be <code>null</code>
     * @throws NullPointerException  if the directory is <code>null</code>
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                throw new IOException("Unable to delete file: " + file);
            }
        }
    }
    
    public static <T> List<T> copyListOnlySpecified(List<T> list, int[] indexes) {
        List<T> newlist = new ArrayList<T>();
        for(int i = 0; i < list.size(); i++) {
            if(Arrays.asList(indexes).contains(i)) {
                newlist.add(list.get(i));
            }
        }
        return newlist;
    }
    
    public static boolean isNumeric(String s) {  
        return s.matches("[-+]?\\d*\\.?\\d+");  
    }

    public static ImageIcon stretchImage(ImageIcon image, int width, int height, ImageObserver imageObserver) {
        return stretchImage(image.getImage(), width, height, imageObserver);
    }
    
    public static ImageIcon stretchImage(Image image, int width, int height, ImageObserver imageObserver) {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) newImage.getGraphics();
        g.drawImage(image, 0, 0, width, height, imageObserver);
        g.dispose();
        return new ImageIcon(newImage);
    }
    
    public static Locale localeFromString(String locale) {
        String parts[] = locale.split("_", -1);
        if (parts.length == 1) return new Locale(parts[0]);
        else if (parts.length == 2) return new Locale(parts[0], parts[1]);
        else return new Locale(parts[0], parts[1], parts[2]);
    }
    
    public static Object getByIndex(Map hMap, int index){
        return hMap.values().toArray()[index];
    }
}
