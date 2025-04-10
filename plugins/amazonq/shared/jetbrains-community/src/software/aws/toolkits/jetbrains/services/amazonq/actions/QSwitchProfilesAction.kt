// Copyright 2025 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.amazonq.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import software.aws.toolkits.jetbrains.services.amazonq.profile.QRegionProfileDialog
import software.aws.toolkits.jetbrains.services.amazonq.profile.QRegionProfileManager
import software.aws.toolkits.resources.AmazonQBundle.message

class QSwitchProfilesAction : AnAction(message("action.q.switchProfiles.text")), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.icon = AllIcons.Actions.SwapPanels
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        QRegionProfileDialog(
            project,
            selectedProfile = QRegionProfileManager.getInstance().activeProfile(project)
        ).show()
    }
}
