package feature;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import util.Config;
import util.FileManager;

public class Search {
	public static void init() {
        String lastCheckpoint = FileManager.readCheckpoint();
		ExecutorService executor = Executors.newFixedThreadPool(Config.THREADS);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Config.OUTPUT_FILE, true))) {
            int checked = 0;
            int found = 0;
            long startTime = System.currentTimeMillis();
            boolean skip = lastCheckpoint != null;
        
            for (int len = Config.MIN_LEN; len <= Config.MAX_LEN; len++) {
                Iterator<String> generator = new UsernameGenerator(Config.CHARS, len);
        
                List<String> batch = new ArrayList<>();
        
                while (generator.hasNext()) {
                    String username = generator.next();

                    if (skip) {
                        if (username.equals(lastCheckpoint)) skip = false;
                        continue;
                    }
                    
                    batch.add(username);

                    if (batch.size() >= Config.THREADS || !generator.hasNext()) {
                        List<Future<Boolean>> futures = executor.invokeAll(
                            batch.stream().map(u -> (Callable<Boolean>) () -> Checker.checkUsername(u)).toList()
                        );

                        for (int i = 0; i < batch.size(); i++) {
                            Boolean available = null;
                            try {
                                available = futures.get(i).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                            String u = batch.get(i);
                            checked++;

                            if (available != null && available == true) {
                                writer.write(u);
                                writer.newLine();
                                writer.flush();
                                found++;
                                System.out.println("[FOUND] " + u);
                            }
                            else if (available == null) {
                                System.err.println("[ERROR] API error for username: " + u);
                                for (Future<Boolean> f : futures) {
                                    f.cancel(true);
                                }
                                executor.shutdownNow();
                                System.exit(1);
                                return;
                            }
                        }

                        FileManager.saveCheckpoint(batch.get(batch.size() - 1));

                        if (checked % 50 == 0) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            double rate = checked / (elapsed / 1000.0);
                            System.out.println("Checked: " + checked + " | Found: " + found + " | Rate: " + String.format("%.2f", rate) + "/s");
                        }
                        batch.clear();
                        Thread.sleep(Config.DELAY_MS);
                    }
                }
            }

            FileManager.saveCheckpoint(null);
            executor.shutdown();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
	}
}
