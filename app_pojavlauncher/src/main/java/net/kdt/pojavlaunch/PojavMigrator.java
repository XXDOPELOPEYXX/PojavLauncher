package net.kdt.pojavlaunch;
import android.content.*;
import java.io.*;
import net.kdt.pojavlaunch.value.*;

public class PojavMigrator
{
    public static void migrateAccountData(Context ctx) {
        File oldAccDir = new File(Tools.DIR_ACCOUNT_OLD);
        if (oldAccDir.exists() && oldAccDir.isDirectory()) {
            for (String account : oldAccDir.list()) {
                File oldAccFile = new File(oldAccDir, account);

                try {
                    MCProfile.Builder oldAccStruct = MCProfile.load(oldAccFile.getAbsolutePath());

                    MinecraftAccount newAccStruct = new MinecraftAccount();
                    newAccStruct.accessToken = oldAccStruct.getAccessToken();
                    newAccStruct.clientToken = oldAccStruct.getClientID();
                    newAccStruct.isMicrosoft = false;
                    newAccStruct.profileId = oldAccStruct.getProfileID();
                    newAccStruct.selectedVersion = oldAccStruct.getVersion();
                    newAccStruct.username = oldAccStruct.getUsername();
                    newAccStruct.save();
                } catch (IOException e) {
                    Tools.showError(ctx, e);
                }

                oldAccFile.delete();
            }
        }
    }
    
    public static boolean migrateGameDir() throws IOException, InterruptedException {
        File oldGameDir = new File(Tools.DIR_GAME_OLD);
        
        boolean moved = oldGameDir.exists() && oldGameDir.isDirectory();
        /*
        if (!migrateBugFix20201217() && moved) {
            command("mv " + Tools.DIR_GAME_OLD + " " + Tools.DIR_GAME_HOME + "/");
        }
        */
        if(!moved) {
            oldGameDir.renameTo(new File(Tools.DIR_GAME_HOME + "/"));
        }
        return moved;
    }

    public static boolean migrateBugFix20201217() throws IOException, InterruptedException {
        File bugGameDir = new File(Tools.DIR_GAME_NEW + "/.minecraft");
        File oldGameDir = new File(Tools.DIR_GAME_OLD);
        boolean moved = bugGameDir.exists() && bugGameDir.isDirectory();
        /*
        if (oldGameDir.exists() && oldGameDir.isDirectory() && moved) {
            command("rm -rf " + oldGameDir.getAbsolutePath());
        }
        */
        if (moved) {
            command("mv " + bugGameDir.getAbsolutePath() + " " + Tools.DIR_GAME_OLD);
            command("rm -rf " + Tools.DIR_GAME_HOME + "/*");
            command("mv " + Tools.DIR_GAME_OLD + " " + Tools.DIR_GAME_HOME + "/");
        }

        return moved;
    }

    private static void command(String cmd) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new IOException("Exit code " + exitCode +
                                  ", message:\n" + Tools.read(p.getErrorStream()));
        }
    }
}
