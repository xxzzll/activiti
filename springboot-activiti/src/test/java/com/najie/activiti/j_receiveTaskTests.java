package com.najie.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;

/**
 * @author xixi
 * @Description： 测试    接收活动任务
 * @create 2020/6/27
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class j_receiveTaskTests {

    private Logger logger = LoggerFactory.getLogger(j_receiveTaskTests.class);

    @Autowired
    private ProcessEngine processEngine;

    // 1. 准备：部署流程定义
    /**
     *  部署对象 id=240001
     *  部署对象 name=接收活动任务
     */
    @Test
    public void deploymentProcessDefinition_inputStream(){
        InputStream inputStreamBpmn = this.getClass().getResourceAsStream("/receiveTask/receiveTask.bpmn");
        InputStream inputStreamPng = this.getClass().getResourceAsStream("/receiveTask/receiveTask.png");

        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment() // 创建部署对象
                .name("接收活动任务") // 部署名称
                .addInputStream("receiveTask.bpmn", inputStreamBpmn)
                .addInputStream("receiveTask.png", inputStreamPng)
                .deploy();// 部署完成

        logger.info("部署对象 id={}", deployment.getId());
        logger.info("部署对象 name={}", deployment.getName());
    }

    // 2. 启动流程实例

    /**
     * 启动流程实例 + 设置流程变量 + 获取流程变量 + 向后执行一步
     */
    @Test
    public void startProcessInstance(){
        // 流程定义的 key
        String processDefinitionKey = "receiveTask";

        ProcessInstance processInstance = processEngine.getRuntimeService() // 与正在执行的流程实例和执行对象相关的 service
                .startProcessInstanceByKey(processDefinitionKey);// 使用流程定义的key启动流程实例，key对应文件 classpath: processes/myprocess.bpmn 文件中 ID 属性; 使用 key 启动，默认按照最新版本的流程定义启动

        logger.info("流程实例 id={}", processInstance.getId());
        logger.info("流程定义 id={}", processInstance.getProcessDefinitionId());

        // 查询执行对象
        Execution execution1 = processEngine.getRuntimeService()
                .createExecutionQuery()
                .processInstanceId(processInstance.getId())
                .activityId("receivetask1") // 活动ID == xx.bpmn 活动节点的属性ID的值
                .singleResult();


        // 设置流程变量
        processEngine.getRuntimeService()
                .setVariable(execution1.getId(), "汇总当日销售额", 21000);

        // 流程向后执行一步
        processEngine.getRuntimeService()
                .trigger(execution1.getId());

        // 查询执行对象
        Execution execution2 = processEngine.getRuntimeService()
                .createExecutionQuery()
                .processInstanceId(processInstance.getId())
                .activityId("receivetask2") // 活动ID == xx.bpmn 活动节点的属性ID的值
                .singleResult();

        Integer amount = (Integer)processEngine.getRuntimeService()
                .getVariable(execution2.getId(), "汇总当日销售额");

        System.out.println("给老板发短信，销售金额：" + amount);

        // 流程向后执行一步
        processEngine.getRuntimeService()
                .trigger(execution2.getId());

    }

}
