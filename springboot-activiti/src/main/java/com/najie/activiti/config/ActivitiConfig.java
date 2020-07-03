package com.najie.activiti.config;

import org.activiti.engine.*;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xixi
 * @description Activiti 核心接口配置，通过扫描 @Bean，把核心api 交给 Spring 容器管理
 */
@Configuration
public class ActivitiConfig {
    private Logger logger = LoggerFactory.getLogger(ActivitiConfig.class);


    /**
     * 下面代码，是从默认配置中获取流程引擎
     * ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
     *
     * @param transactionManager
     * @param dataSource
     * @return
     * @throws IOException
     */
    @Bean
    public ProcessEngine processEngine(DataSourceTransactionManager transactionManager, DataSource dataSource) throws IOException {

        // 内部有自动部署
        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        // 自动部署已有的流程文件
//        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(ResourceLoader.CLASSPATH_URL_PREFIX + "processes/*.bpmn");
//        configuration.setDeploymentResources(resources);

        configuration.setTransactionManager(transactionManager);
        configuration.setDataSource(dataSource);
        configuration.setDatabaseSchemaUpdate("true");
        configuration.setDbIdentityUsed(false);
        ActivitiEventListener activitiEventListener = new ProcessEventListener();
        List<ActivitiEventListener> list = new ArrayList<>();
        list.add(activitiEventListener);
        configuration.setEventListeners(list);
        ProcessEngine processEngine = configuration.buildProcessEngine();
        logger.info("create process engine {}", processEngine);


        return processEngine;
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        logger.info("Create services defined by management process {}", repositoryService);

        return repositoryService;
    }


    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        logger.info("Create management, including services for starting, advancing, and deleting instances {}", runtimeService);

        return runtimeService;
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        TaskService taskService = processEngine.getTaskService();
        logger.info("Create task manage service {}", taskService);

        return taskService;
    }

    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        HistoryService historyService = processEngine.getHistoryService();
        logger.info("Create history manage service {}", historyService);

        return historyService;
    }

    @Bean
    public ManagementService managementService(ProcessEngine processEngine) {
        ManagementService managementService = processEngine.getManagementService();
        logger.info("Create managementService {}", managementService);

        return managementService;
    }

    @Bean
    public IdentityService identityService(ProcessEngine processEngine) {
        IdentityService identityService = processEngine.getIdentityService();
        logger.info("Create Organization Service {}", identityService);

        return identityService;
    }

    @Bean
    public FormService formService(ProcessEngine processEngine) {
        FormService formService = processEngine.getFormService();
        logger.info("Create an optional task form service {}", formService);

        return formService;
    }

}
