package group.aelysium.rustyconnector.common.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

public class GitLoader {
    public static Git cloneRepo() throws GitAPIException {
        return Git.cloneRepository()
                .setURI("https://github.com/eclipse/jgit.git")
                .setDirectory(new File(""))
                .setBranch("main")
                .setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(
                                "username",
                                "password"
                        )
                )
                .call();
    }
    public static void commit() throws GitAPIException {
        Git git = cloneRepo();
        git.add().addFilepattern("*").call();
        git.commit().setMessage("Automated Update - Update Configs").call();
        git.push().setRemote("origin").call();
    }
}
