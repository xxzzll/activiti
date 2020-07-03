package com.najie.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xixi
 * @Description： 测试    开始活动
 * @create 2020/6/27
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class i_startTests {

    private Logger logger = LoggerFactory.getLogger(i_startTests.class);

    @Autowired
    private ProcessEngine processEngine;

    // 1. 准备：部署流程定义
    /**
     *  部署对象 id=220001
     *  部署对象 name=网关-并行网关
     */
    @Test
    public void deploymentProcessDefinition_inputStream(){
        InputStream inputStreamBpmn = this.getClass().getResourceAsStream("/start/start.bpmn");
        InputStream inputStreamPng = this.getClass().getResourceAsStream("/start/start.png");

        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment() // 创建部署对象
                .name("开始活动") // 部署名称
                .addInputStream("start.bpmn", inputStreamBpmn)
                .addInputStream("start.png", inputStreamPng)
                .deploy();// 部署完成

        logger.info("部署对象 id={}", deployment.getId());
        logger.info("部署对象 name={}", deployment.getName());
    }

    // 2. 启动流程实例

    /**
     * 流程实例 id=222501
     * 流程定义 id=parallelGateWay:1:220004
     */
    @Test
    public void startProcessInstance(){
        // 流程定义的 key
        String processDefinitionKey = "start";

        ProcessInstance processInstance = processEngine.getRuntimeService() // 与正在执行的流程实例和执行对象相关的 service
                .startProcessInstanceByKey(processDefinitionKey);// 使用流程定义的key启动流程实例，key对应文件 classpath: processes/myprocess.bpmn 文件中 ID 属性; 使用 key 启动，默认按照最新版本的流程定义启动

        logger.info("流程实例 id={}", processInstance.getId());
        logger.info("流程定义 id={}", processInstance.getProcessDefinitionId());

        // 判断流程是否结束
        String processInstanceId = processInstance.getId();
        ProcessInstance pi = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (pi == null) {
            System.out.println("流程已经结束");

            // 查看历史
            HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            System.out.println(historicProcessInstance.getId() + "   " + historicProcessInstance.getName() + "   " + historicProcessInstance.getDurationInMillis());
        }
    }

}
