package ly.gerge.rancher2git;

import ly.gerge.rancher2git.rancher.RancherInstance;
import ly.gerge.rancher2git.rancher.RancherStack;
import ly.gerge.rancher2git.repo.GitRepo;
import ly.gerge.rancher2git.zip.ZipAgent;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by gerge on 05.02.2017.
 */
public class Rancher2git {
    public static void main(String[] args) {

        System.out.println("Starting up...");
        String rancherApiURL = args[0] + "/stacks";
        String rancherUSER =  args[1];
        String rancherPASS =  args[2];
        String repoURL = args[3];
        String repoUSER =  args[4];
        String repoPASS =  args[5];



        try {
            RancherInstance rancherInstance = new RancherInstance(rancherApiURL,rancherUSER,rancherPASS);

            FileUtils.deleteDirectory(new File("tmp"));
            FileUtils.deleteDirectory(new File("repo"));
            new File("tmp").mkdirs();

            ArrayList<RancherStack> zips =  rancherInstance.downloadZippedStackConfigs("tmp");

            try(GitRepo gitRepo = new GitRepo("repo",repoURL,repoUSER,repoPASS)){
                FileUtils.copyDirectory(new File("repo/.git"), new File("tmp/git"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            FileUtils.cleanDirectory(new File("repo"));
            FileUtils.moveDirectory(new File("tmp/git"),new File("repo/.git"));

            for (RancherStack zip : zips) {
                new File("repo" + File.separator + zip.getName()).mkdirs();
                ZipAgent.unzip("tmp"+File.separator+zip.getName() + ".zip", "repo" + File.separator +zip.getName());
            }

            try(GitRepo gitRepo = new GitRepo("repo",repoURL,repoUSER,repoPASS)){
                gitRepo.push();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
