package testPlugin;

import com.intellij.openapi.diagnostic.Logger;

/**
 * Java source code which need to be enhanced
 * Created by Yan Yu on 2014-05-19.
 */
public class JavaSourceCode {
    private static final Logger LOG = Logger.getInstance("#testPlugin.JavaSourceCode");
    private static boolean isDir = true;

    public static void main(String[] args){
        removeEntry("file.txt", false);
    }

    public static void removeEntry(String filename, boolean dp) {
        String con = "condition";
        if(con.length() > 5){
            if(dp){
                isDir = false;
            }else{
                isDir = false;
            }
        }


        if(isDir == false){
            if(filename.equals("file.txt")){
                LOG.error("something bad happen!"<caret>);
                return;
            }
        }

        System.out.println("normal exit");
        return;
    }
}
