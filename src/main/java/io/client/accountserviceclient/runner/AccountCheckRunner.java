package io.client.accountserviceclient.runner;

import io.client.accountserviceclient.entity.Account;
import io.client.accountserviceclient.server.MockAccountServer;
import io.client.accountserviceclient.service.AccountService;
import io.client.accountserviceclient.service.impl.MessageSender;
import io.client.accountserviceclient.util.ConsoleColors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCheckRunner implements ApplicationRunner {
    private final AccountService accountService;
    private final MockAccountServer accountServer;
    private final Random random = new Random();

    private final MessageSender messageSender;

    @Value("${app.desired_account_number}")
    private int desiredAccountNumber;

    @Value("${client.rCount}")
    private int rCount;

    @Value("${client.wCount}")
    private int wCount;

    @Value("${client.simulationTime}")
    private int simulationTime;

    @Value("${client.idListLength}")
    private long idListLength;

    private volatile boolean isShutDown = false;

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
//        start();

//        for (int i = 0; i < 10; i++) {
//            messageSender.send(i + ". wow");
//        }

        Integer accountId = 12;  // Example account ID
        BigDecimal balance = messageSender.sendAndReceive(accountId);
        log.info("Received balance for account {}: {}", accountId, balance);

    }

    private void start() throws InterruptedException {
        generateAccounts();
//        startSimulation();
        List<Account> idList = initalizeIdList();
        try (
                ExecutorService rCountPool = Executors.newCachedThreadPool();
                ExecutorService wCountPool = Executors.newCachedThreadPool();
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2)
        ) {


            // Schedule the shutdown of rCountPool after 2 seconds
            scheduler.schedule(() -> {
                log.info(ConsoleColors.YELLOW + "Shutting down the rCountPool after {} seconds" + ConsoleColors.RESET, simulationTime);
                isShutDown = true;
                rCountPool.shutdown();
            }, simulationTime, TimeUnit.SECONDS);

            // Schedule the shutdown of wCountPool after 2 seconds
            scheduler.schedule(() -> {
                log.info(ConsoleColors.YELLOW + "Shutting down the wCountPool after {} seconds" + ConsoleColors.RESET, simulationTime);
                isShutDown = true;
                wCountPool.shutdown();
            }, simulationTime, TimeUnit.SECONDS);

//            LocalDateTime start = LocalDateTime.now();
//            LocalDateTime end = start.plusSeconds(simulationTime);
//        for (; start.isBefore(end); start = start.plusSeconds(1)) {
//            submitGetAmountTasksInto(rCountPool, idList, rCount);
//            submitAddAmountTasksInto(wCountPool, idList, wCount);
//        }
            int iterationCounter = 0;
//            while (start.isBefore(end)) {
            while (!isShutDown) {
                log.info(ConsoleColors.ANSI_WHITE_BACKGROUND + "iteration: {}" + ConsoleColors.RESET, ++iterationCounter);
                submitGetAmountTasksInto(rCountPool, idList, rCount);
                submitAddAmountTasksInto(wCountPool, idList, wCount);
                sleep();
//                start = start.plus(100, ChronoUnit.MILLIS); // Increment by 100 milliseconds
            }


            // Wait for the rCountPool to terminate
            if (!rCountPool.awaitTermination(simulationTime + 1, TimeUnit.SECONDS)) { // Increased to 5 seconds
                log.warn("rCountPool did not terminate in the specified time.");
                List<Runnable> droppedTasks = rCountPool.shutdownNow();
                log.warn("rCountPool was abruptly shut down. {} tasks will not be executed.", droppedTasks.size());
            } else {
                log.info("rCountPool terminated successfully.");
            }

            // Wait for the wCountPool to terminate
            if (!wCountPool.awaitTermination(simulationTime + 1, TimeUnit.SECONDS)) { // Increased to 5 seconds
                log.warn("wCountPool did not terminate in the specified time.");
                List<Runnable> droppedTasks = wCountPool.shutdownNow();
                log.warn("wCountPool was abruptly shut down. {} tasks will not be executed.", droppedTasks.size());
            } else {
                log.info("wCountPool terminated successfully.");
            }
            scheduler.shutdownNow();
            // Log the counters
            log.info("Total getAmount calls: {}", accountServer.getGetAmountCount());
            log.info("Total addAmount calls: {}", accountServer.getAddAmountCount());
        }

//
//        rCountPool.shutdown();
//        wCountPool.shutdown();
    }

    private void submitGetAmountTasksInto(ExecutorService rCountPool, List<Account> idList, int rCount) {
        if (!isShutDown && !rCountPool.isShutdown()) {
            log.info(ConsoleColors.PURPLE_BOLD_BRIGHT +
                            "{} : creating rCount tasks.." +
                            ConsoleColors.RESET,
                    LocalDateTime.now());

            IntStream.range(0, rCount)
                    .mapToObj(i -> new CustomTask(
                            i,
                            makeRCountTask(idList.get(random.nextInt(idList.size())).getId()))
                    )
                    .forEach(task -> tryToSubmit(rCountPool, task, task.getId()));
        }
    }

    private void submitAddAmountTasksInto(ExecutorService wCountPool, List<Account> idList, int wCount) {
        if (!isShutDown && !wCountPool.isShutdown()) {
            log.info(ConsoleColors.CYAN_BOLD_BRIGHT +
                            "{} : creating wCount tasks.." +
                            ConsoleColors.RESET,
                    LocalDateTime.now());

            IntStream.range(0, wCount)
                    .mapToObj(i -> new CustomTask(
                            i,
                            makeWCountTask(idList.get(random.nextInt(idList.size())).getId()))
                    )
                    .forEach(task -> {
                        tryToSubmit(wCountPool, task, task.getId());

                    });
        }
    }

    private Runnable makeRCountTask(int accountId) {
        return () -> {
            accountServer.getAmount(accountId);
            sleep();
        };
    }

    private Runnable makeWCountTask(int accountId) {
        return () -> {
            accountServer.addAmount(accountId, 10L);
            sleep();
        };
    }

    private void tryToSubmit(ExecutorService pool, Runnable task, int taskId) {
        if (!isShutDown && !pool.isShutdown()) {
            try {
                pool.submit(new CustomTask(taskId, () -> {
                    log.info(ConsoleColors.GREEN_BACKGROUND +
                                    "task with {} is being executed by thread {}" +
                                    ConsoleColors.RESET,
                            task.toString(),
                            Thread.currentThread().getName());
                    task.run();
                }));
            } catch (RejectedExecutionException e) {
                log.warn(ConsoleColors.RED_BACKGROUND +
                                "Task submission rejected: {}" +
                                ConsoleColors.RESET,
                        e.getMessage());
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(random.nextInt(100, 501)); // Sleep 100 - 500 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<Account> initalizeIdList() {
        log.info(
                ConsoleColors.BLUE_BOLD +
                        "the length of id list are set to {} in application.yaml" +
                        ConsoleColors.RESET,
                idListLength
        );
        long accountCount = accountService.countAccounts();

        if (accountCount < idListLength) {
            log.info(
                    ConsoleColors.BLUE_BOLD +
                            "unable to create id list of desired length ({}) " +
                            "because number of available accounts ({}) is less than requested id list length." +
                            "Instead list of length {} will be created" +
                            ConsoleColors.RESET,
                    idListLength,
                    accountCount,
                    accountCount
            );
            idListLength = accountCount;
        }
        return accountService.getAccounts().subList(0, (int) idListLength);

    }

    private void generateAccounts() {
        log.info(
                ConsoleColors.BLUE_BOLD +
                        "number of generated accounts is set to {} in application.yaml" +
                        ConsoleColors.RESET,
                desiredAccountNumber
        );
        long accountCount = accountService.countAccounts();
        if (accountCount < desiredAccountNumber) {
            int accountsToGenerate = desiredAccountNumber - (int) accountCount;
            accountService.generateAccounts(accountsToGenerate);
            log.info(
                    ConsoleColors.ANSI_PURPLE_BACKGROUND +
                            "Generated {} accounts to meet the minimum requirement of {} accounts." +
                            ConsoleColors.RESET,
                    accountsToGenerate,
                    desiredAccountNumber
            );
        } else {
            log.info(
                    ConsoleColors.ANSI_CYAN_BACKGROUND +
                            "There are already {} (>= {}) accounts in the database." +
                            ConsoleColors.RESET,
                    accountCount,
                    desiredAccountNumber
            );
        }
    }

    // old version method
//    private void startSimulation() throws InterruptedException {
//        List<Account> idList = initalizeIdList();
//        try (
//                ExecutorService executorServiceRCount = Executors.newFixedThreadPool(rCount);
//                ExecutorService executorServiceWCount = Executors.newFixedThreadPool(wCount);
//                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1) // use this?
//        ) {
//
//            for (int i = 0; i < rCount; i++) {
//                executorServiceRCount.submit(() -> {
//                    while (!Thread.currentThread().isInterrupted()) {
//                        for (Account account : idList) {
//                            accountServer.getAmount(account.getId());
//                            sleep();
//                        }
//                    }
//                });
//            }
//
//            for (int i = 0; i < wCount; i++) {
//                executorServiceWCount.submit(() -> {
//                    while (!Thread.currentThread().isInterrupted()) {
//                        for (Account account : idList) {
//                            accountServer.addAmount(account.getId(), 10L);
//                            sleep();
//                        }
//                    }
//                });
//            }
//
//            // Schedule a task to shut down the executor service after 5 seconds
//            scheduler.schedule(() -> {
//                log.info(ConsoleColors.RED_BACKGROUND +
//                                "Shutting down the executor service after {} seconds (set on application.yaml)" +
//                                ConsoleColors.RESET,
//                        simulationTime
//                );
//                executorServiceRCount.shutdownNow();
//                executorServiceWCount.shutdownNow();
//            }, simulationTime, TimeUnit.SECONDS);
//
//            // Wait for the executor service to terminate, with a buffer of 1 second
//            executorServiceRCount.awaitTermination(16, TimeUnit.SECONDS);
//            scheduler.shutdown();
//        }
//        // Log the counters
//        log.info("Total getAmount calls: {}", accountServer.getGetAmountCount());
//        log.info("Total addAmount calls: {}", accountServer.getAddAmountCount());
//    }

}
