// package org.spideruci.experiments;

// import java.io.BufferedReader;
// import java.io.File;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.text.SimpleDateFormat;
// import java.util.Date;

// import org.eclipse.jgit.api.Git;
// import org.eclipse.jgit.api.errors.GitAPIException;
// import org.eclipse.jgit.api.errors.NoHeadException;
// import org.eclipse.jgit.errors.NoWorkTreeException;
// import org.eclipse.jgit.lib.Repository;
// import org.eclipse.jgit.revwalk.RevCommit;

// public class SCMUtil {

//     private SCMUtil() {
//     }

//     public static String getCommitsSeparatedByOneEpoch(Repository repo, String startCommit, int epoch) {
//         String result;
//         String beforeDate;
//         String afterDate;

//         Git git = new Git(repo);

//         try {
//             if (startCommit == null) {
//                 for (RevCommit commit : git.log().setMaxCount(1).call()) {
//                     beforeDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(commit.getCommitTime() * 1000L));
//                     break;
//                 }
//             } else {
//                 Ref ref = git.checkout().setStartPoint(startCommit).call();
//                 ref.
//             }
            

//             String command = String.format(
//                 "git rev-list -n 1 --first-parent --before=\"{}\" --after=\"{}\" master",
//                 beforeDate,
//                 afterDate
//             );


//             Process p = Runtime.getRuntime().exec(command, null, repo.getWorkTree());
//             p.waitFor();

//             BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

//             while (reader.ready()) {
//                 result = reader.readLine();
//                 break;
//             }
//         } catch (GitAPIException | NoHeadException | NoWorkTreeException | IOException | InterruptedException e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//         }

//         return result;


//     }
// }