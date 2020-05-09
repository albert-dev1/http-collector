import java.util.List;
import java.util.concurrent.*;

public class CrawlerExecutor {
    private final List<Callable<String>> callableTask;
    private ExecutorService executor;

    public CrawlerExecutor(List<Callable<String>> callableTask) {
        this.callableTask = callableTask;
    }
    public CompletionService<String> runBlocking() throws InterruptedException {
        executor = Executors.newFixedThreadPool(10);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

        callableTask.forEach(completionService::submit);

        int received = 0;
        float currentProgress;
        float lastPrintedProgress = 0f;
        final int progressStep = 5;
        boolean errors = false;

        while(received < callableTask.size() && !errors) {
            Future<String> resultFuture = completionService.take(); //blocks if none available
            try {
                String result = resultFuture.get();
                received ++;
                currentProgress = received / (float) callableTask.size() * 100;
                if(currentProgress >= lastPrintedProgress + progressStep) {
                    lastPrintedProgress = currentProgress;
                    System.out.println(String.format("Progress: %.0f%%", lastPrintedProgress ));
                }
            }
            catch(Exception e) {
                System.err.println(e);
                errors = true;
            }
        }
        return completionService;


    }

    public void shutdown(){
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }
}
