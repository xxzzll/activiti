package com.najie.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
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
import java.util.zip.ZipInputStream;

/**
 * @author xixi
 * @Description： 测试流程实例、任务执行
 * @create 2020/6/26
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class c_processInstanceTests {

    private Logger logger = LoggerFactory.getLogger(a_helloWorldTests.class);

    @Autowired
    private ProcessEngine processEngine;

    // 1. 准备：部署流程定义
    /**
     * 部署对象 id=95001
     * 部署对象 name=zip包部署流程定义
     */
    @Test
    public void deploymentProcessDefinition_zip(){
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("processes/myprocess.zip");
        ZipInputStream zipInputStream = new ZipInputStream(in);
        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment() // 创建部署对象
                .name("zip包部署流程定义") // 部署名称
                .addZipInputStream(zipInputStream)
                .deploy();// 部署完成

        logger.info("部署对象 id={}", deployment.getId());
        logger.info("部署对象 name={}", deployment.getName());
    }

    // 2. 启动流程实例

    /**
     * 流程实例 id=97501
     * 流程定义 id=myProcess:1:95004
     */
    @Test
    public void startProcessInstance(){
        // 流程定义的 key
        String processDefinitionKey = "myProcess";

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
        String taskId = "102502";
        processEngine.getTaskService()
                .complete(taskId);

        logger.info("完成我的任务，任务id={}", taskId);
    }

    // 查询流程状态(判断流程正在执行还是结束)
    @Test
    public void isCompletedProcess(){
        String processInstanceId = "97501";

        ProcessInstance processInstance = processEngine.getRuntimeService() // 表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId) //
                .singleResult();

        if (processInstance == null) {
            System.out.println("流程已结束");
        }
        else{
            System.out.println("流程还未完成");
        }
    }

    // 查询历史任务
    @Test
    public void queryHistoryTask(){
        String assignee = "张三";

        List<HistoricTaskInstance> list = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .taskAssignee(assignee) // 查询条件：办理人
                .list();

        list.forEach(hti->{
            System.out.println(hti.getId() + " " + hti.getName() + " " + hti.getStartTime() + " " + hti.getEndTime() + " " + hti.getDurationInMillis());
            System.out.println("################################################");
        });
    }

    // 查询历史流程实例
    @Test
    public void queryHistoryProcessInstance(){
        String processInstanceId = "97501";

        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        System.out.println(historicProcessInstance.getId() + " " + historicProcessInstance.getProcessDefinitionId() + " " + historicProcessInstance.getStartTime() + " " + historicProcessInstance.getEndTime() + " " + historicProcessInstance.getDurationInMillis());
    }
}
