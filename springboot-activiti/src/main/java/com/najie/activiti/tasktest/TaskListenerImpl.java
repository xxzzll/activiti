package com.najie.activiti.tasktest;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * @author xixi
 * @Description： 通过监听类来实现个人任务的办理人分配
 * @create 2020/6/27
 * @since 1.0.0
 */
public class TaskListenerImpl implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        // 通过查询数据库，查询出下一个要经办的人，通过 setAssignee() 方法，传递给任务
        delegateTask.setAssignee("灭绝师太");
    }
}
