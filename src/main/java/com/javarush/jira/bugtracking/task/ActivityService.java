package com.javarush.jira.bugtracking.task;

import com.javarush.jira.bugtracking.Handlers;
import com.javarush.jira.bugtracking.task.to.ActivityTo;
import com.javarush.jira.common.error.DataConflictException;
import com.javarush.jira.login.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.javarush.jira.bugtracking.task.TaskUtil.getLatestValue;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final TaskRepository taskRepository;

    private final Handlers.ActivityHandler handler;

    private static void checkBelong(HasAuthorId activity) {
        if (activity.getAuthorId() != AuthUser.authId()) {
            throw new DataConflictException("Activity " + activity.getId() + " doesn't belong to " + AuthUser.get());
        }
    }

    @Transactional
    public Activity create(ActivityTo activityTo) {
        checkBelong(activityTo);
        Task task = taskRepository.getExisted(activityTo.getTaskId());
        if (activityTo.getStatusCode() != null) {
            task.checkAndSetStatusCode(activityTo.getStatusCode());
        }
        if (activityTo.getTypeCode() != null) {
            task.setTypeCode(activityTo.getTypeCode());
        }
        return handler.createFromTo(activityTo);
    }

    @Transactional
    public void update(ActivityTo activityTo, long id) {
        checkBelong(handler.getRepository().getExisted(activityTo.getId()));
        handler.updateFromTo(activityTo, id);
        updateTaskIfRequired(activityTo.getTaskId(), activityTo.getStatusCode(), activityTo.getTypeCode());
    }

    @Transactional
    public void delete(long id) {
        Activity activity = handler.getRepository().getExisted(id);
        checkBelong(activity);
        handler.delete(activity.id());
        updateTaskIfRequired(activity.getTaskId(), activity.getStatusCode(), activity.getTypeCode());
    }

    private void updateTaskIfRequired(long taskId, String activityStatus, String activityType) {
        if (activityStatus != null || activityType != null) {
            Task task = taskRepository.getExisted(taskId);
            List<Activity> activities = handler.getRepository().findAllByTaskIdOrderByUpdatedDesc(task.id());
            if (activityStatus != null) {
                String latestStatus = getLatestValue(activities, Activity::getStatusCode);
                if (latestStatus == null) {
                    throw new DataConflictException("Primary activity cannot be delete or update with null values");
                }
                task.setStatusCode(latestStatus);
            }
            if (activityType != null) {
                String latestType = getLatestValue(activities, Activity::getTypeCode);
                if (latestType == null) {
                    throw new DataConflictException("Primary activity cannot be delete or update with null values");
                }
                task.setTypeCode(latestType);
            }
        }
    }

    private Long getTaskDuration(Task task, String fromStatus, String toStatus) {
        List<Activity> activityList = task.getActivities();
        Long taskId = task.getId();
        List<Activity> taskActivities = activityList.
                stream().
                filter(a -> a.getTaskId() == taskId).collect(Collectors.toList());

        boolean taskHasChangedStatus = taskActivities.size() >= 2;
        if (!taskHasChangedStatus) {
            return 0L;
        }

        Activity inProgressActivity = taskActivities.
                stream().
                filter(a -> {
                    assert a.getStatusCode() != null;
                    return a.getStatusCode()
                            .equals(fromStatus);
                }).
                findAny().
                orElse(null);

        if (inProgressActivity == null) {
            return 0L;
        }

        Activity readyForReviewActivity = taskActivities.
                stream().
                filter(a -> {
                    assert a.getStatusCode() != null;
                    return a.getStatusCode()
                            .equals(toStatus);
                }).
                findAny().
                orElse(null);

        if (readyForReviewActivity == null) {
            return 0L;
        }

        return Duration.between(inProgressActivity.getUpdated(), readyForReviewActivity.getUpdated()).getSeconds();


    }

    public Long getTaskInProgressDuration(Task task){
        return getTaskDuration(task, "in_progress", "ready_for_review");
    }

    public Long getTaskInTestingDuration(Task task){
        return getTaskDuration(task, "ready_for_review", "done");
    }
}
