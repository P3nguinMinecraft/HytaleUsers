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
import util.IOManager;
import util.Paths;

public class SearchManager {
	public static void init() {
		ArrayList<String> usernames = IOManager.readOutput();
        String lastCheckpoint = IOManager.readCheckpoint();
		ExecutorService executor = Executors.newFixedThreadPool(Config.THREADS);

        int checked = 0;
        int found = 0;
        long startTime = System.currentTimeMillis();
        
        try {
	        for (int len = Config.MIN_LEN; len <= Config.MAX_LEN; len++) {
	            Iterator<String> iterator = Config.RECHECK_EXISTING ? new ArrayList<String>(usernames).iterator() : new UsernameGenerator(Config.CHARS, len);
	    
	            List<String> batch = new ArrayList<>();
	    
	            while (iterator.hasNext()) {
	                String username = iterator.next();
	
	                if (lastCheckpoint != null && username.compareTo(lastCheckpoint) <= 0) {
	                    continue;
	                }
	                
	                batch.add(username);
	
	                if (batch.size() >= Config.THREADS || !iterator.hasNext()) {
	                    List<Future<Boolean>> futures = executor.invokeAll(
	                        batch.stream().map(u -> (Callable<Boolean>) () -> Checker.checkUsername(u)).toList()
	                    );
	                    
	                    boolean changed = false;
	
	                    for (int i = 0; i < batch.size(); i++) {
	                        Boolean available = null;
	                        try {
	                            available = futures.get(i).get();
	                        } catch (InterruptedException | ExecutionException e) {
	                            e.printStackTrace();
	                        }
	
	                        String u = batch.get(i);
	                        checked++;
	
	                        if (available != null) {
	                        	if (available) {
	                        		usernames.add(u);
		                        	
		                        	if (Config.RECHECK_EXISTING) {
		                        		changed = true;
		                        	}
		                        	else {
		                                IOManager.appendOutput(u);
		                        	}
		                            found++;
		                            System.out.println("[FOUND] " + u);
	                        	}
	                        	else if (Config.RECHECK_EXISTING) {
	                        		usernames.remove(u);
	                        		changed = true;
	                        		System.out.println("[REMOVED] " + u);
	                        	}
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
	                    
	                    if (changed && Config.RECHECK_EXISTING) {
	                    	IOManager.saveOutput(usernames);
	                    }
	
	                    IOManager.saveCheckpoint(batch.get(batch.size() - 1));
	
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
	
	        IOManager.saveCheckpoint(null);
	        executor.shutdown();
		}
		catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
}
