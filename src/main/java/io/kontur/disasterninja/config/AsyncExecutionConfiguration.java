package io.kontur.disasterninja.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync(proxyTargetClass = true)
public class AsyncExecutionConfiguration extends AsyncConfigurerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncExecutionConfiguration.class);

    @Override
    @Primary
    @Bean
    public Executor getAsyncExecutor() {
        int poolSize = Runtime.getRuntime().availableProcessors() <= 1 ? 1 :
                Runtime.getRuntime().availableProcessors() - 1;
        LOG.info(String.format("Using %s pool size for AsyncExecutor", poolSize));

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setThreadNamePrefix("async-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(DelegatingSecurityContextRunnable::new);
        executor.initialize();

        return executor;
    }
}
