// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.ecs

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.project.Project
import icons.AwsIcons
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.ecs.model.ContainerDefinition
import software.amazon.awssdk.services.ecs.model.LogDriver
import software.amazon.awssdk.services.ecs.model.Service
import software.aws.toolkits.jetbrains.core.AwsResourceCache
import software.aws.toolkits.jetbrains.core.awsClient
import software.aws.toolkits.jetbrains.core.explorer.actions.SingleExplorerNodeActionGroup
import software.aws.toolkits.jetbrains.services.clouddebug.actions.StartRemoteShellAction
import software.aws.toolkits.jetbrains.services.cloudwatch.logs.CloudWatchLogWindow
import software.aws.toolkits.jetbrains.services.ecs.resources.EcsResources
import software.aws.toolkits.jetbrains.utils.notifyError
import software.aws.toolkits.resources.message
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException as CloudWatchResourceNotFoundException

class ContainerActions(
    private val project: Project,
    private val container: ContainerDetails
) : ActionGroup(container.containerDefinition.name(), null, null) {
    init {
        isPopup = true
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> = arrayOf(
        StartRemoteShellAction(project, container),
        ContainerLogsAction(project, container)
    )
}

data class ContainerDetails(val service: Service, val containerDefinition: ContainerDefinition)

class ServiceContainerActions : SingleExplorerNodeActionGroup<EcsServiceNode>("Containers") {
    override fun getChildren(selected: EcsServiceNode, e: AnActionEvent): List<AnAction> {
        val containers = try {
            AwsResourceCache.getInstance(selected.nodeProject).getResourceNow(EcsResources.listContainers(selected.value.taskDefinition()))
        } catch (e: Exception) {
            e.notifyError(message("cloud_debug.ecs.run_config.container.loading.error", e.localizedMessage), selected.nodeProject)
            return emptyList()
        }

        val containerActions = containers.map { ContainerActions(selected.nodeProject, ContainerDetails(selected.value, it)) }

        if (containerActions.isEmpty()) {
            return emptyList()
        }

        return listOf(Separator(), Separator(message("ecs.container_actions_group.label"))) + containerActions
    }
}

@Suppress("ComponentNotRegistered")
class ContainerLogsAction(
    private val project: Project,
    private val container: ContainerDetails
) : AnAction(message("ecs.service.container_logs.action_label"), null, AwsIcons.Resources.CloudWatch.LOGS) {

    private val logConfiguration: Pair<String, String>? by lazy {
        container.containerDefinition.logConfiguration().takeIf { it.logDriver() == LogDriver.AWSLOGS }?.options()?.let {
            val group = it["awslogs-group"] ?: return@let null
            val prefix = it["awslogs-stream-prefix"] ?: return@let null
            group to prefix
        }
    }

    override fun update(e: AnActionEvent) {
        if (logConfiguration == null) {
            e.presentation.isEnabled = false
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val (logGroup, logPrefix) = logConfiguration ?: return

        val window = CloudWatchLogWindow.getInstance(project)

        AwsResourceCache.getInstance(project).getResource(EcsResources.listTaskIds(container.service.clusterArn(), container.service.serviceArn())).thenAccept {
            val taskId = it.firstOrNull()
            if (taskId == null) {
                notifyError(message("ecs.service.logs.no_running_tasks"))
                return@thenAccept
            }
            val logStream = "$logPrefix/${container.containerDefinition.name()}/$taskId"
            if (!project.awsClient<CloudWatchLogsClient>().checkIfLogStreamExists(logGroup, logStream)) {
                notifyError(message("ecs.service.logs.no_log_stream"))
                return@thenAccept
            }
            window.showLogStream(logGroup, logStream)
        }
    }

    private fun CloudWatchLogsClient.checkIfLogStreamExists(logGroup: String, logStream: String) = try {
        val existingStreams = describeLogStreams { it.logGroupName(logGroup).logStreamNamePrefix(logStream) }
        existingStreams.logStreams().any { it.logStreamName() == logStream }
        // Thrown if the log group does not exist
    } catch (e: CloudWatchResourceNotFoundException) {
        false
    }
}
