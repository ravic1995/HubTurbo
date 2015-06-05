package backend.interfaces;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import util.Utility;

public abstract class RepoStore {

    private static String directory = "store";
    private static final String TEST_DIRECTORY = "store/test";
    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public static String escapeRepoName(String repoName) {
        return repoName.replace("/", "-") + ".json";
    }

    protected void addTask(StoreTask task) {
        pool.execute(task);
    }

    public abstract CompletableFuture<Model> loadRepository(String repoId);
    public abstract void saveRepository(String repoId, SerializableModel model);

    public boolean isRepoStored(String repoId) {
        File file = new File(getRepoPath(repoId));
        return file.exists() && file.isFile();
    }

    private static String getRepoPath(String repoId) {
        ensureDirectoryExists();
        String newRepoName = RepoStore.escapeRepoName(repoId);
        return new File(RepoStore.directory, newRepoName).getAbsolutePath();
    }

    public static void write(String repoId, String output) {
        Utility.writeFile(getRepoPath(repoId), output);
    }

    public static Optional<String> read(String repoId) {
        return Utility.readFile(getRepoPath(repoId));
    }

    private static void ensureDirectoryExists() {
        File directory = new File(RepoStore.directory);
        if (!directory.exists() || !directory.isDirectory()) {
            directory.mkdirs();
        }
    }

    public static void enableTestDirectory() {
        changeDirectory(RepoStore.TEST_DIRECTORY);
    }

    private static void changeDirectory(String newDir) {
        RepoStore.directory = newDir;
    }
}
