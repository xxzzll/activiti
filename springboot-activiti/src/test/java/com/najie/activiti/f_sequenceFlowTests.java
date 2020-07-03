package com.najie.activiti;

import org.activiti.engine.ProcessEngine;
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
 * @Description： 测试 流程连线
 * @create 2020/6/27
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class f_sequenceFlowTests {

    private Logger logger = LoggerFactory.getLogger(f_sequenceFlowTests.class);

    @Autowired
    private ProcessEngine processEngine;

    // 1. 准备：部署流程定义
    /**
     *  部署对象 id=165001
     *  部署对象 name=流程连线
     */
    @Test
    public void deploymentProcessDefinition_inputStream(){
        InputStream inputStreamBpmn = this.getClass().getResourceAsStream("/sequenceflow/sequenceFlowProcess.bpmn");
        InputStream inputStreamPng = this.getClass().getResourceAsStream("/sequenceflow/sequenceFlowProcess.png");

        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment() // 创建部署对象
                .name("流程连线") // 部署名称
                .addInputStream("sequenceFlowProcess.bpmn", inputStreamBpmn)
                .addInputStream("sequenceFlowProcess.png", inputStreamPng)
                .deploy();// 部署完成

        logger.info("部署对象 id={}", deployment.getId());
        logger.info("部署对象 name={}", deployment.getName());
    }

    // 2. 启动流程实例

    /**
     * 流程实例 id=167501
     * 流程定义 id=sequenceFlow:2:165004
     */
    @Test
    public void startProcessInstance(){
        // 流程定义的 key
        String processDefinitionKey = "sequenceFlow";

        ProcessInstance processInstance = processEngine.getRuntimeService() // 与正在执行的流程实例和执行对象相关的 service
                .startProcessInstanceByKey(processDefinitionKey);// 使用流程定义的key启动流程实例，key对应文件 classpath: processes/myprocess.bpmn 文件中 ID 属性; 使用 key 启动，默认按照最新版本的流程定义启动

        logger.info("流程实例 id={}", processInstance.getId());
        logger.info("流程定义 id={}", processInstance.getProcessDefinitionId());

    }

    /*3. 查询当前人的个人任务*/
    @Test
    public void findMyPersonTaskList(){
        String assignee = "王五";
        List<Task> list = processEngine.getTaskService() // 与正在执行的任务管理有关的 Service
                .createTaskQuery() // 创建任务查询对象
                .taskAssignee(assignee) // 指定个人任务查询，指定办理人
                .list();

        list.forEach(task -> {
            logger.info("任务 id={}", task.getId());
            logger.info("任务名称 {}", task.getName());
            logger.info("任务创建时间 {}", task.getCreateTime());
            logger.info("任务办理人 {}", task.getAssignee());
            logger.info("流程实例 id={}", task.getProcessInstanceId());
            logger.info("执行对象 id={}", task.getExecutionId());
            logger.info("流程定义 id={}", task.getProcessDefinitionId());
            logger.info("###################################################");

        });
    }

    /*4. 完成我的任务*/
    @Test
    public void completeMyPersonTask(){
        // org.activiti.engine.ActivitiException: Unknown property used in expression: ${message=='重要'}
        Map<String, Object> variables = new HashMap<>();
        variables.put("message", "重要");
        String taskId = "170004";
        // 完成任务的同时，设置流程变量，使用流程变量来指定完成任务后，下一个连线，对应 xx.bpmn 文件中 ${message=='不重要'}
        processEngine.getTaskService()
                .complete(taskId, variables);

        logger.info("完成我的任务，任务id={}", taskId);
    }
}
