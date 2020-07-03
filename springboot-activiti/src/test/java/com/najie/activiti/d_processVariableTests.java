package com.najie.activiti;

import com.najie.activiti.variable.Person;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * @author xixi
 * @Description： 测试 流程变量
 * @create 2020/6/26
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class d_processVariableTests {

    private Logger logger = LoggerFactory.getLogger(a_helloWorldTests.class);

    @Autowired
    private ProcessEngine processEngine;

    // 1. 准备：部署流程定义
    /**
     * 部署对象 id=140001
     * 部署对象 name=部署流程定义[流程变量]
     */
    @Test
    public void deploymentProcessDefinition_inputStream(){
        InputStream inputStreamBpmn = this.getClass().getResourceAsStream("/processes/processVariables.bpmn");
        InputStream inputStreamPng = this.getClass().getResourceAsStream("/processes/processVariables.png");

        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment() // 创建部署对象
                .name("部署流程定义[流程变量]") // 部署名称
                .addInputStream("processVariables.bpmn", inputStreamBpmn)
                .addInputStream("processVariables.png", inputStreamPng)
                .deploy();// 部署完成

        logger.info("部署对象 id={}", deployment.getId());
        logger.info("部署对象 name={}", deployment.getName());
    }

    // 2. 启动流程实例

    /**
     * 流程实例 id=142501
     * 流程定义 id=processVariable:2:140004
     */
    @Test
    public void startProcessInstance(){
        // 流程定义的 key
        String processDefinitionKey = "processVariable";

        ProcessInstance processInstance = processEngine.getRuntimeService() // 与正在执行的流程实例和执行对象相关的 service
                .startProcessInstanceByKey(processDefinitionKey);// 使用流程定义的key启动流程实例，key对应文件 classpath: processes/myprocess.bpmn 文件中 ID 属性; 使用 key 启动，默认按照最新版本的流程定义启动

        logger.info("流程实例 id={}", processInstance.getId());
        logger.info("流程定义 id={}", processInstance.getProcessDefinitionId());

    }

    @Test
    public void delete(){
        String processDefinitionKey = "processVariable";

        List<ProcessDefinition> list = processEngine.getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .list();

        list.forEach(pd->{
            processEngine.getRepositoryService()
                    .deleteDeployment(pd.getDeploymentId(), true);
        });

        System.out.println("删除成功！");
    }

    // 设置流程变量
    @Test
    public void setVariables(){
        /*与正在进行的任务有关的*/
        TaskService taskService = processEngine.getTaskService();
        String taskId = "142505";
        /* 设置变量，基本数据类型*/
//        taskService.setVariableLocal(taskId, "请假天数", 5);
//        taskService.setVariable(taskId, "请假时间", new Date());
//        taskService.setVariable(taskId, "请假原因", "回家探亲，一起吃了饭");

        /*设置变量，引用类型*/
        /**
         * 引用类型没有实现序列化接口异常:
         *      couldn't find a variable type that is able to serialize com.najie.activiti.variable.Person@33e434c8
         *
         */
        Person p = new Person(20, "翠花");

        taskService.setVariable(taskId, "人员信息(serialVersionUID)", p);

        System.out.println("设置完成！");
    }


    /**
     * 获取流程变量
     *  注意：查询完成后的下个任务查询流程变量，本地变量无法查询出对应值（跟上一个任务绑定了）
     */
    @Test
    public void getVariables(){
        /*与正在进行的任务有关的*/
        TaskService taskService = processEngine.getTaskService();
        String taskId = "142505";
        /*获取变量，基本数据类型*/
//        Integer days = (Integer)taskService.getVariable(taskId, "请假天数");
//        Date date = (Date)taskService.getVariable(taskId, "请假时间");
//        String reasons = (String)taskService.getVariable(taskId, "请假原因");
//
//        System.out.println("days: " + days);
//        System.out.println("date: " + date);
//        System.out.println("reasons: " + reasons);

        /*获取变量，引用类型*/
        /**
         * 当一个 javaBean(实现序列化)放置到流程变量中，要求javaBean属性不能发生变化，
         *      如果发生变化，再获取的时候，抛出异常： Couldn't deserialize object in variable '人员信息'
         *
         * 解决办法： 在实体类中固定序列化版本，private static final long serialVersionUID = -6178525619293275720L;
         *
         */
        Person p = ((Person) taskService.getVariable(taskId, "人员信息(serialVersionUID)"));
        System.out.println("id: " + p.getId() + " ,name: " + p.getName());
    }

    // 模拟设置或获取流程变量的场景
    public void setAndGetVariables(){
        /*与正在运行的流程实例有关的*/
        RuntimeService runtimeService = processEngine.getRuntimeService();
        /*与正在进行的任务有关的*/
        TaskService taskService = processEngine.getTaskService();

        /*模拟设置变量的值*/
        // 通过 RuntimeService
//        runtimeService.setVariables(executionId, variables); // 一次可以设置多个变量的值
//                .setVariable(executionId, variableName, value); // 一次只能设置一个变量的值

        // 通过 TaskService
//        taskService.setVariables(taskId, variables);
//                .setVariable(taskId, variableName, value);

//        runtimeService.startProcessInstanceByKey(startProcessInstanceByKey, variables); // 启动流程实例的同时，设置流程变量
//        taskService.complete(taskId, variables); // 完成任务的同时，设置流程变量


        /*模拟获取变量*/
//        runtimeService.getVariable(executionId, variableName); // 获取指定流程变量名称的值
//        runtimeService.getVariables(executionId); // 获取所有流程变量的的值，封装为Map存放
//        runtimeService.getVariables(executionId, variableNames); // 获取集合 variableNames 内对应变量的值，并封装为Map存放

//        taskService.getVariable(taskId, variableName); // 获取指定流程变量名称的值
//        taskService.getVariables(taskId); // 获取所有流程变量的的值，封装为Map存放
//        taskService.getVariables(taskId, variableNames); // 获取集合 variableNames 内对应变量的值，并封装为Map存放

    }


    // 完成任务，以测试上面变量是否存在
    @Test
    public void completeMyPersonTask(){
        String taskId = "155002";
        processEngine.getTaskService()
                .complete(taskId);

        logger.info("完成我的任务，任务id={}", taskId);
    }


    // 查询流程变量的历史表
    @Test
    public void findHistoryVariables(){
        List<HistoricVariableInstance> list = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .variableName("请假天数")
                .list();

        list.forEach(hvi->{
            System.out.println(hvi.getId() + "   " + hvi.getProcessInstanceId() + "   " + hvi.getVariableName() + "   " + hvi.getVariableTypeName() + "   " + hvi.getValue());
            System.out.println("########################################");
        });
    }

}
