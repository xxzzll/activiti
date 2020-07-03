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
import java.util.List;

/**
 * @author xixi
 * @Description： 测试    个人任务
 * @create 2020/6/27
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class k_taskTests {

    private Logger logger = LoggerFactory.getLogger(k_taskTests.class);

    @Autowired
    private ProcessEngine processEngine;

    // 1. 准备：部署流程定义
    @Test
    public void deploymentProcessDefinition_inputStream(){
        InputStream inputStreamBpmn = this.getClass().getResourceAsStream("/task/task.bpmn");
        InputStream inputStreamPng = this.getClass().getResourceAsStream("/task/task.png");

        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment() // 创建部署对象
                .name("任务") // 部署名称
                .addInputStream("task.bpmn", inputStreamBpmn)
                .addInputStream("task.png", inputStreamPng)
                .deploy();// 部署完成

        logger.info("部署对象 id={}", deployment.getId());
        logger.info("部署对象 name={}", deployment.getName());
    }

    // 2. 启动流程实例
    @Test
    public void startProcessInstance(){
        // 流程定义的 key
        String processDefinitionKey = "task";
        // 启动流程变量指定办理人
        ProcessInstance processInstance = processEngine.getRuntimeService() // 与正在执行的流程实例和执行对象相关的 service
                .startProcessInstanceByKey(processDefinitionKey);

        logger.info("流程实例 id={}", processInstance.getId());
        logger.info("流程定义 id={}", processInstance.getProcessDefinitionId());
    }

    /*3. 查询当前人的个人任务*/
    @Test
    public void findMyPersonTaskList(){
        String assignee = "张催山";
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

    // 灭绝师太出差 ==》分配个人任务：从一个人到另一个人（认领任务）
    @Test
    public void setAssigneeTask(){
        // 任务 ID
        String taskId = "262505";
        // 重新分配的办理人
        String userId = "张催山";

        processEngine.getTaskService()
                .setAssignee(taskId, userId);
    }

    /*4. 完成我的任务*/
    @Test
    public void completeMyPersonTask(){
        String taskId = "262505";
        processEngine.getTaskService()
                .complete(taskId);

        logger.info("完成我的任务，任务id={}", taskId);
    }
}
